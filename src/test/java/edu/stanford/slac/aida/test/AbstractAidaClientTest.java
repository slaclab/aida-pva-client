package edu.stanford.slac.aida.test;

import junit.framework.TestCase;
import lombok.extern.java.Log;
import org.epics.pvaccess.PVAException;
import org.epics.pvaccess.server.rpc.RPCServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static java.util.logging.Level.SEVERE;

@Log
public abstract class AbstractAidaClientTest extends TestCase {
    public static final String PROVIDER_NAME = "TEST";
    public static final String TEST1_CHANNEL_NAME = "XCOR:LI31:41:BCON";
    public static final String TEST2_CHANNEL_NAME = "DEV_DGRP:XCOR:BDES";
    public static final String TEST3_CHANNEL_NAME = "KLYS:LI31:31:TACT";
    public static final String TEST4_CHANNEL_NAME = "KLYSTRONGET:TACT";
    public static final String TEST5_CHANNEL_NAME = "KLYS:LI31:31:PDES";
    public static final String TEST6_CHANNEL_NAME = "XCOR:LI31:4100:BCON";
    public static final String TEST7_CHANNEL_NAME = "NDRFACET:BUFFACQ";

    private static RPCServer server;
    private static Thread serverThread;

    public static String getChannel(String channelName) {
        return channelName.substring(PROVIDER_NAME.length() + 2);
    }

    /**
     * Start the Test AIDA service for tests
     */
    @BeforeClass
    public static void startServer() {
        log.info("Starting Test RPC Server ...");
        server = new RPCServer();
        AidaTestService testService = new AidaTestService();
        server.registerService(PROVIDER_NAME + "::*", testService);
        server.printInfo();
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.run(0);
                } catch (PVAException e) {
                    log.log(SEVERE, "Error starting Server: {}", e.getMessage());
                }
            }
        });
        serverThread.start();
    }

    /**
     * Stop the Test AIDA service after tests
     */
    @AfterClass
    public static void shutdownServer() {
        if (server != null) {
            try {
                server.destroy();
                if (serverThread != null) {
                    serverThread.interrupt();
                }
                server = null;
                log.info("Stopped Test RPC Server");
            } catch (PVAException ignored) {
            }
        }
    }
}
