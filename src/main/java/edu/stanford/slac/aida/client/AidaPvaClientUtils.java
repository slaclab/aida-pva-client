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
import org.epics.pvdata.pv.PVByteArray;
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
 *  Float bact = getRequest("XCOR:LI03:120:LEFF", FLOAT, "Float BACT");
 * @endcode
 * @subsection p2 e.g. 2: Multiple arguments
 * @code
 *  request("NDRFACET:BUFFACQ", "BPM Values")
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
 *  AidaTable table = setRequest("XCOR:LI31:41:BCON", 5.0f);
 * @endcode
 * @subsection p4 e.g. 4: Advanced set
 * @code
 *  Short status = request("KLYS:LI31:31:TACT", "Deactivated")
 *      .with("BEAM", 8)
 *      .with("DGRP", "DEV_DGRP")
 *      .set(0);
 * @endcode
 * @subsection p5 e.g. 5: Selecting the return value type
 * @code testSuiteHeader(" AIDA - PVA SLC Klystron TESTS ");
 *  String value = request("KLYS:LI31:31:TACT", "String")
 *      .with("BEAM", 8)
 *      .with("DGRP", "DEV_DGRP")
 *      .returning(STRING)
 *      .get();
 * @endcode
 * @noop @formatter:on
 */
public class AidaPvaClientUtils {
    /**
     * This functional interface is the similar to a Supplier except that
     * we throw RPCRequestExceptions for errors and always return PVStructures
     */
    @FunctionalInterface
    public interface AidaGetter<T extends PVStructure> {
        T get() throws RPCRequestException;
    }

    /**
     * Builder for any request.  When the get() or set() are finally called the test runs and
     * the results are displayed in a standardised format
     *
     * @param query   the starting query for this request
     * @return An AidaPvaRequest that can be further configured before calling get() or set()
     */
    public static AidaPvaRequest request(final String query) {
        return new AidaPvaRequest(query);
    }

    /**
     * Call this to run a query for channels with no arguments
     *  @param query   the request
     * @param type    the type expected
     */
    public static Object getRequest(final String query, AidaType type) throws RPCRequestException {
        String stringType = type.toString();
        if (type.equals(AidaType.TABLE)) {
            return getTableRequest(query);
        } else if (stringType.endsWith("_ARRAY")) {
            return getArrayRequest(query, type);
        } else {
            return getScalarRequest(query, type);
        }
    }

    /**
     * Call this to run a setter test that does not return anything
     *
     * @param query the request
     * @param value the value to set
     */
    public static void setRequest(final String query, Object value) throws RPCRequestException {
        new AidaPvaRequest(query).setter(value);
    }

    /**
     * This will get a list of scalar value from the returned result structure.
     * In AIDA-PVA CHAR_ARRAY does not exist so requests are made using
     * BYTE_ARRAY and marshalled into CHAR_ARRAY on return
     *
     * @param result         the result to retrieve values from
     * @param clazz          the class to use to pull out the data.  Must extend PVField
     * @param isForCharArray is this for the pseudo-type CHAR_ARRAY.
     * @return the list of objects of the desired type
     */
    static <T extends PVField> List<Object> getScalarArrayValues(PVStructure result, Class<T> clazz, boolean isForCharArray) {
        List<Object> values = new ArrayList<>();
        T array = result.getSubField(clazz, AidaType.NT_FIELD_NAME);
        if (PVByteArray.class.equals(clazz)) {
            PVUtils.byteArrayIterator((PVByteArray) array, b -> values.add(isForCharArray ? "'" + (char) (b & 0xFF) + "'" : b));
        } else {
            PVUtils.arrayIterator(array, values::add);
        }
        return values;
    }

    /**
     * Execute the request and return the results.
     * It uses the supplied result-supplier to get the result.
     *
     * @param supplier             the supplier of the results
     * @param isForCharOrCharArray is for a char or char_array
     * @return the result
     */
    static Object executeRequest(AidaGetter<PVStructure> supplier, boolean isForCharOrCharArray) throws RPCRequestException {
        PVStructure result = supplier.get();

        AidaType type = AidaType.from(result);
        Class<PVField> clazz = type.toPVFieldClass();

        if (type == AidaType.VOID || clazz == null) {
            return null;
        }

        switch (result.getStructure().getID()) {
            case AidaType.NTSCALAR_ID:
                return scalarResults(clazz, isForCharOrCharArray, result);
            case AidaType.NTSCALARARRAY_ID:
                return scalarArrayResults(result, clazz, isForCharOrCharArray);
            case AidaType.NTTABLE_ID:
                return tableResults(result);
        }
        return null;
    }

