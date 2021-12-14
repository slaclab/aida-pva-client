package edu.stanford.slac.aida.test;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;
import org.junit.Test;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.pvaRequest;
import static junit.framework.TestCase.*;

/**
 * Tests for the AIDA-PVA Client.
 * These tests can only be run with a correct EPICs environment setup.
 * JDK 1.8, and EPICS_PVA_ADDR_LIST set to point to the DEV environment (mccdev)
 */
public class AidaUriTest {
    @Test
    public void testSimpleUri() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for no parameter URI");

            PVStructure uri = pvaRequest("channel").uri();
            assertTrue("Checking if uri has valid structure", uri.checkValid());
            assertEquals("Checking Structure ID", "epics:nt/NTURI:1.0", uri.getStructure().getID());

            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }
}
