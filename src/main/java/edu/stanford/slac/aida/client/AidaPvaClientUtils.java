/**
 * @noop @formatter:off
 * @file
 * @brief Utility class to facilitate running all the AIDA-PVA tests.
 * - Test Suite
 *  - Tests
 *    - Test Cases
 *
 * @see
 *  testSuiteHeader(),
 *  testCaseHeader(),
 *  testHeader(),
 *  request(),
 *  getRequest(),
 *  setRequest()
 * @noop @formatter:on
 */
package edu.stanford.slac.aida.client;

import org.apache.commons.lang3.tuple.Pair;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @noop @formatter:off
 * Utility class to facilitate running AIDA-PVA requests.
 *
 * @section Details
 * In order to write a query it is very easy.
 * @subsection p1 e.g. 1: Simple get
 * @code
 *  Float bact = getRequest("XCOR:LI03:120:LEFF", FLOAT);
 * @endcode
 * @subsection p2 e.g. 2: Multiple arguments
 * @code
 *  request("NDRFACET:BUFFACQ")
 *      .with("BPMD", 57)
 *      .with("NRPOS", 180)
 *      .with("BPMS", List.of(
 *              "BPMS:LI11:501",
 *              "BPMS:LI11:601",
 *              "BPMS:LI11:701",
 *              "BPMS:LI11:801"))
 *      .get();
 * @endcode
 * @subsection p3 e.g. 3: Simple set
 * @code
 *  setRequest("XCOR:LI31:41:BCON", 5.0f);
 * @endcode
 * @subsection p4 e.g. 4: Advanced set
 * @code
 *  Short status = ((AidaTable)request("KLYS:LI31:31:TACT")
 *      .with("BEAM", 8)
 *      .with("DGRP", "DEV_DGRP")
 *      .setReturningTable(0)
 *      ).getValues().get("status").get(0);
 * @endcode
 * @subsection p5 e.g. 5: Selecting the return value type
 * @code
 *  String value = request("KLYS:LI31:31:TACT")
 *      .with("BEAM", 8)
 *      .with("DGRP", "DEV_DGRP")
 *      .returning(STRING)
 *      .get();
 * @endcode
 * @noop @formatter:on
 */
public class AidaPvaClientUtils {

    /**
     * Change here for whatever implementation we require for the executor.  Pojo, PvaClient, or ExPva
     *
     * @return the chosen request executor
     */
    private static PvaRequestExecutor requestExecutor() {
        return PvaClientPvaRequest::executeRequest;
    }

    /**
     * Builder for any request.  When the get() or set() are finally called the test runs and
     * the results are displayed in a standardised format
     *
     * @param query the starting query for this request
     * @return An AidaPvaRequest that can be further configured before calling get() or set()
     */
    public static AidaPvaRequest request(final String query) {
        return new AidaPvaRequest(requestExecutor(), query);
    }

    /**
     * Call a channel getter with no arguments
     *
     * @param channel the channel
     * @param type    the type expected
     */
    public static Object getRequest(final String channel, AidaType type) throws RPCRequestException {
        String stringType = type.toString();
        if (type.equals(AidaType.TABLE)) {
            return getTableRequest(channel);
        } else if (stringType.endsWith("_ARRAY")) {
            return getArrayRequest(channel, type);
        } else {
            return getScalarRequest(channel, type);
        }
    }

    /**
     * Call a channel setter with the given value.
     *
     * @param channel the channel
     * @param value   the value to set
     */
    public static void setRequest(final String channel, Object value) throws RPCRequestException {
        new AidaPvaRequest(requestExecutor(), channel).setter(value);
    }

    /**
     * Convert the results structure to a list of Objects of the desired type.
     *
     * @param result the result to retrieve values from
     * @param clazz  the class to use to pull out the data.  Must extend PVField
     * @return the list of objects of the desired type
     */
    private static <T extends PVField> List<Object> getScalarArrayValues(PVStructure result, Class<T> clazz) {
        List<Object> values = new ArrayList<>();
        T array = result.getSubField(clazz, AidaType.NT_FIELD_NAME);
        PVUtils.arrayIterator(array, values::add);
        return values;
    }

    /**
     * Execute the request and return the results.
     * It uses the supplied result-supplier to get the result.
     *
     * @param supplier the supplier of the results
     * @return the result
     */
    protected static Object executeRequest(AidaGetter<PVStructure> supplier) throws RPCRequestException {
        PVStructure result = supplier.get();

        AidaType type = AidaType.from(result);
        Class<PVField> clazz = type.toPVFieldClass();

        if (type == AidaType.VOID || clazz == null) {
            return null;
        }

        switch (result.getStructure().getID()) {
            case AidaType.NTSCALAR_ID:
                return scalarResults(clazz, result);
            case AidaType.NTSCALARARRAY_ID:
                return scalarArrayResults(result, clazz);
            case AidaType.NTTABLE_ID:
                return tableResults(result);
        }
        return null;
    }

