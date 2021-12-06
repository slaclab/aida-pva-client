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
        RPCClientImpl client = null;
        ChannelProvider cp = null;
        try {
            ClientFactory.start();
            cp = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");
            if ( cp == null ) {
                System.out.println("Can't find PVA channel provider: 1");
            }
            client = new RPCClientImpl(channelName);
            cp = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");
            if ( cp == null ) {
                System.out.println("Can't find PVA channel provider: 2");
            }
            return client.request(request, 3.0);
        } catch (Exception e) {
            cp = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");
            if ( cp == null ) {
                System.out.println("Can't find PVA channel provider: 3");
            }
            throw new RPCRequestException(ERROR, e.getMessage(), e);
        } finally {
            cp = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");
            if ( cp == null ) {
                System.out.println("Can't find PVA channel provider: 4");
            }
            if ( client != null ) {
                client.destroy();
                cp = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");
                if ( cp == null ) {
                    System.out.println("Can't find PVA channel provider: 5");
                }
            }
            ClientFactory.stop();
            cp = ChannelProviderRegistryFactory.getChannelProviderRegistry().getProvider("pva");
            if ( cp == null ) {
                System.out.println("Can't find PVA channel provider: 6");
            }
        }
    }
}
