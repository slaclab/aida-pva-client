package edu.stanford.slac.aida.client;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

/**
 * This functional interface is the similar to a Supplier except that
 * we throw RPCRequestExceptions for errors and always return PVStructures
 */
@FunctionalInterface
public interface AidaGetter<T extends PVStructure> {
    T get() throws RPCRequestException;
}

