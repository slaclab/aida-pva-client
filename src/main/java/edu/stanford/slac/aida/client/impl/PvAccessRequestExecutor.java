package edu.stanford.slac.aida.client.impl;

import edu.stanford.slac.aida.client.PvaRequestExecutor;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvAccessRequestExecutor implements PvaRequestExecutor {
    public PVStructure executeRequest(String channelName, PVStructure request, Double timeout) throws RPCRequestException {
        RPCClientImpl client = null;
        try {
            client = new RPCClientImpl(channelName);
            return client.request(request, timeout);
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage(), e);
        } finally {
            if (client != null) {
                client.destroy();
            }
        }
    }
}