    /**
     * Display scalar array result.  This displays one line for each array entry in a standard way
     * for any scalar array results.
     * It uses the supplied result-supplier to get the result so that if it gives an
     * error, the error can be displayed in a standard way too.
     *  @param supplier       the supplier of the results
     * @param clazz          the class of the scalar array data that has been returned in the structure
     * @param isForCharArray if this is for a pseudo-type CHAR_ARRAY
     */
    static <T extends PVField> Object displayScalarArrayResults(AidaGetter<PVStructure> supplier, Class<T> clazz, boolean isForCharArray) throws RPCRequestException {
        return scalarArrayResults(supplier.get(), clazz, isForCharArray);
    }

    /**
     * Display a scalar result.  This displays one line in a standard way for any scalar result.
     * It uses the supplied result-supplier to get the result so that if it gives an
     * error, the error can be displayed in a standard way too.
     *  @param supplier  the supplier of the results
     * @param clazz     the class of the scalar data returned in the structure
     * @param isForChar if this is for a pseudo-type CHAR
     */
    static <T extends PVField> Object displayScalarResult(AidaGetter<PVStructure> supplier, Class<T> clazz, boolean isForChar) throws RPCRequestException {
        return scalarResults(clazz, isForChar, supplier.get());
    }

    /**
     * Call this to run a test and display results for scalar channels with no arguments
     *  @param query   the request
     * @param type    the scalar type expected
     */
    private static Object getScalarRequest(final String query, AidaType type) throws RPCRequestException {
        Class<PVField> clazz = type.toPVFieldClass();
        return displayScalarResult(
                () -> new AidaPvaRequest(query)
                        .returning(AidaType.valueOf(realReturnType(type)))
                        .getter(),
                clazz, type.equals(AidaType.CHAR));
    }

    /**
     * Call this to run a test and display results for scalar array channels with no arguments
     *
     * @param query   the request
     * @param type    the scalar array type expected
     * @return the array value
     */
    private static <T extends PVField> Object getArrayRequest(final String query, AidaType type) throws RPCRequestException {
        Class<T> clazz = type.toPVFieldClass();
        return displayScalarArrayResults(
                () -> new AidaPvaRequest(query)
                        .returning(AidaType.valueOf(realReturnType(type)))
                        .getter(),
                clazz, type.equals(AidaType.CHAR_ARRAY));
    }

    /**
     * Call this to run a test and display results for scalar array channels with no arguments
     *  @param query   the request
     *
     */
    private static AidaTable getTableRequest(final String query) throws RPCRequestException {
        return getTableResults(() -> new AidaPvaRequest(query).returning(AidaType.TABLE).getter());
    }

    /**
     * This will get a scalar value from the returned result structure.
     * In AIDA-PVA CHAR does not exist so requests are made using BYTE and marshalled into char on return
     *
     * @param result    the result to retrieve value from
     * @param clazz     the class to use to pull out the data.  Must extend PVField
     * @param isForChar is this for the pseudo-type CHAR.
     * @return the object of the desired type
     */
    private static <T extends PVField> Object getScalarValue(PVStructure result, Class<T> clazz, boolean isForChar) {
        Object value = PVUtils.extractScalarValue(result.getSubField(clazz, AidaType.NT_FIELD_NAME));
        if (value instanceof Byte && isForChar) {
            return "'" + (char) ((Byte) value & 0xFF) + "'";
        } else {
            return value;
        }
    }

    /**
     * Display a table result.  This displays tabular results in a standard way
     * It uses the supplied result-supplier to get the result so that if it gives an
     * error, the error can be displayed in a standard way too.
     *
     * @param supplier the supplier of the results
     */
    private static AidaTable getTableResults(AidaGetter<PVStructure> supplier) throws RPCRequestException {
        return tableResults(supplier.get());
    }

    /**
     * To display formatted table results in a standard way
     *
     * @param result the results to be displayed
     * @return AidaTable a table of data
     */
    private static AidaTable tableResults(PVStructure result) {
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
     * Return scalar results
     *
     * @param clazz     the class of the scalar array
     * @param isForChar is this for a char
     * @param result    the result
     * @return Scalar Value
     */
    private static <T extends PVField> Object scalarResults(Class<T> clazz, boolean isForChar, PVStructure result) {
        return getScalarValue(result, clazz, isForChar);
    }

    /**
     * Return scalar array results
     *
     * @param result         the results
     * @param clazz          the class of the scalar array
     * @param isForCharArray is this for a char array
     * @return Scalar array values
     */
    private static <T extends PVField> List<Object> scalarArrayResults(PVStructure result, Class<T> clazz, boolean isForCharArray) {
        return getScalarArrayValues(result, clazz, isForCharArray);
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

    /**
     * Determine the pseudo return type for request to be used in display
     *
     * @param type the given AidaType
     * @return the pseudo return type
     */
    private static String pseudoReturnType(AidaType type) {
        return type.equals(AidaType.CHAR_ARRAY) ? "CHAR_ARRAY" : type.equals(AidaType.CHAR) ? "CHAR" : realReturnType(type);
    }
}

