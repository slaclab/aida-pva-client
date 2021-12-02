package edu.stanford.slac.aida.test;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.junit.jupiter.api.Test;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.*;
import static edu.stanford.slac.aida.client.AidaType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the AIDA-PVA Client.
 * These tests can only be run with a correct EPICs environment setup.
 * JDK 1.8, and EPICS_PVA_ADDR_LIST set to point to the DEV environment (mccdev)
 */
public class AidaClientTest {
    @Test
    void test1() {
        assertEquals(1, 1);
        try {
            Integer response = (Integer) request("XCOR:LI31:41:BCON")
                    .returning(INTEGER)
                    .get();
            assertEquals(16800, response);
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }
}
