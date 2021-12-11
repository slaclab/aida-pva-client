/**
 * @noop @formatter:off
 * @file
 * @brief Class containing the AIDA-PVA client utilities.
 *
 * @see
 *  pvaRequest(),
 *  pvaGet(),
 *  pvaSet()
 * @noop @formatter:on
 */
package edu.stanford.slac.aida.client;

import edu.stanford.slac.aida.client.compat.AidaConsumer;
import edu.stanford.slac.aida.client.impl.EasyPvaRequestExecutor;
import edu.stanford.slac.aida.client.impl.PvAcccessRequestExecutor;
import edu.stanford.slac.aida.client.impl.PvaClientRequestExecutor;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @noop @formatter:off
 * Utility class to facilitate running AIDA-PVA requests.
 *
 * @section Details
 * In order to write a query it is very easy.
 * @subsection p1 e.g. 1: Simple get
 * @code
 *  Float bact = pvaGet("XCOR:LI03:120:LEFF", FLOAT);
 * @endcode
 * @subsection p2 e.g. 2: Multiple arguments
 * @code
 *  pvaRequest("NDRFACET:BUFFACQ")
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
 *  pvaSet("XCOR:LI31:41:BCON", 5.0f);
 * @endcode
 * @subsection p4 e.g. 4: Advanced set
 * @code
 *  Short status = ((PvaTable)pvaRequest("KLYS:LI31:31:TACT")
 *      .with("BEAM", 8)
 *      .with("DGRP", "DEV_DGRP")
 *      .set(0)
 *      ).getValues().get("status").get(0);
 * @endcode
 * @subsection p5 e.g. 5: Selecting the return value type
 * @code
 *  String value = pvaRequest("KLYS:LI31:31:TACT")
 *      .with("BEAM", 8)
 *      .with("DGRP", "DEV_DGRP")
 *      .returning(STRING)
 *      .get();
 * @endcode
 * @noop @formatter:on
 */
public class AidaPvaClientUtils {
    private static final Logger logger = Logger.getLogger(AidaPvaClientUtils.class.getName());

    private final static String DEFAULT_AIDA_PVA_CLIENT_REQUEST_EXECUTOR = "PvAccess";

    private static final PvaRequestExecutor pvaRequestExecutor;

    static {
        // Get pva Request Executor property (commandline or resource file).
        String requestExecutorName = System.getProperty("aida.pva.client.request.executor", DEFAULT_AIDA_PVA_CLIENT_REQUEST_EXECUTOR);

        // Override with the environment variable if it is set
        String aidaPvaRequestExecutorEnv = System.getenv("AIDA_PVA_CLIENT_REQUEST_EXECUTOR");
        if (aidaPvaRequestExecutorEnv != null) {
            requestExecutorName = aidaPvaRequestExecutorEnv;
        }

        // If we've overridden the default name then log it to the console
        if (!requestExecutorName.equals(DEFAULT_AIDA_PVA_CLIENT_REQUEST_EXECUTOR)) {
            logger.info("Request Executor: " + requestExecutorName);
        }

        if (requestExecutorName.equalsIgnoreCase("PvaClient")) {
            pvaRequestExecutor = new PvaClientRequestExecutor();
        } else if (requestExecutorName.equalsIgnoreCase("EasyPva")) {
            pvaRequestExecutor = new EasyPvaRequestExecutor();
        } else {
            pvaRequestExecutor = new PvAcccessRequestExecutor();
        }
    }

    /**
     * Builder for any type of request.
     *
     * @param query the starting query for this request
     * @return An AidaPvaRequest that can be further configured before calling get() or set()
     */
    public static AidaPvaRequest pvaRequest(final String query) {
        return new AidaPvaRequest(pvaRequestExecutor, query);
    }

    /**
     * Call a channel getter with no arguments
     *
     * @param channel the channel
     */
    public static Object pvaGet(final String channel) throws RPCRequestException {
        return new AidaPvaRequest(pvaRequestExecutor, channel).get();
    }

