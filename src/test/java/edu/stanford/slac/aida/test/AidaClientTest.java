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
    void test1() {
        try {
            System.out.println("Test for request().returning(INTEGER).get() - Integer");
            Integer response = (Integer) request("XCOR:LI31:41:BCON")
                    .returning(INTEGER)
                    .get();
            assertEquals(16800, response);
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
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test3() {
        try {
            System.out.println("Test for request().set() - void");
            request("XCOR:LI31:41:BCON").set(5.0);
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test4() {
        try {
            System.out.println("Test for setRequest() - void");
            setRequest("XCOR:LI31:41:BCON", 5.0);
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
        } catch (RPCRequestException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void test7() {
        try {
            System.out.println("Test for get Errors");
            request("XCOR:LI31:41:BCON")
                    .returning(TABLE)
                    .with("TABLE_TYPE", "SHORT")
                    .get();
            fail("Request XCOR:LI31:10000:BCON, TYPE=TABLE, should have failed");
        } catch (RPCRequestException e) {
            e.printStackTrace();
            assertEquals("Unspecified error executing request", e.getMessage());
        }
    }

    @Test
    void test8() {
        try {
            System.out.println("Test for set Errors");
            setRequest("XCOR:LI31:41:BCON", "FOO");
            fail("Setting XCOR:LI31:41:BCON, to \"FOO\" should have failed");
        } catch (RPCRequestException e) {
            e.printStackTrace();
            assertEquals("Unspecified error executing request", e.getMessage());
        }
    }
}
