package edu.stanford.slac.aida.test;

import com.sun.tools.javac.util.List;
import edu.stanford.slac.aida.client.PvaTable;
import junit.framework.TestCase;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Map;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.*;
import static edu.stanford.slac.aida.client.AidaType.*;
import static edu.stanford.slac.aida.test.util.AidaClientTestUtils.abbreviate;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tests for the AIDA-PVA Client.
 * These tests can only be run with a correct EPICs environment setup.
 * JDK 1.8, and EPICS_PVA_ADDR_LIST set to point to the DEV environment (mccdev)
 */
@RunWith(JUnit4.class)
public class AidaClientTest extends TestCase {
    @Test
    public void testSimpleGet() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().returning(AIDA_INTEGER).get() - Integer");

            int response = (Integer) pvaRequest("XCOR:LI31:41:BCON")
                    .returning(AIDA_INTEGER)
                    .get();
            assertEquals("Checking if response is correct", 16800, response);

            System.out.println("get: XCOR:LI31:41:BCON: returned: " + response);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleArrayGet() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().returning(AIDA_INTEGER_ARRAY).get() - Integer Array");

            Object[] response = (Object[]) pvaRequest("XCOR:LI31:41:BCON")
                    .returning(AIDA_INTEGER_ARRAY)
                    .get();

            assertArrayEquals("Checking if array response is correct", new Object[]{16800}, response);

            System.out.println("get: XCOR:LI31:41:BCON: returned: [" + response[0] + "]");
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetRequestWithNoType() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaGet() - no type");

            PvaTable table = (PvaTable) pvaGet("DEV_DGRP:XCOR:BDES");
            Map<String, Object[]> tableValues = table.values;
            Object[] names = tableValues.get("name");
            Object[] secondaryValues = tableValues.get("secondary");

            assertEquals("Checking if table element is correct", "XCOR:LI31:41", names[0]);
            assertEquals("Checking if table element is correct", 4.0f, secondaryValues[0]);
            assertEquals("Checking if table element is correct", "XCOR:LI31:201", names[1]);
            assertEquals("Checking if table element is correct", 0.0f, secondaryValues[1]);
            assertEquals("Checking if table element is correct", "XCOR:LI31:301", names[2]);
            assertEquals("Checking if table element is correct", 0.0f, secondaryValues[2]);
            assertEquals("Checking if table element is correct", "XCOR:LI31:401", names[3]);
            assertEquals("Checking if table element is correct", 0.03f, secondaryValues[3]);

            System.out.println("pvaGet: DEV_DGRP:XCOR:BDES: returned: " + table);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetRequest() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaGet() - Integer");

            int response = (Integer) pvaGet("XCOR:LI31:41:BCON", AIDA_INTEGER);
            assertEquals("Checking if response is correct", 16800, response);

