package edu.stanford.slac.aida.client.impl;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvAcccessRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        try {
            ChannelProvider cp = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");
            if ( cp == null ) {
                throw new RPCRequestException(ERROR, "No pva provider registered");
            }
            RPCClientImpl client = new RPCClientImpl(channelName);
            PVStructure result = client.request(request, 3.0);
            return result;
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage(), e);
        } finally {
            ClientFactory.stop();
        }
    }
}
