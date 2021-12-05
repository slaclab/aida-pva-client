package edu.stanford.slac.aida.client.impl;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

import static org.epics.pvdata.pv.Status.StatusType.FATAL;
import static org.epics.pvdata.pv.Status.StatusType.WARNING;

public class PojoRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        ClientFactory.start();
        RPCClientImpl client;
        try {
            client = new RPCClientImpl(channelName);
        } catch (Exception e) {
            throw new RPCRequestException(FATAL, e.getMessage(), e);
        }

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
