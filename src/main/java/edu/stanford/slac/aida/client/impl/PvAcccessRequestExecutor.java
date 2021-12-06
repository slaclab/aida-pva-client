package edu.stanford.slac.aida.client.impl;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderFactory;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

import java.util.Arrays;

import static org.epics.ca.ClientFactory.PROVIDER_NAME;
import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvAcccessRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        RPCClientImpl client = null;
        try {
            client = new RPCClientImpl(channelName);
            return client.request(request, 3.0);
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage(), e);
        } finally {
            if ( client != null ) {
                client.destroy();
            }
//            ClientFactory.stop();
        }
    }
}
