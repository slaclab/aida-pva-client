package edu.stanford.slac.aida.client.impl;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.*;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.epics.pvaccess.ClientFactory.PROVIDER_NAME;
import static org.epics.pvaccess.client.ChannelProvider.PRIORITY_DEFAULT;
import static org.epics.pvaccess.client.ChannelProviderRegistryFactory.getChannelProviderRegistry;
import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvAcccessRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        try {
            ClientFactory.start();
            ChannelProvider channelProvider = getChannelProviderRegistry().getProvider(PROVIDER_NAME);
            CountDownLatch doneSignal = new CountDownLatch(1);
            ChannelRequesterImpl channelRequester = new ChannelRequesterImpl();
            Channel channel = channelProvider.createChannel(channelName, channelRequester, PRIORITY_DEFAULT);

            ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl(doneSignal);
            channel.createChannelGet(channelGetRequester, request);

            if (doneSignal.await(10, SECONDS)) {
                throw new RPCRequestException(ERROR, "Failed to get value (timeout condition).");
            }
            ClientFactory.stop();
            return channelGetRequester.getResult();
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage(), e);
        }

    }

    static class ChannelRequesterImpl implements ChannelRequester {
        public ChannelRequesterImpl() {
        }

        @Override
        public void channelCreated(Status status, Channel channel) {
        }

        @Override
        public void channelStateChange(Channel channel, Channel.ConnectionState connectionState) {
        }

        @Override
        public String getRequesterName() {
            return getClass().getName();
        }

        @Override
        public void message(String message, MessageType messageType) {
        }
    }

    static class ChannelGetRequesterImpl implements ChannelGetRequester {
        private final CountDownLatch doneSignaler;
        private PVStructure result = null;

        public ChannelGetRequesterImpl(CountDownLatch doneSignaler) {
            this.doneSignaler = doneSignaler;
        }

        @Override
        public String getRequesterName() {
            return getClass().getName();
        }

        public PVStructure getResult() {
            return result;
        }

        @Override
        public void message(String message, MessageType messageType) {
        }

        @Override
        public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
            if (status.isSuccess()) {
                channelGet.lastRequest();
                channelGet.get();
            } else
                doneSignaler.countDown();
        }

        @Override
        public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet changedBitSet) {
            if (status.isSuccess()) {
                // NOTE: no need to call channelGet.lock()/unlock() since we read pvStructure in the same thread (i.e. in the callback)
                result = pvStructure;
            }
            doneSignaler.countDown();
        }
    }
}
