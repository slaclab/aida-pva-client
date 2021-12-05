package edu.stanford.slac.aida.client.impl;

//import org.epics.pvaccess.easyPVA.EasyChannel;
//import org.epics.pvaccess.easyPVA.EasyPVA;
//import org.epics.pvaccess.easyPVA.EasyPVAFactory;
//import org.epics.pvaccess.easyPVA.EasyRPC;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class EasyPvaRequestExecutor {
    public static PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException {
        try {
/*
            EasyPVA easyPVA = EasyPVAFactory.get();
            EasyChannel channel = easyPVA.createChannel(channelName);
            EasyRPC rpc = channel.createRPC();
            if (rpc.connect())
                return rpc.request(request);
            else
*/
                return null;
        } catch (Exception e) {
            throw new RPCRequestException(ERROR, e.getMessage());
        }
    }
}
