package edu.stanford.slac.aida.client.impl;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvAcccessRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        try {
            ClientFactory.start();
            RPCClientImpl client = new RPCClientImpl(channelName);
            PVStructure result = client.request(request, 3.0);
            client.destroy();
            ClientFactory.stop();
            return result;
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage(), e);
        }
    }
}