    /**
     * Internal: Execute scalar array request.
     * It uses the supplied result-supplier to get the result.
     *
     * @param supplier the supplier of the results
     * @param clazz    the PVField class of the scalar array data that will be returned from the supplier
     */
    private static <T extends PVField> Object executeScalarArrayRequest(AidaGetter<PVStructure> supplier, Class<T> clazz) throws RPCRequestException {
        return scalarArrayResults(supplier.get(), clazz);
    }

    /**
     * Execute a scalar request and return value.
     * It uses the supplied result-supplier to get the result.
     *
     * @param supplier the supplier of the results
     * @param clazz    the PVField class of the scalar data that will be returned from the supplier
     */
    private static <T extends PVField> Object executeScalarRequest(AidaGetter<PVStructure> supplier, Class<T> clazz) throws RPCRequestException {
        return scalarResults(clazz, supplier.get());
    }

    /**
     * Internal: Call this to get a scalar channel with no arguments
     *
     * @param query the request
     * @param type  the scalar type expected
     */
    private static Object getScalarRequest(final String query, AidaType type) throws RPCRequestException {
        Class<PVField> clazz = type.toPVFieldClass();
        return executeScalarRequest(
                () -> new AidaPvaRequest(requestExecutor(), query)
                        .returning(AidaType.valueOf(realReturnType(type)))
                        .getter(),
                clazz);
    }

    /**
     * Internal: Call this to get a scalar array channel with no arguments
     *
     * @param query the request
     * @param type  the scalar array type expected
     * @return the array value
     */
    private static <T extends PVField> Object getArrayRequest(final String query, AidaType type) throws RPCRequestException {
        Class<T> clazz = type.toPVFieldClass();
        return executeScalarArrayRequest(
                () -> new AidaPvaRequest(requestExecutor(), query)
                        .returning(AidaType.valueOf(realReturnType(type)))
                        .getter(),
                clazz);
    }

    /**
     * Internal: Call this to run a test and display results for scalar array channels with no arguments
     *
     * @param query the request
     */
    private static AidaTable getTableRequest(final String query) throws RPCRequestException {
        return getTableResults(() -> new AidaPvaRequest(requestExecutor(), query).returning(AidaType.TABLE).getter());
    }

    /**
     * Internal: This will get a scalar value from the returned result structure.
     * In AIDA-PVA CHAR does not exist so requests are made using BYTE and marshalled into char on return
     *
     * @param result the result to retrieve value from
     * @param clazz  the class to use to pull out the data.  Must extend PVField
     * @return the object of the desired type
     */
    private static <T extends PVField> Object getScalarValue(PVStructure result, Class<T> clazz) {
        return PVUtils.extractScalarValue(result.getSubField(clazz, AidaType.NT_FIELD_NAME));
    }

    /**
     * Internal: Get table results.
     * It uses the supplied result-supplier to get the result and marshal it into a AidaTable object.
     *
     * @param supplier the supplier of the results
     */
    private static AidaTable getTableResults(AidaGetter<PVStructure> supplier) throws RPCRequestException {
        return tableResults(supplier.get());
    }

    /**
     * Internal: To get an AidaTable from a PVStructure result object
     *
     * @param result the results to be displayed
     * @return AidaTable
     */
    static AidaTable tableResults(PVStructure result) {
        // Aida table for return
        AidaTable table = new AidaTable();

        // Get labels
        List<String> labels = new ArrayList<>();
        PVUtils.stringArrayIterator(result.getSubField(PVStringArray.class, AidaType.NT_LABELS_NAME), labels::add);
        table.setLabels(labels);

        // values
        table.setValues(
                Arrays.stream(result.getSubField(PVStructure.class, AidaType.NT_FIELD_NAME).getPVFields())
                        .map(column -> {
                            List<Object> columnValues = new ArrayList<>();
                            PVUtils.arrayIterator(column, columnValues::add);
                            return Pair.of(column.getFieldName(), columnValues);
                        })
                        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
        );
        return table;
    }

    /**
     * Internal: Return scalar results
     *
     * @param clazz  the class of the scalar array
     * @param result the result
     * @return Scalar Value
     */
    private static <T extends PVField> Object scalarResults(Class<T> clazz, PVStructure result) {
        return getScalarValue(result, clazz);
    }

    /**
     * Internal: Return scalar array results
     *
     * @param result the results
     * @param clazz  the class of the scalar array
     * @return Scalar array values
     */
    private static <T extends PVField> List<Object> scalarArrayResults(PVStructure result, Class<T> clazz) {
        return getScalarArrayValues(result, clazz);
    }

    /**
     * Determine the real return type value for TYPE argument from the given AidaType
     *
     * @param type the given AidaType
     * @return the real return type value for TYPE argument
     */
    private static String realReturnType(AidaType type) {
        return type.equals(AidaType.CHAR_ARRAY) ? "BYTE_ARRAY" : type.equals(AidaType.CHAR) ? "BYTE" : type.toString();
    }
}

