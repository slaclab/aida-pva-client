package edu.stanford.slac.aida.test;

import org.epics.nt.NTScalar;
import org.epics.nt.NTScalarArray;
import org.epics.nt.NTTable;
import org.epics.nt.NTTableBuilder;
import org.epics.pvdata.pv.*;

import static edu.stanford.slac.aida.client.AidaType.NT_FIELD_NAME;
import static org.epics.pvdata.pv.ScalarType.pvInt;

public class AbstractTestService {
    /**
     * Make an NTTableBuilder that is prepopulated with the given fields of given types
     *
     * @param fieldNames the fields to create
     * @param types      the corresponding field types
     * @return an NTTableBuilder that is prepopulated with the given fields of given types
     */
    protected static NTTableBuilder getNtTableBuilder(String[] fieldNames, ScalarType[] types) {
        NTTableBuilder builder = NTTable.createBuilder();
        for (int i = 0; i < fieldNames.length; i++) {
            builder.addColumn(fieldNames[i], types[i]);
        }
        return builder;
    }

    /**
     * Just barely good enough to get argument values for test.
     *
     * @param field the field
     * @return the string represented by the given field
     */
    protected static String fieldToString(PVField field) {
        String value;

        if (field instanceof PVBoolean) {
            value = Boolean.toString(((PVBoolean) field).get());
        } else if (field instanceof PVByte) {
            value = Byte.toString(((PVByte) field).get());
        } else if (field instanceof PVUByte) {
            value = Byte.toString(((PVUByte) field).get());
        } else if (field instanceof PVShort) {
            value = Short.toString(((PVShort) field).get());
        } else if (field instanceof PVInt) {
            value = Integer.toString(((PVInt) field).get());
        } else if (field instanceof PVLong) {
            value = Long.toString(((PVLong) field).get());
        } else if (field instanceof PVFloat) { // For float store the ieee value in the list as well
            value = Float.toString(((PVFloat) field).get());
        } else if (field instanceof PVDouble) { // For double store the ieee value in the list as well
            value = Double.toString(((PVDouble) field).get());
        } else if (field instanceof PVString) { // For strings quote them is they are subfields but leave them untouched if not
            value = ((PVString) field).get();
        } else {
            value = "";
        }
        return value;
    }

    /**
     * To set the contents of boolean array at the specified subfield in the given ntTable.
     *
     * @param ntTable    the given NTTable
     * @param fieldNames the list of all field names
     * @param fieldIndex the index of the subfield in the list of all field names
     * @param array      the boolean array to set as the value of the specified subfield
     */
    protected static void setBooleanArray(PVStructure ntTable, String[] fieldNames, int fieldIndex, boolean[] array) {
        ((PVBooleanArray) ntTable.getSubField(fieldNames[fieldIndex])).put(0, array.length, array, 0);
    }

    /**
     * To set the contents of short array at the specified subfield in the given ntTable.
     *
     * @param ntTable    the given NTTable
     * @param fieldNames the list of all field names
     * @param fieldIndex the index of the subfield in the list of all field names
     * @param array      the short array to set as the value of the specified subfield
     */
    protected static void setShortArray(PVStructure ntTable, String[] fieldNames, int fieldIndex, short[] array) {
        ((PVShortArray) ntTable.getSubField(fieldNames[fieldIndex])).put(0, array.length, array, 0);
    }

    /**
     * To set the contents of String array at the specified subfield in the given ntTable.
     *
     * @param ntTable    the given NTTable
     * @param fieldNames the list of all field names
     * @param fieldIndex the index of the subfield in the list of all field names
     * @param array      the String array to set as the value of the specified subfield
     */
    protected static void setStringArray(PVStructure ntTable, String[] fieldNames, int fieldIndex, String[] array) {
        ((PVStringArray) ntTable.getSubField(fieldNames[fieldIndex])).put(0, array.length, array, 0);
    }

    /**
     * Create an integer array response from the given list of values
     *
     * @param values the given list of values
     * @return integer array response
     */
    protected static PVStructure integerArrayResponse(int... values) {
        PVStructure retVal = NTScalarArray.createBuilder().value(pvInt).createPVStructure();
        ((PVIntArray) retVal.getSubField(NT_FIELD_NAME)).put(0, values.length, values, 0);
        return retVal;
    }

    /**
     * Return the given integer as the response
     *
     * @param value the given integer
     * @return an NTScalar containing the given integer value
     */
    protected static PVStructure integerResponse(Integer value) {
        PVStructure retVal = NTScalar.createBuilder().value(pvInt).createPVStructure();
        ((PVInt) retVal.getSubField(NT_FIELD_NAME)).put(value);
        return retVal;
    }

