package edu.stanford.slac.aida.client;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

@FunctionalInterface
public interface PvaRequestExecutor {
    PVStructure executeRequest(String channelName, PVStructure request) throws RPCRequestException;
}
