package edu.stanford.slac.aida.test;

import edu.stanford.slac.aida.client.AidaPvaRequest;
import edu.stanford.slac.aida.client.PvaTable;
import edu.stanford.slac.aida.client.compat.AidaConsumer;
import lombok.extern.java.Log;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.*;
import static edu.stanford.slac.aida.client.AidaType.*;
import static edu.stanford.slac.aida.test.AidaClientTestUtils.abbreviate;
import static org.junit.Assert.assertArrayEquals;

/**
 * Tests for the AIDA-PVA Client.
 * A Test Server is started to respond to these requests
 */
@RunWith(JUnit4.class)
@Log
public class AidaClientTest extends AbstractAidaClientTest {
    @Test
    public void testAsyncGet() {
        log.info("#############################################");

        log.info("Test synchronous call without setting timeout");
        try {
            pvaRequest("TEST::NDRFACET:BUFFACQ")
                    .with("BPMD", 57)
                    .with("NRPOS", 10)
                    .with("BPMS", Arrays.asList("BPMS:LI02:501", "BPMS:DR12:334"))
                    .get();
            fail("TEST::NDRFACET:BUFFACQ: should have failed without extending timeout");
        } catch (RPCRequestException e) {
            // Expect to fail without timeout() or callback
            assertEquals("Checking if error message is correct", "TEST::NDRFACET:BUFFACQ(NRPOS=10, BPMD=57, BPMS=[BPMS:LI02:501, BPMS:DR12:334]) :timeout", abbreviate(e.getMessage()));
            log.info("TEST::NDRFACET:BUFFACQ: generates a timeout as expected when run synchronously");
        }

        log.info("Test asynchronous calls using callback");
        final CountDownLatch complete = new CountDownLatch(1);
        pvaRequest("TEST::NDRFACET:BUFFACQ")
                .with("BPMD", 57)
                .with("NRPOS", 10)
                .with("BPMS", Arrays.asList("BPMS:LI02:501", "BPMS:DR12:334"))
                .setResponseCallback(new AidaConsumer<Object>() {
                    @Override
                    public void accept(Object response) {
                        PvaTable table = (PvaTable) response;
                        log.info("asyncGet: TEST::NDRFACET:BUFFACQ(BPMD=57,NRPOS=10,BPMS=BPMS:LI02:501,BPMS:DR12:334): returned: " + table);
                        assertEquals("Checking if table element is correct", "BPMS:LI11:501", table.values.get("name")[4]);
                        assertEquals("Checking if table element is correct", 71313, table.values.get("pulseId")[2]);
                        assertEquals("Checking if table element is correct", -0.9565048f, table.values.get("x")[3]);
                        assertEquals("Checking if table element is correct", 0.81614506f, table.values.get("y")[0]);
                        assertEquals("Checking if table element is correct", 1.0E-10, table.values.get("tmits")[5]);
                        assertEquals("Checking if table element is correct", 1, table.values.get("stat")[1]);
                        assertEquals("Checking if table element is correct", true, table.values.get("goodmeas")[1]);
                        complete.countDown();
                    }
                })
                .asyncGet();

        // Wait until seconds to be sure test has completed
        try {
            if (complete.await(15, TimeUnit.SECONDS)) {
                log.info("Asynchronous callback has completed");
            } else {
                fail("Asynchronous call has failed to complete");
            }
        } catch (InterruptedException ignored) {
        }

        log.info("Test asynchronous calls without callback");
        AidaPvaRequest asynchRequest = pvaRequest("TEST::NDRFACET:BUFFACQ")
                .with("BPMD", 57)
                .with("NRPOS", 10)
                .with("BPMS", Arrays.asList("BPMS:LI02:501", "BPMS:DR12:334"))
                .asyncGet();

        // Wait until seconds to be sure test has completed
        assertTrue("Request should be running", asynchRequest.isRunning());
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ignored) {
        }

        assertFalse("Request should not still be running", asynchRequest.isRunning());
        assertTrue("Request has not been executed", asynchRequest.isReady());
        PvaTable table = (PvaTable) asynchRequest.getResponse();
        log.info("asyncGet: TEST::NDRFACET:BUFFACQ(BPMD=57,NRPOS=10,BPMS=BPMS:LI02:501,BPMS:DR12:334): returned: " + table);
        assertEquals("Checking if table element is correct", "BPMS:LI11:501", table.values.get("name")[4]);
        assertEquals("Checking if table element is correct", 71313, table.values.get("pulseId")[2]);
        assertEquals("Checking if table element is correct", -0.9565048f, table.values.get("x")[3]);
        assertEquals("Checking if table element is correct", 0.81614506f, table.values.get("y")[0]);
        assertEquals("Checking if table element is correct", 1.0E-10, table.values.get("tmits")[5]);
        assertEquals("Checking if table element is correct", 1, table.values.get("stat")[1]);
        assertEquals("Checking if table element is correct", true, table.values.get("goodmeas")[1]);

