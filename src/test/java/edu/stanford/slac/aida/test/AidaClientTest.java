package edu.stanford.slac.aida.test;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.junit.jupiter.api.Test;

import static edu.stanford.slac.aida.client.AidaPvaClientUtils.request;
import static edu.stanford.slac.aida.client.AidaType.INTEGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
