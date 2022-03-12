package edu.stanford.slac.aida.client.impl;

import edu.stanford.slac.aida.client.PvaRequestExecutor;
import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvaClientRequestExecutor implements PvaRequestExecutor {
    public PVStructure executeRequest(String channelName, PVStructure request, Double timeout) throws RPCRequestException {
        try {
            PvaClientChannel channel = PvaClient.get("pva").channel(channelName);
            channel.connect(timeout);
            PVStructure response = channel.rpc(request);
            if (response == null) {
                throw new RPCRequestException(ERROR, "error executing PvaRequest");
            }
            return response;
        } catch (Exception e) {
            // Will never get here rpc() never throws
            throw new RPCRequestException(ERROR, e.getMessage());
        }
    }
}