        log.info("_____________________________________________\n");
    }

    @Test
    public void testGetErrors() {
        try {
            log.info("#############################################");
            log.info("Test for get Errors");

            pvaRequest("TEST::XCOR:LI31:4100:BCON")
                    .returning(AIDA_FLOAT)
                    .get();

            fail("get: TEST::XCOR:LI31:4100:BCON: should have failed");
        } catch (RPCRequestException e) {
            assertEquals("Checking if error message is correct", "TEST::XCOR:LI31:4100:BCON(TYPE=FLOAT) : Unknown Unit requested; UnableToGetDataException; getting SLC db floating point device data", abbreviate(e.getMessage()));

            log.info("get: TEST::XCOR:LI31:4100:BCON: failed as expected:" + abbreviate(e.getMessage()));
            log.info("_____________________________________________\n");
        }
    }

    @Test
    public void testGetMultiStatus() {
        try {
            log.info("#############################################");
            log.info("Test for pvaRequest().get() - klystron multi-status");

            PvaTable table = (PvaTable) pvaRequest("TEST::KLYSTRONGET:TACT")
                    .with("DEVICES", Arrays.asList("KLYS:LI31:31", "KLYS:LI31:31"))
                    .with("BEAM", 8)
                    .with("DGRP", "DEV_DGRP")
                    .get();
            log.info("get: TEST::KLYSTRONGET:TACT(BEAM=8,DEVICES=KLYS:LI31:31,KLYS:LI31:31,DGRP=DEV_DGRP): returned: " + table);
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

            log.info("get: TEST::KLYSTRONGET:TACT(BEAM=8,DEVICES=KLYS:LI31:31,KLYS:LI31:31,DGRP=DEV_DGRP): returned: " + table);
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetRequest() {
        try {
            log.info("#############################################");
            log.info("Test for pvaGet() - Integer");

            int response = (Integer) pvaGet("TEST::XCOR:LI31:41:BCON", AIDA_INTEGER);
            assertEquals("Checking if response is correct", 16800, response);

            log.info("pvaGet: TEST::XCOR:LI31:41:BCON: returned: " + response);
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetRequestWithNoType() {
        try {
            log.info("#############################################");
            log.info("Test for pvaGet() - no type");

            PvaTable table = (PvaTable) pvaGet("TEST::DEV_DGRP:XCOR:BDES");
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

            log.info("pvaGet: TEST::DEV_DGRP:XCOR:BDES: returned: " + table);
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetTable() {
        try {
            log.info("#############################################");
            log.info("Test for pvaRequest().get() - PvaTable");

            PvaTable table = (PvaTable) pvaRequest("TEST::KLYS:LI31:31:TACT")
                    .with("BEAM", 8)
                    .with("DGRP", "DEV_DGRP")
                    .timeout(10.0)
                    .returning(AIDA_TABLE)
                    .get();
            log.info("get: TEST::KLYS:LI31:31:TACT(BEAM=8,DGRP=DEV_DGRP): returned: " + table);

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

            log.info("get: TEST::KLYS:LI31:31:TACT(BEAM=8,DGRP=DEV_DGRP): returned: " + table);
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetErrors() {
        try {
            log.info("#############################################");
            log.info("Test for set Errors");

            pvaSet("TEST::XCOR:LI31:41:BCON", "FOO");

            fail("pvaSet: TEST::XCOR:LI31:41:BCON = FOO: should have failed");
        } catch (RPCRequestException e) {
            assertEquals("Checking if error message is correct", "TEST::XCOR:LI31:41:BCON(VALUE=FOO) :AidaInternalException; can't convert argument \"FOO\" to float", abbreviate(e.getMessage()));

            log.info("pvaSet: TEST::XCOR:LI31:41:BCON = FOO: failed as expected:" + abbreviate(e.getMessage()));
            log.info("_____________________________________________\n");
        }
    }

    @Test
    public void testSetRequest() {
        try {
            log.info("#############################################");
            log.info("Test for pvaSet() - void");

            pvaSet("TEST::XCOR:LI31:41:BCON", 5.0);

            log.info("pvaSet: TEST::XCOR:LI31:41:BCON = 5.0: returned: successfully");
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSetReturningTable() {
        try {
            log.info("#############################################");
            log.info("Test for pvaRequest().set() - PvaTable");

            PvaTable table = pvaRequest("TEST::KLYS:LI31:31:PDES")
                    .with("TRIM", "NO")
                    .set(90.0f);
            assertEquals("Checking if float response is correct", 0.0f, table.values.get("PHAS")[0]);

            log.info("set: TEST::KLYS:LI31:31:PDES = 0: returned: " + table);
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleArrayGet() {
        try {
            log.info("#############################################");
            log.info("Test for pvaRequest().returning(AIDA_INTEGER_ARRAY).get() - Integer Array");

            Object[] response = (Object[]) pvaRequest("TEST::XCOR:LI31:41:BCON")
                    .returning(AIDA_INTEGER_ARRAY)
                    .get();

            assertArrayEquals("Checking if array response is correct", new Object[]{16800}, response);

            log.info("get: TEST::XCOR:LI31:41:BCON: returned: [" + response[0] + "]");
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleGet() {
        try {
            log.info("#############################################");
            log.info("Test for pvaRequest().returning(AIDA_INTEGER).get() - Integer");

            int response = (Integer) pvaRequest("TEST::XCOR:LI31:41:BCON")
                    .returning(AIDA_INTEGER)
                    .get();
            assertEquals("Checking if response is correct", 16800, response);

            log.info("get: TEST::XCOR:LI31:41:BCON: returned: " + response);
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleSet() {
        try {
            log.info("#############################################");
            log.info("Test for pvaRequest().set() - void");

            pvaRequest("TEST::XCOR:LI31:41:BCON").set(5.0);

            log.info("set: TEST::XCOR:LI31:41:BCON = 5.0: returned: successfully");
            log.info("_____________________________________________\n");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }


}