            System.out.println("pvaGet: XCOR:LI31:41:BCON: returned: " + response);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleSet() {
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
    public void testSetRequest() {
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
    public void testGetTable() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().get() - PvaTable");

            PvaTable table = (PvaTable) pvaRequest("KLYS:LI31:31:TACT")
                    .with("BEAM", 8)
                    .with("DGRP", "DEV_DGRP")
                    .timeout(10.0)
                    .returning(AIDA_TABLE)
                    .get();
            System.out.println("get: KLYS:LI31:31:TACT(BEAM=8,DGRP=DEV_DGRP): returned: " + table);

            assertEquals("Checking if table element is correct", "KLYS:LI31:31", table.values.get("name")[0]);
            assertEquals("Checking if table element is correct", true, table.values.get("opstat")[0]);
            assertEquals("Checking if table element is correct", (short) 18, table.values.get("status")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("accel")[0]);
            assertEquals("Checking if table element is correct", true, table.values.get("standby")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("bad")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("sled")[0]);
            assertEquals("Checking if table element is correct", true, table.values.get("sleded")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("pampl")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("pphas")[0]);

            // Check get directly on table
            assertArrayEquals("Checking if get is correct", new String[]{"KLYS:LI31:31"}, table.get("name"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{true}, table.get("opstat"));
            assertArrayEquals("Checking if get is correct", new Short[]{18}, table.get("status"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false}, table.get("accel"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{true}, table.get("standby"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false}, table.get("bad"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false}, table.get("sled"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{true}, table.get("sleded"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false}, table.get("pampl"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false}, table.get("pphas"));

            System.out.println("get: KLYS:LI31:31:TACT(BEAM=8,DGRP=DEV_DGRP): returned: " + table);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetMultiStatus() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().get() - klystron multi-status");

            PvaTable table = (PvaTable) pvaRequest("KLYSTRONGET:TACT")
                    .with("DEVICES", List.of("KLYS:LI31:31", "KLYS:LI31:31"))
                    .with("BEAM", 8)
                    .with("DGRP", "DEV_DGRP")
                    .get();
            System.out.println("get: KLYSTRONGET:TACT(BEAM=8,DEVICES=KLYS:LI31:31,KLYS:LI31:31,DGRP=DEV_DGRP): returned: " + table);
            assertEquals("Checking if table element is correct", "KLYS:LI31:31", table.values.get("name")[0]);
            assertEquals("Checking if table element is correct", true, table.values.get("opstat")[0]);
            assertEquals("Checking if table element is correct", (short) 18, table.values.get("status")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("accel")[0]);
            assertEquals("Checking if table element is correct", true, table.values.get("standby")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("bad")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("sled")[0]);
            assertEquals("Checking if table element is correct", true, table.values.get("sleded")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("pampl")[0]);
            assertEquals("Checking if table element is correct", false, table.values.get("pphas")[0]);

            assertEquals("Checking if table element is correct", "KLYS:LI31:31", table.values.get("name")[1]);
            assertEquals("Checking if table element is correct", true, table.values.get("opstat")[1]);
            assertEquals("Checking if table element is correct", (short) 18, table.values.get("status")[1]);
            assertEquals("Checking if table element is correct", false, table.values.get("accel")[1]);
            assertEquals("Checking if table element is correct", true, table.values.get("standby")[1]);
            assertEquals("Checking if table element is correct", false, table.values.get("bad")[1]);
            assertEquals("Checking if table element is correct", false, table.values.get("sled")[1]);
            assertEquals("Checking if table element is correct", true, table.values.get("sleded")[1]);
            assertEquals("Checking if table element is correct", false, table.values.get("pampl")[1]);
            assertEquals("Checking if table element is correct", false, table.values.get("pphas")[1]);

            // Check get directly on table
            assertArrayEquals("Checking if get is correct", new String[]{"KLYS:LI31:31", "KLYS:LI31:31"}, table.get("name"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{true, true}, table.get("opstat"));
            assertArrayEquals("Checking if get is correct", new Short[]{18, 18}, table.get("status"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false, false}, table.get("accel"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{true, true}, table.get("standby"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false, false}, table.get("bad"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false, false}, table.get("sled"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{true, true}, table.get("sleded"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false, false}, table.get("pampl"));
            assertArrayEquals("Checking if get is correct", new Boolean[]{false, false}, table.get("pphas"));

            System.out.println("get: KLYSTRONGET:TACT(BEAM=8,DEVICES=KLYS:LI31:31,KLYS:LI31:31,DGRP=DEV_DGRP): returned: " + table);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetReturningTable() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for pvaRequest().set() - PvaTable");

            PvaTable table = pvaRequest("KLYS:LI31:31:PDES")
                    .with("TRIM", "NO")
                    .set(90.0f);
            assertEquals("Checking if float response is correct", 0.0f, table.values.get("PHAS")[0]);

            System.out.println("set: KLYS:LI31:31:PDES = 0: returned: " + table);
            System.out.println("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetErrors() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for get Errors");

            pvaRequest("XCOR:LI31:4100:BCON")
                    .returning(AIDA_FLOAT)
                    .get();

            fail("get: XCOR:LI31:4100:BCON: should have failed");
        } catch (RPCRequestException e) {
            assertEquals("Checking if error message is correct", "XCOR:LI31:4100:BCON(TYPE=FLOAT) : Unknown Unit requested; UnableToGetDataException; getting SLC db floating point device data", abbreviate(e.getMessage()));

            System.out.println("get: XCOR:LI31:4100:BCON: failed as expected:" + abbreviate(e.getMessage()));
            System.out.println("_____________________________________________\n");
        }
    }

    @Test
    public void testSetErrors() {
        try {
            System.out.println("#############################################");
            System.out.println("Test for set Errors");

            pvaSet("XCOR:LI31:41:BCON", "FOO");

            fail("pvaSet: XCOR:LI31:41:BCON = FOO: should have failed");
        } catch (RPCRequestException e) {
            assertEquals("Checking if error message is correct", "XCOR:LI31:41:BCON(VALUE=FOO) :AidaInternalException; can't convert argument \"FOO\" to float", abbreviate(e.getMessage()));

            System.out.println("pvaSet: XCOR:LI31:41:BCON = FOO: failed as expected:" + abbreviate(e.getMessage()));
            System.out.println("_____________________________________________\n");
        }
    }
}
