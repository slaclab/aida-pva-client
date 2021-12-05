package edu.stanford.slac.aida.client.impl;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientRPC;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;

import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class PvaClientRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        try {
/*
            PvaClientRPC pvaClientRPC = PvaClient.get("pva").channel(channelName).createRPC();
            pvaClientRPC.issueConnect();
            Status status = pvaClientRPC.waitConnect();
            if (!status.isOK()) {
                throw new RPCRequestException(status.getType(), status.getMessage());
            }
            return pvaClientRPC.request(request);
*/
            return PvaClient.get("pva").channel(channelName).rpc(request);
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage());
        }

    }
}