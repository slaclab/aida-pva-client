package edu.stanford.slac.aida.client;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

/**
 * This functional interface is the similar to a Supplier except that
 * we throw RPCRequestExceptions for errors and always return PVStructures
 */
public abstract class AidaRequest<T extends PVStructure> {
    public abstract T execute() throws RPCRequestException;
}

