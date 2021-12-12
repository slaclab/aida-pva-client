package edu.stanford.slac.aida.test;

import edu.stanford.slac.aida.client.PvaTable;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.*;
import static edu.stanford.slac.aida.client.AidaType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AIDA-PVA Client.
 * These tests can only be run with a correct EPICs environment setup.
 * JDK 1.8, and EPICS_PVA_ADDR_LIST set to point to the DEV environment (mccdev)
 */
public class AidaUriTest {
    @Test
    void testSimpleUri() {
        System.out.println("#############################################");
        System.out.println("Test for no parameter URI");

        PVStructure uri = pvaRequest("channel")
                .uri();
        assertTrue(uri.checkValid(), "Checking if uri has valid structure");
        assertEquals("epics:nt/NTURI:1.0", uri.getStructure().getID(), "Checking Structure ID");

        System.out.println("_____________________________________________\n");
    }
}