    /**
     * Return an NTTable response based on the given parameters
     *
     * @param fieldNames the field names
     * @param types      the types
     * @param param1     parameter 1
     * @return NTTable response
     */
    protected static PVStructure tableResponse(String[] fieldNames, ScalarType[] types, float[] param1) {
        NTTableBuilder builder = getNtTableBuilder(fieldNames, types);
        PVStructure retVal = builder.createPVStructure();
        PVStructure ntTable = ((PVStructure) retVal.getSubField("value"));

        ((PVFloatArray) ntTable.getSubField(fieldNames[0])).put(0, param1.length, param1, 0);
        return retVal;
    }

    /**
     * Return an NTTable response based on the given parameters
     *
     * @param fieldNames the field names
     * @param types      the types
     * @param param1     parameter 1
     * @param param2     parameter 2
     * @return NTTable response
     */
    protected static PVStructure tableResponse(String[] fieldNames, ScalarType[] types, String[] param1, float[] param2) {
        NTTableBuilder builder = getNtTableBuilder(fieldNames, types);
        PVStructure retVal = builder.createPVStructure();
        PVStructure ntTable = ((PVStructure) retVal.getSubField("value"));

        ((PVStringArray) ntTable.getSubField(fieldNames[0])).put(0, param1.length, param1, 0);
        ((PVFloatArray) ntTable.getSubField(fieldNames[1])).put(0, param2.length, param2, 0);
        return retVal;
    }

    /**
     * Return an NTTable response based on the given parameters
     *
     * @param fieldNames the field names
     * @param types      the types
     * @param param1     parameter 1
     * @param param2     parameter 2
     * @param param3     parameter 3
     * @param param4     parameter 4
     * @param param5     parameter 5
     * @param param6     parameter 6
     * @param param7     parameter 7
     * @return NTTable response
     */
    protected static PVStructure tableResponse(String[] fieldNames, ScalarType[] types, String[] param1, int[] param2, float[] param3, float[] param4, double[] param5, int[] param6, boolean[] param7) {
        NTTableBuilder builder = getNtTableBuilder(fieldNames, types);
        PVStructure retVal = builder.createPVStructure();
        PVStructure ntTable = ((PVStructure) retVal.getSubField("value"));

        ((PVStringArray) ntTable.getSubField(fieldNames[0])).put(0, param1.length, param1, 0);
        ((PVIntArray) ntTable.getSubField(fieldNames[1])).put(0, param2.length, param2, 0);
        ((PVFloatArray) ntTable.getSubField(fieldNames[2])).put(0, param3.length, param3, 0);
        ((PVFloatArray) ntTable.getSubField(fieldNames[3])).put(0, param4.length, param4, 0);
        ((PVDoubleArray) ntTable.getSubField(fieldNames[4])).put(0, param5.length, param5, 0);
        ((PVIntArray) ntTable.getSubField(fieldNames[5])).put(0, param6.length, param6, 0);
        setBooleanArray(ntTable, fieldNames, 6, param7);

        return retVal;
    }

    /**
     * Return an NTTable response based on the given parameters
     *
     * @param fieldNames the field names
     * @param types      the types
     * @param param1     parameter 1
     * @param param2     parameter 2
     * @param param3     parameter 3
     * @param param4     parameter 4
     * @param param5     parameter 5
     * @param param6     parameter 6
     * @param param7     parameter 7
     * @param param8     parameter 8
     * @param param9     parameter 9
     * @param param10    parameter 10
     * @return NTTable response
     */
    protected static PVStructure tableResponse(String[] fieldNames, ScalarType[] types, String[] param1, boolean[] param2, short[] param3, boolean[] param4, boolean[] param5, boolean[] param6, boolean[] param7, boolean[] param8, boolean[] param9, boolean[] param10) {
        NTTableBuilder builder = getNtTableBuilder(fieldNames, types);
        PVStructure retVal = builder.createPVStructure();
        PVStructure ntTable = ((PVStructure) retVal.getSubField("value"));
        setStringArray(ntTable, fieldNames, 0, param1);
        setBooleanArray(ntTable, fieldNames, 1, param2);
        setShortArray(ntTable, fieldNames, 2, param3);
        setBooleanArray(ntTable, fieldNames, 3, param4);
        setBooleanArray(ntTable, fieldNames, 4, param5);
        setBooleanArray(ntTable, fieldNames, 5, param6);
        setBooleanArray(ntTable, fieldNames, 6, param7);
        setBooleanArray(ntTable, fieldNames, 7, param8);
        setBooleanArray(ntTable, fieldNames, 8, param9);
        setBooleanArray(ntTable, fieldNames, 9, param10);
        return retVal;
    }
}
