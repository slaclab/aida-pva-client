package edu.stanford.slac.aida.test;

import edu.stanford.slac.aida.client.AidaTable;
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
    void test01() {
        try {
            System.out.println("Test for request().returning(INTEGER).get() - Integer");
            Integer response = (Integer) request("XCOR:LI31:41:BCON")
                    .returning(INTEGER)
                    .get();
            assertEquals(16800, response);
            System.out.println("get: XCOR:LI31:41:BCON: returned: " + response);
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    void test02() {
        try {
            System.out.println("Test for request().returning(INTEGER).get() - Integer");
            Integer response = (Integer) request("XCOR:LI31:41:BCON")
                    .returning(INTEGER)
                    .get();
            assertEquals(16800, response);
            System.out.println("get: XCOR:LI31:41:BCON: returned: " + response);
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test2() {
        try {
            System.out.println("Test for getRequest() - Integer");
            Integer response = (Integer) getRequest("XCOR:LI31:41:BCON", INTEGER);
            assertEquals(16800, response);
            System.out.println("getRequest: XCOR:LI31:41:BCON: returned: " + response);
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test3() {
        try {
            System.out.println("Test for request().set() - void");
            request("XCOR:LI31:41:BCON").set(5.0);
            System.out.println("set: XCOR:LI31:41:BCON = 5.0: returned: successfully");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test4() {
        try {
            System.out.println("Test for setRequest() - void");
            setRequest("XCOR:LI31:41:BCON", 5.0);
            System.out.println("setRequest: XCOR:LI31:41:BCON = 5.0: returned: successfully");
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test5() {
        try {
            System.out.println("Test for request().get() - AidaTable");
            AidaTable table = (AidaTable) request("KLYS:LI31:31:TACT")
                    .with("BEAM", 8)
                    .with("DGRP", "DEV_DGRP")
                    .returning(TABLE)
                    .get();
            assertEquals(false, table.getValues().get("accel").get(0));
            assertEquals(true, table.getValues().get("standby").get(0));
            assertEquals(false, table.getValues().get("bad").get(0));
            assertEquals(false, table.getValues().get("sled").get(0));
            assertEquals(true, table.getValues().get("sleded").get(0));
            assertEquals(false, table.getValues().get("pampl").get(0));
            assertEquals(false, table.getValues().get("pphas").get(0));
            System.out.println("get: KLYS:LI31:31:TACT(BEAM=8,DGRP=DEV_DGRP): returned: " + table);
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test6() {
        try {
            System.out.println("Test for request().setReturningTable() - AidaTable");
            AidaTable table = request("KLYS:LI31:31:PDES")
                    .with("TRIM", "NO")
                    .setReturningTable(90.0f);
            assertEquals(0.0f, table.getValues().get("PHAS").get(0));
            System.out.println("set: KLYS:LI31:31:PDES = 0: returned: " + table);
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test7() {
        try {
            System.out.println("Test for get Errors");
            request("XCOR:LI31:4100:BCON")
                    .returning(FLOAT)
                    .get();
            fail("get: XCOR:LI31:4100:BCON: should have failed");
        } catch (RPCRequestException e) {
            assertEquals(" Unknown Unit requested; UnableToGetDataException; getting SLC db floating point device data", abbreviate(e.getMessage()));
            System.out.println("get: XCOR:LI31:4100:BCON: failed as expected:" + abbreviate(e.getMessage()) );
        }
    }

    @Test
    void test8() {
        try {
            System.out.println("Test for set Errors");
            setRequest("XCOR:LI31:41:BCON", "FOO");
            fail("setRequest: XCOR:LI31:41:BCON = FOO: should have failed");
        } catch (RPCRequestException e) {
            assertEquals("AidaInternalException; can't convert argument \"FOO\" to float", abbreviate(e.getMessage()));
            System.out.println("setRequest: XCOR:LI31:41:BCON = FOO: failed as expected:" + abbreviate(e.getMessage()) );
        }
    }

    private String abbreviate(String message) {
        int end = message.indexOf(".");
        int endC = message.indexOf(", cause:");
        if ( end == -1 ) {
            return message;
        }
        if ( endC == -1 ) {
            endC = end;
        }
        return message.substring(0,Math.min(end, endC));
    }
}
