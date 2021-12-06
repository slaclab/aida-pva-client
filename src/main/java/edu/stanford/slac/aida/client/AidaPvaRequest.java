/**
 * @file
 * @brief Class to create and execute AIDA-PVA requests.
 * Use static members of this class to create and execute AIDA-PVA requests.  It
 * works using the builder pattern.
 */
package edu.stanford.slac.aida.client;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.*;

/**
 * This is a general purpose AIDA PVA Request Executor
 * It follows the builder pattern for maximum configurability
 * You can use this general code to get any AIDA-PVA request
 */
public class AidaPvaRequest {
    /**
     * To be able to create fields in PV Access you need a field creator factory
     */
    private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

    /**
     * The argument Builder which is used to add any desired arguments
     */
    private final ArgumentBuilder argumentBuilder = new ArgumentBuilder();

    /**
     * The request executor implementation
     */
    private final PvaRequestExecutor requestExecutor;

    /**
     * The channel name
     */
    private final String channelName;

    /**
     * Internal: Constructor
     *
     * @param requestExecutor the request executor implementation
     * @param channelName     the request you want to get your request against
     */
    AidaPvaRequest(PvaRequestExecutor requestExecutor, String channelName) {
        this.requestExecutor = requestExecutor;
        this.channelName = channelName;
    }

    /**
     * To add an argument to the request.
     *
     * @param name  name of argument
     * @param value to set for argument
     * @return AidaPvaRequest
     */
    public AidaPvaRequest with(String name, Object value) {
        argumentBuilder.addArgument(name, value);
        return this;
    }

    /**
     * To set return type of the request
     *
     * @param type return type to set
     * @return AidaPvaRequest
     */
    public AidaPvaRequest returning(AidaType type) {
        if (type.equals(AidaType.CHAR)) {
            type = AidaType.BYTE;
        } else if (type.equals(AidaType.CHAR_ARRAY)) {
            type = AidaType.BYTE_ARRAY;
        }
        argumentBuilder.addArgument("TYPE", type.toString());
        return this;
    }

    /**
     * Set VALUE argument of the request and execute.  Exceptions are thrown to caller
     *
     * @param value to set
     */
    public void set(Object value) throws RPCRequestException {
        AidaPvaClientUtils.executeRequest(() -> setter(value));
    }

    /**
     * Set VALUE argument of the request and execute and return the result as an
     * PvaTable.  Exceptions are thrown to caller
     *
     * @param value to set
     * @return the PvaTable
     */
    public PvaTable setReturningTable(Object value) throws RPCRequestException {
        return (PvaTable) AidaPvaClientUtils.executeRequest(() -> setter(value));
    }

    /**
     * Execute the request and return the result as an Object which can be a scalar, scalar list
     * or PvaTable.  Exceptions are thrown to caller.
     *
     * @return the result of the request
     */
    public Object get() throws RPCRequestException {
        return AidaPvaClientUtils.executeRequest(this::getter);
    }

    /**
     * Internal: Execute the request and return the result PVStructure which can be an NTScalar,
     * NTScalarArray or NTTable.  Exceptions are thrown to caller
     *
     * @return the result of the request
     */
    PVStructure getter() throws RPCRequestException {
        return execute();
    }

    /**
     * Internal: Execute the set request with the given value and return the resulting
     * PVStructure which can an NTTable or empty.  Exceptions are thrown to caller
     *
     * @param value to set
     * @return the result of the request
     */
    PVStructure setter(Object value) throws RPCRequestException {
        if (value != null) {
            argumentBuilder.addArgument("VALUE", value);
        }
        return execute();
    }

    /**
     * Internal: Execute a request and return the PVStructure result.  Exceptions are thrown to caller
     *
     * @return the PVStructure result
     * @throws RPCRequestException if there is an error making the request
     */
    private PVStructure execute() throws RPCRequestException {
        // Build the arguments structure
        Structure arguments = argumentBuilder.build();

        // Build the uri structure
        Structure uriStructure =
                fieldCreate.createStructure(AidaType.NTURI_ID,
                        new String[]{"path", "scheme", "query"},
                        new Field[]{fieldCreate.createScalar(ScalarType.pvString), fieldCreate.createScalar(ScalarType.pvString), arguments}
                );

        // Make the query (contains the uri and arguments
        PVStructure request = PVDataFactory.getPVDataCreate().createPVStructure(uriStructure);
        request.getStringField("scheme").put("pva");

        // Set the request path
        request.getStringField("path").put(channelName);
        // Set the request query values
        PVStructure query = request.getStructureField("query");
        argumentBuilder.initializeQuery(query);

        // Execute the query
        return requestExecutor.executeRequest(channelName, request);
    }
}
