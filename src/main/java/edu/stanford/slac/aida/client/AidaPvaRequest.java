/**
 * @file
 * @brief Class to create and execute AIDA-PVA requests.
 * Use static members of this class to create and execute AIDA-PVA requests.  It
 * works using the builder pattern.
 */
package edu.stanford.slac.aida.client;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.*;

import static org.epics.pvdata.pv.Status.StatusType.FATAL;
import static org.epics.pvdata.pv.Status.StatusType.WARNING;

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

    private final String channelName;
    private Boolean isForChar = false;
    private Boolean isForCharArray = false;

    /**
     * Constructor
     *
     * @param channelName the request you want to get your request against
     */
    public AidaPvaRequest(String channelName) {
        this.channelName = channelName;
    }

    /**
     * To add an argument to the request
     *
     * @param name  name of argument
     * @param value to set for argument
     * @return AidaPvaRequestExecutor
     */
    public AidaPvaRequest with(String name, Object value) {
        argumentBuilder.addArgument(name, value);
        return this;
    }

    /**
     * To set returning argument of the request
     *
     * @param type to set
     * @return AidaPvaRequestExecutor
     */
    public AidaPvaRequest returning(AidaType type) {
        if (type.equals(AidaType.CHAR)) {
            isForChar = true;
            type = AidaType.BYTE;
        } else if (type.equals(AidaType.CHAR_ARRAY)) {
            isForCharArray = true;
            type = AidaType.BYTE_ARRAY;
        }
        argumentBuilder.addArgument("TYPE", type.toString());
        return this;
    }

    /**
     * To set VALUE argument of the request and execute the request
     *
     * @param value to set
     */
    public void set(Object value) throws RPCRequestException {
        argumentBuilder.addArgument("VALUE", value);
        AidaPvaClientUtils.executeRequest(() -> setter(null), false);
    }

    /**
     * To get the query.  This will add all the arguments you've specified
     * and then get the request
     */
    public <T extends PVField> Object get() throws RPCRequestException {
        return AidaPvaClientUtils.executeRequest(this::getter, isForChar || isForCharArray);
    }

    /**
     * To get the query.  This will add all the arguments you've specified
     * and then get the request
     *
     * @return the result of the request
     */
    PVStructure getter() throws RPCRequestException {
        return execute();
    }

    /**
     * To set VALUE argument of the request and execute the request
     *
     * @param value to set
     * @return AidaPvaRequestExecutor
     */
    PVStructure setter(Object value) throws RPCRequestException {
        if (value != null) {
            argumentBuilder.addArgument("VALUE", value);
        }
        return execute();
    }

    /**
     * Execute a request and return the PVStructure result.  Exceptions are thrown to caller
     *
     * @return the PVStructure result
     * @throws RPCRequestException if there is an error calling the request
     */
    private PVStructure execute() throws RPCRequestException {
        ClientFactory.start();
        RPCClientImpl client = null;
        try {
            client = new RPCClientImpl(channelName);
        } catch (Exception e) {
            throw new RPCRequestException(FATAL, e.getMessage(), e);
        }

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
        PVStructure result = client.request(request, 3.0);
        try {
            client.destroy();
            ClientFactory.stop();
        } catch (Exception e) {
            throw new RPCRequestException(WARNING, e.getMessage(), e);
        }
        return result;
    }
}
