package edu.stanford.slac.aida.test;

import edu.stanford.slac.aida.client.AidaType;
import org.epics.nt.NTScalar;
import org.epics.nt.NTURI;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvaccess.server.rpc.RPCService;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;

import static edu.stanford.slac.aida.client.AidaType.AIDA_INTEGER;
import static edu.stanford.slac.aida.client.AidaType.AIDA_INTEGER_ARRAY;
import static edu.stanford.slac.aida.test.AbstractAidaClientTest.*;
import static org.epics.pvdata.pv.ScalarType.*;
import static org.epics.pvdata.pv.Status.StatusType.ERROR;

public class AidaTestService extends AbstractTestService implements RPCService {
    /**
     * Return responses for requests for channel 1
     *
     * @param aidaType the parsed type parameter
     * @param value    string representation of the value parameter
     * @return the PVStructure containing the test results
     * @throws RPCRequestException if the test requires an exception be thrown or there is some other error
     */
    private static PVStructure channel1Responses(AidaType aidaType, String value) throws RPCRequestException {
        PVStructure retVal = null;

        if (value == null) {
            if (aidaType == AIDA_INTEGER) {
                retVal = integerResponse(16800);
            } else if (aidaType == AIDA_INTEGER_ARRAY) {
                retVal = integerArrayResponse(16800);
            }
        } else {
            try {
                Float.valueOf(value); // Only check that value is a valid float
                retVal = NTScalar.createBuilder().value(pvString).createPVStructure();
            } catch (NumberFormatException e) {
                throw new RPCRequestException(ERROR, "AidaInternalException; can't convert argument \"" + value + "\" to float");
            }
        }

        return retVal;
    }

    /**
     * Return responses for requests for channel 2
     *
     * @return the PVStructure containing the test results
     */
    private static PVStructure channel2Responses() {
        String[] fieldNames = {"name", "secondary"};
        ScalarType[] types = {pvString, pvFloat};
        String[] names = {"XCOR:LI31:41", "XCOR:LI31:201", "XCOR:LI31:301", "XCOR:LI31:401"};
        float[] secondaries = {4.0f, 0.0f, 0.0f, 0.03f};
        return tableResponse(fieldNames, types, names, secondaries);
    }

    /**
     * Return responses for requests for channel 3
     *
     * @return the PVStructure containing the test results
     */
    private static PVStructure channel3Responses() {
        String[] fieldNames = {"name", "opstat", "status", "accel", "standby", "bad", "sled", "sleded", "pampl", "pphas"};
        ScalarType[] types = {pvString, pvBoolean, pvShort, pvBoolean, pvBoolean, pvBoolean, pvBoolean, pvBoolean, pvBoolean, pvBoolean};

        String[] names = {"KLYS:LI31:31"};
        boolean[] opstats = {true};
        short[] statuses = {18};
        boolean[] accels = {false};
        boolean[] standbys = {true};
        boolean[] bads = {false};
        boolean[] sleds = {false};
        boolean[] slededs = {true};
        boolean[] pampls = {false};
        boolean[] pphases = {false};
        return tableResponse(fieldNames, types, names, opstats, statuses, accels, standbys, bads, sleds, slededs, pampls, pphases);
    }

    /**
     * Return responses for requests for channel 4
     *
     * @return the PVStructure containing the test results
     */
    private static PVStructure channel4Responses() {
        String[] fieldNames = {"name", "opstat", "status", "accel", "standby", "bad", "sled", "sleded", "pampl", "pphas"};
        ScalarType[] types = {pvString, pvBoolean, pvShort, pvBoolean, pvBoolean, pvBoolean, pvBoolean, pvBoolean, pvBoolean, pvBoolean};

        String[] names = {"KLYS:LI31:31", "KLYS:LI31:31"};
        boolean[] opstats = {true, true};
        short[] statuses = {18, 18};
        boolean[] accels = {false, false};
        boolean[] standbys = {true, true};
        boolean[] bads = {false, false};
        boolean[] sleds = {false, false};
        boolean[] slededs = {true, true};
        boolean[] pampls = {false, false};
        boolean[] pphases = {false, false};
        return tableResponse(fieldNames, types, names, opstats, statuses, accels, standbys, bads, sleds, slededs, pampls, pphases);
    }

    /**
     * Return responses for requests for channel 5
     *
     * @return the PVStructure containing the test results
     */
    private static PVStructure channel5Responses() {
        String[] fieldNames = {"PHAS"};
        ScalarType[] types = {pvFloat};

        float[] phases = {0.0f};
        return tableResponse(fieldNames, types, phases);
    }

