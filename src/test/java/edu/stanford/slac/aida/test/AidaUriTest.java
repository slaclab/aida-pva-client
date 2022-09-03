package edu.stanford.slac.aida.test;

import junit.framework.TestCase;
import lombok.extern.java.Log;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.pvaRequest;

/**
 * Tests for the AIDA-PVA Client.
 * These tests can only be run with a correct EPICs environment setup.
 * JDK 1.8, and EPICS_PVA_ADDR_LIST set to point to the DEV environment (mccdev)
 */
@RunWith(JUnit4.class)
@Log
public class AidaUriTest extends TestCase {
    @Test
    public void testSimpleUri() {
        try {
            log.info("#############################################");
            log.info("Test for no parameter URI");

            PVStructure uri = pvaRequest("channel").uri();
            assertTrue("Checking if uri has valid structure", uri.checkValid());
            assertEquals("Checking Structure ID", "epics:nt/NTURI:1.0", uri.getStructure().getID());

            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }
}
