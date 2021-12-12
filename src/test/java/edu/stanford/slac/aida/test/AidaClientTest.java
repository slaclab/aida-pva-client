package edu.stanford.slac.aida.test;

import edu.stanford.slac.aida.client.PvaTable;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.*;
import static edu.stanford.slac.aida.client.AidaType.*;
import static edu.stanford.slac.aida.test.util.AidaClientTestUtils.abbreviate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AIDA-PVA Client.
 * These tests can only be run with a correct EPICs environment setup.
 * JDK 1.8, and EPICS_PVA_ADDR_LIST set to point to the DEV environment (mccdev)
 */
public class AidaClientTest {
    @Test
    void testSimpleGet() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().returning(INTEGER).get() - Integer");

            Integer response = (Integer) pvaRequest("XCOR:LI31:41:BCON")
                    .returning(INTEGER)
                    .get();
            assertEquals(16800, response, "Checking if response is correct");

            System.out.println("get: XCOR:LI31:41:BCON: returned: " + response);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testSimpleArrayGet() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().returning(INTEGER_ARRAY).get() - Integer Array");

            Object[] response = (Object[]) pvaRequest("XCOR:LI31:41:BCON")
                    .returning(INTEGER_ARRAY)
                    .get();

            assertArrayEquals(new Object[]{16800}, response, "Checking if array response is correct");

            System.out.println("get: XCOR:LI31:41:BCON: returned: [" + response[0] + "]");
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testGetRequestWithNoType() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaGet() - no type");

            PvaTable table = (PvaTable) pvaGet("DEV_DGRP:XCOR:BDES");
            Map<String, Object[]> tableValues = table.values;
            String[] names = (String[]) tableValues.get("name");
            Float[] secondaryValues = (Float[]) tableValues.get("secondary");

            assertEquals("XCOR:LI31:41", names[0], "Checking if table element is correct");
            assertEquals(0.0f, secondaryValues[0], "Checking if table element is correct");
            assertEquals("XCOR:LI31:201", names[1], "Checking if table element is correct");
            assertEquals(0.0f, secondaryValues[1], "Checking if table element is correct");
            assertEquals("XCOR:LI31:301", names[2], "Checking if table element is correct");
            assertEquals(0.0f, secondaryValues[2], "Checking if table element is correct");
            assertEquals("XCOR:LI31:401", names[3], "Checking if table element is correct");
            assertEquals(0.03f, secondaryValues[3], "Checking if table element is correct");

            System.out.println("pvaGet: DEV_DGRP:XCOR:BDES: returned: " + table);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testGetRequest() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaGet() - Integer");

            Integer response = (Integer) pvaGet("XCOR:LI31:41:BCON", INTEGER);
            assertEquals(16800, response, "Checking if response is correct");

            System.out.println("pvaGet: XCOR:LI31:41:BCON: returned: " + response);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testSimpleSet() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().set() - void");

            pvaRequest("XCOR:LI31:41:BCON").set(5.0);

            System.out.println("set: XCOR:LI31:41:BCON = 5.0: returned: successfully");
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testSetRequest() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaSet() - void");

            pvaSet("XCOR:LI31:41:BCON", 5.0);

            System.out.println("pvaSet: XCOR:LI31:41:BCON = 5.0: returned: successfully");
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testGetTable() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().get() - PvaTable");

            PvaTable table = (PvaTable) pvaRequest("KLYS:LI31:31:TACT")
                    .with("BEAM", 8)
                    .with("DGRP", "DEV_DGRP")
                    .returning(TABLE)
                    .get();
            assertEquals(false, table.values.get("accel")[0], "Checking if table element is correct");
            assertEquals(true, table.values.get("standby")[0], "Checking if table element is correct");
            assertEquals(false, table.values.get("bad")[0], "Checking if table element is correct");
            assertEquals(false, table.values.get("sled")[0], "Checking if table element is correct");
            assertEquals(true, table.values.get("sleded")[0], "Checking if table element is correct");
            assertEquals(false, table.values.get("pampl")[0], "Checking if table element is correct");
            assertEquals(false, table.values.get("pphas")[0], "Checking if table element is correct");

            // Check get as
            assertArrayEquals(new Boolean[] {false}, table.getAsBooleans("accel"), "Checking if getAsBooleans is correct");
            assertArrayEquals(new Boolean[] {true}, table.getAsBooleans("standby"), "Checking if getAsBooleans is correct");
            assertArrayEquals(new Boolean[] {false}, table.getAsBooleans("bad"), "Checking if getAsBooleans is correct");
            assertArrayEquals(new Boolean[] {false}, table.getAsBooleans("sled"), "Checking if getAsBooleans is correct");
            assertArrayEquals(new Boolean[] {true}, table.getAsBooleans("sleded"), "Checking if getAsBooleans is correct");
            assertArrayEquals(new Boolean[] {false}, table.getAsBooleans("pampl"), "Checking if getAsBooleans is correct");
            assertArrayEquals(new Boolean[] {false}, table.getAsBooleans("pphas"), "Checking if getAsBooleans is correct");

            System.out.println("get: KLYS:LI31:31:TACT(BEAM=8,DGRP=DEV_DGRP): returned: " + table);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testSetReturningTable() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().set() - PvaTable");

            PvaTable table = pvaRequest("KLYS:LI31:31:PDES")
                    .with("TRIM", "NO")
                    .set(90.0f);
            assertEquals(0.0f, table.values.get("PHAS")[0], "Checking if float response is correct");

            System.out.println("set: KLYS:LI31:31:PDES = 0: returned: " + table);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testGetErrors() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for get Errors");

            pvaRequest("XCOR:LI31:4100:BCON")
                    .returning(FLOAT)
                    .get();

            fail("get: XCOR:LI31:4100:BCON: should have failed");
        } catch (RPCRequestException e) {
            assertEquals("XCOR:LI31:4100:BCON(TYPE=FLOAT) : Unknown Unit requested; UnableToGetDataException; getting SLC db floating point device data", abbreviate(e.getMessage()), "Checking if error message is correct");

            System.out.println("get: XCOR:LI31:4100:BCON: failed as expected:" + abbreviate(e.getMessage()));
            System.out.println("_____________________________________________\n");
        }
    }

    @Test
    void testSetErrors() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for set Errors");

            pvaSet("XCOR:LI31:41:BCON", "FOO");

            fail("pvaSet: XCOR:LI31:41:BCON = FOO: should have failed");
        } catch (RPCRequestException e) {
            assertEquals("XCOR:LI31:41:BCON(VALUE=FOO) :AidaInternalException; can't convert argument \"FOO\" to float", abbreviate(e.getMessage()), "Checking if error message is correct");

            System.out.println("pvaSet: XCOR:LI31:41:BCON = FOO: failed as expected:" + abbreviate(e.getMessage()));
            System.out.println("_____________________________________________\n");
        }
    }
}
