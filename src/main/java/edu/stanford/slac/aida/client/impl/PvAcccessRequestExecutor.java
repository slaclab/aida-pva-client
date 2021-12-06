package edu.stanford.slac.aida.client.impl;

import org.epics.pvaccess.ClientFactory;
import org.epics.pvaccess.client.rpc.RPCClient;
import org.epics.pvaccess.client.rpc.RPCClientImpl;
import org.epics.pvaccess.client.rpc.RPCClientRequester;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.*;

import java.nio.ByteBuffer;

import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvAcccessRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        try {
            ServiceClientRequesterImpl requester = new ServiceClientRequesterImpl();
            RPCClientImpl client = new RPCClientImpl(channelName, requester);
            if (!client.waitConnect(3.0)) throw new RuntimeException("connection timeout");

            client.sendRequest(request);
            if (!client.waitResponse(3.0)) throw new RuntimeException("response timeout");

            Status status = requester.getStatus();
            if (status.isSuccess()) {
                return requester.getResult();
            }
            throw new RPCRequestException(status.getType(), status.getMessage());
/*
            RPCClientImpl client = new RPCClientImpl(channelName);
            PVStructure result = client.request(request, 3.0);
            return result;
*/
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage(), e);
        } finally {
            ClientFactory.stop();
        }
    }

    private static class ServiceClientRequesterImpl implements RPCClientRequester {
        private volatile Status status;
        private volatile PVStructure result;

        public String getRequesterName() {
            return getClass().getName();
        }

        public void message(String message, MessageType messageType) {
        }

        public void connectResult(RPCClient client, Status status) {
        }

        public void requestResult(RPCClient client, Status status, PVStructure pvResult) {
            this.status = status;
            this.result = pvResult;
        }

        public Status getStatus() {
            if (status == null) {
                return new Status() {
                    @Override
                    public StatusType getType() {
                        return ERROR;
                    }

                    @Override
                    public String getMessage() {
                        return "Unspecified Error";
                    }

                    @Override
                    public String getStackDump() {
                        return null;
                    }

                    @Override
                    public boolean isOK() {
                        return false;
                    }

                    @Override
                    public boolean isSuccess() {
                        return false;
                    }

                    @Override
                    public void serialize(ByteBuffer byteBuffer, SerializableControl serializableControl) {
                    }

                    @Override
                    public void deserialize(ByteBuffer byteBuffer, DeserializableControl deserializableControl) {
                    }
                };
            }
            return status;
        }

        /**
         * @return the result
         */
        public PVStructure getResult() {
            return result;
        }

    }

}