    /**
     * Call a channel getter with no arguments
     *
     * @param channel the channel
     * @param type    the type expected
     */
    public static Object pvaGet(final String channel, AidaType type) throws RPCRequestException {
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
    public static void pvaSet(final String channel, Object value) throws RPCRequestException {
        new AidaPvaRequest(pvaRequestExecutor, channel).setter(value);
    }

    /**
     * Convert a returned AIDA-PVA value into a simple type
     * @param result an AIDA-PVA result value
     * @return a simple scalar, scalar array, or PvaTable
     */
    public static Object pvUnpack(PVStructure result) {
        AidaType type = AidaType.from(result);
        Class<PVField> clazz = type.toPVFieldClass();

        if (type == AidaType.VOID || clazz == null) {
            return null;
        }

        String id = result.getStructure().getID();
        if (AidaType.NTSCALAR_ID.equals(id)) {
            return scalarResults(clazz, result);
        } else if (AidaType.NTSCALARARRAY_ID.equals(id)) {
            return scalarArrayResults(result, clazz);
        } else if (AidaType.NTTABLE_ID.equals(id)) {
            return tableResults(result);
        }
        return null;
    }

    /**
     * Convert the results structure to a list of Objects of the desired type.
     *
     * @param result the result to retrieve values from
     * @param clazz  the class to use to pull out the data.  Must extend PVField
     * @return the list of objects of the desired type
     */
    private static <T extends PVField> Object[] getScalarArrayValues(PVStructure result, Class<T> clazz) {
        final List<Object> values = new ArrayList<Object>();
        T array = result.getSubField(clazz, AidaType.NT_FIELD_NAME);
        PVUtils.arrayIterator(array, new AidaConsumer<Object>() {
            @Override
            public void accept(Object e) {
                values.add(e);
            }
        });
        return values.toArray();
    }

    /**
     * Execute the request and return the results.
     * It uses the supplied result-supplier to get the result.
     *
     * @param supplier the supplier of the results
     * @return the result
     */
    protected static Object executeRequest(AidaRequest<PVStructure> supplier) throws RPCRequestException {
        PVStructure result = supplier.execute();

        return pvUnpack(result);
    }

    /**
     * Internal: Execute scalar array request.
     * It uses the supplied result-supplier to get the result.
     *
     * @param supplier the supplier of the results
     * @param clazz    the PVField class of the scalar array data that will be returned from the supplier
     */
    private static <T extends PVField> Object executeScalarArrayRequest(AidaRequest<PVStructure> supplier, Class<T> clazz) throws RPCRequestException {
        return scalarArrayResults(supplier.execute(), clazz);
    }

    /**
     * Execute a scalar request and return value.
     * It uses the supplied result-supplier to get the result.
     *
     * @param supplier the supplier of the results
     * @param clazz    the PVField class of the scalar data that will be returned from the supplier
     */
    private static <T extends PVField> Object executeScalarRequest(AidaRequest<PVStructure> supplier, Class<T> clazz) throws RPCRequestException {
        return scalarResults(clazz, supplier.execute());
    }

    /**
     * Internal: Call this to get a scalar channel with no arguments
     *
     * @param query the request
     * @param type  the scalar type expected
     */
    private static Object getScalarRequest(final String query, final AidaType type) throws RPCRequestException {
        Class<PVField> clazz = type.toPVFieldClass();
        return executeScalarRequest(
                new AidaRequest<PVStructure>() {
                    @Override
                    public PVStructure execute() throws RPCRequestException {
                        return new AidaPvaRequest(pvaRequestExecutor, query)
                                .returning(AidaType.valueOf(realReturnType(type)))
                                .getter();
                    }
                },
                clazz);
    }

    /**
     * Internal: Call this to get a scalar array channel with no arguments
     *
     * @param query the request
     * @param type  the scalar array type expected
     * @return the array value
     */
    private static <T extends PVField> Object getArrayRequest(final String query, final AidaType type) throws RPCRequestException {
        Class<T> clazz = type.toPVFieldClass();
        return executeScalarArrayRequest(
                new AidaRequest<PVStructure>() {
                    @Override
                    public PVStructure execute() throws RPCRequestException {
                        return new AidaPvaRequest(pvaRequestExecutor, query)
                                .returning(AidaType.valueOf(realReturnType(type)))
                                .getter();
                    }
                },
                clazz);
    }

    /**
     * Internal: get table results in a PvaTable structure
     *
     * @param query the request
     */
    private static PvaTable getTableRequest(final String query) throws RPCRequestException {
        return getTableResults(new AidaRequest<PVStructure>() {
            @Override
            public PVStructure execute() throws RPCRequestException {
                return new AidaPvaRequest(pvaRequestExecutor, query).returning(AidaType.TABLE).getter();
            }
        });
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
     * It uses the supplied result-supplier to get the result and marshal it into a PvaTable object.
     *
     * @param supplier the supplier of the results
     */
    private static PvaTable getTableResults(AidaRequest<PVStructure> supplier) throws RPCRequestException {
        return tableResults(supplier.execute());
    }

    /**
     * Internal: To get an PvaTable from a PVStructure result object
     *
     * @param result the results to be displayed
     * @return PvaTable
     */
    static PvaTable tableResults(PVStructure result) {
        // Aida table for return
        PvaTable table = new PvaTable();

        // Get labels
        final List<String> labels = new ArrayList<String>();
        PVUtils.stringArrayIterator(result.getSubField(PVStringArray.class, AidaType.NT_LABELS_NAME), new AidaConsumer<String>() {
            @Override
            public void accept(String t) {
                labels.add(t);
            }
        });
        table.setLabels(labels);

        // values
        Map<String, List<Object>> values = new HashMap<String, List<Object>>();
        PVField[] pvFields = result.getSubField(PVStructure.class, AidaType.NT_FIELD_NAME).getPVFields();
        for (PVField column : pvFields) {
            final List<Object> columnValues = new ArrayList<Object>();
            PVUtils.arrayIterator(column, new AidaConsumer<Object>() {
                @Override
                public void accept(Object s) {
                    columnValues.add(s);
                }
            });
            values.put(column.getFieldName(), columnValues);
        }
        table.setValues(values);
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
    private static <T extends PVField> Object[] scalarArrayResults(PVStructure result, Class<T> clazz) {
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