    /**
     * Return responses for requests for channel 7
     *
     * @return the PVStructure containing the test results
     */
    private static PVStructure channel7Responses() {
        String[] fieldNames = {"name", "pulseId", "x", "y", "tmits", "stat", "goodmeas"};
        ScalarType[] types = {pvString, pvInt, pvFloat, pvFloat, pvDouble, pvInt, pvBoolean};

        try {
            // Simulate 5 second acquisition time (more than 3 second default timeout)
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }

        String[] names = {"BPMS:LI11:501", "BPMS:LI11:501", "BPMS:LI11:501", "BPMS:LI11:501", "BPMS:LI11:501", "BPMS:LI11:501"};
        int[] pulseIds = {71311, 71312, 71313, 71314, 71315, 71316};
        float[] x = {0.45966175f, -0.9996438f, -0.28856283f, -0.9565048f, 0.5896404f, -0.9961865f};
        float[] y = {0.81614506f, -0.9657358f, 0.69943863f, -0.91913205f, -0.47585112f, -0.58156544f,};
        double[] tmits = {1.0e-10, 1.0E-10, 1.0E-10, 1.0E-10, 1.0E-10, 1.0E-10};
        int[] statuses = {1, 1, 1, 1, 1, 1};
        boolean[] goodmeasures = {true, true, true, true, true, true};
        return tableResponse(fieldNames, types, names, pulseIds, x, y, tmits, statuses, goodmeasures);
    }

    /**
     * All requests to this test service enter here.  We determine the channel name being called, and get
     * basic parameter information (not a full parse) to determine VALUE and TYPE arguments
     *
     * @param pvUri RPC request arguments.
     * @return a PVStructure containing the test result depending on channel name and parameters
     * @throws RPCRequestException if an error is supposed to be thrown or there are errors in parameter or channel name
     */
    @Override
    public PVStructure request(PVStructure pvUri) throws RPCRequestException {
        // Check that the parameter is always a normative type
        String type = pvUri.getStructure().getID();
        if (!NTURI.is_a(pvUri.getStructure())) {
            String msg = "Unable to get data, unexpected request type: " + type;
            throw new RPCRequestException(ERROR, msg);
        }

        // Retrieve the PV name
        PVString pvPathField = pvUri.getStringField("path");
        if (pvPathField == null) {
            throw new RPCRequestException(ERROR, "unable to determine the channel from the request specified: " + pvUri);
        }
        String channelName = pvPathField.get();
        if (channelName == null || channelName.length() == 0) {
            throw new RPCRequestException(ERROR, "unable to determine the channel from the request specified: <blank>");
        }

        channelName = getChannel(channelName);
        PVStructure pvUriQuery = pvUri.getStructureField("query");

        // Get special arguments `TYPE` and `VALUE`
        AidaType aidaType = null;
        String value = null;

        if (pvUriQuery != null) {
            PVField[] pvFields = pvUriQuery.getPVFields();
            for (PVField field : pvFields) {
                String argumentName = field.getFieldName();
                String argumentValue = fieldToString(field);
                if ("VALUE".equalsIgnoreCase(argumentName)) {
                    value = argumentValue;
                } else if ("TYPE".equalsIgnoreCase(argumentName)) {
                    aidaType = AidaType.valueOf("AIDA_" + argumentValue);
                }
            }
        }

        // Depending on the channel name return appropriate responses
        if (TEST1_CHANNEL_NAME.equals(channelName)) {
            return channel1Responses(aidaType, value);
        } else if (TEST2_CHANNEL_NAME.equals(channelName)) {
            return channel2Responses();
        } else if (TEST3_CHANNEL_NAME.equals(channelName)) {
            return channel3Responses();
        } else if (TEST4_CHANNEL_NAME.equals(channelName)) {
            return channel4Responses();
        } else if (TEST5_CHANNEL_NAME.equals(channelName)) {
            return channel5Responses();
        } else if (TEST6_CHANNEL_NAME.equals(channelName)) {
            throw new RPCRequestException(ERROR, " Unknown Unit requested; UnableToGetDataException; getting SLC db floating point device data");
        } else if (TEST7_CHANNEL_NAME.equals(channelName)) {
            return channel7Responses();
        }
        throw new RPCRequestException(ERROR, "Unsupported channel: " + channelName);
    }
}
