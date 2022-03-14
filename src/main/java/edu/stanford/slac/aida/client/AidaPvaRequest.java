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

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.getterChannel;
import static org.epics.pvdata.pv.Status.StatusType.ERROR;

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
     * The request executor timout value
     */
    private Double timeout = 3.0;

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
     * To get the channel name fixed up for getter requests whenever needed
     *
     * @return fixed up channel name when getting for delegated channels
     */
    private String getChannelName() {
        if (!argumentBuilder.contains("VALUE")) {
            return getterChannel(this.channelName);
        } else {
            return this.channelName;
        }
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
     * To set the request timeout.
     *
     * @param timeout the timeout to use instead of the default 3.0 seconds
     * @return AidaPvaRequest
     */
    public AidaPvaRequest timeout(Double timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * To set the request timeout.  This takes an Object and casts as a Double safely
     *
     * @param timeout the timeout to use instead of the default 3.0 seconds
     * @return AidaPvaRequest
     */
    public AidaPvaRequest timeout(Object timeout) {
        timeout(Double.valueOf(timeout.toString()));
        return this;
    }

    /**
     * To set return type of the request
     *
     * @param type return type to set
     * @return AidaPvaRequest
     */
    public AidaPvaRequest returning(AidaType type) {
        if (type.equals(AidaType.AIDA_CHAR)) {
            type = AidaType.AIDA_BYTE;
        } else if (type.equals(AidaType.AIDA_CHAR_ARRAY)) {
            type = AidaType.AIDA_BYTE_ARRAY;
        }
        argumentBuilder.addArgument("TYPE", type.string());
        return this;
    }

    /**
     * Set VALUE argument of the request and execute and return the result as an
     * PvaTable.  Return null if the channel return void
     *
     * @param value to set
     * @return the PvaTable
     */
    public PvaTable set(final Object value) throws RPCRequestException {
        Object response = AidaPvaClientUtils.executeRequest(new AidaRequest<PVStructure>() {
            @Override
            public PVStructure execute() throws RPCRequestException {
                return setter(value);
            }
        });
        if (response == null || response instanceof String) {
            return null;
        } else {
            return ((PvaTable) response);
        }
    }

    /**
     * Execute the request and return the result as an Object which can be a scalar, scalar list
     * or PvaTable.  Exceptions are thrown to caller.
     *
     * @return the result of the request
     */
    public Object get() throws RPCRequestException {
        return AidaPvaClientUtils.executeRequest(new AidaRequest<PVStructure>() {
            @Override
            public PVStructure execute() throws RPCRequestException {
                return getter();
            }
        });
    }

    /**
     * Get the uri formed from the builder
     *
     * @return the NTURI PVStructure
     */
    public PVStructure uri() throws RPCRequestException {
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
        request.getStringField("path").put(getChannelName());
        // Set the request query values
        PVStructure query = request.getStructureField("query");
        argumentBuilder.initializeQuery(query);
        return request;
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
        PVStructure request = uri();

        // Execute the query
        try {
            return requestExecutor.executeRequest(getChannelName(), request, this.timeout);
        } catch (RPCRequestException e) {
            throw new RPCRequestException(ERROR, getChannelName() + "(" + argumentBuilder + ") :" + abbreviate(e.getMessage()));
        }
    }

    /**
     * Show an abbreviated version of the error message
     *
     * @param message the error message
     * @return abbreviated version of the given message
     */
    static String abbreviate(String message) {
        int end = message.indexOf(".");
        int endC = message.indexOf(", cause:");
        if (end == -1) {
            return message;
        }
        if (endC == -1) {
            endC = end;
        }
        return message.substring(0, Math.min(end, endC));
    }

}
