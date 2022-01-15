/**
 * @file
 * @brief Enumerated type representing an AIDA-PVA data type.  This enum
 * contains static methods to help manipulate and work with AidaType.
 */
package edu.stanford.slac.aida.client;

import org.epics.pvdata.pv.*;

/**
 * Enumerated type representing an AIDA-PVA data type.  This enum
 * contains static methods to help manipulate and work with AidaType.
 */
public enum AidaType {
    VOID,           ///< Means that this setter does not return a value (only valid for setters)

    BOOLEAN,        ///< Getter returns Boolean
    BYTE,           ///< Getter returns Byte
    CHAR,           ///< Getter returns Byte
    SHORT,          ///< Getter returns a Short
    INTEGER,        ///< Getter returns an Integer
    LONG,           ///< Getter returns a Long
    FLOAT,          ///< Getter returns a Float
    DOUBLE,         ///< Getter returns a Double
    STRING,         ///< Getter returns a String

    BOOLEAN_ARRAY,  ///< Getter returns a List<Boolean>
    BYTE_ARRAY,     ///< Getter returns a List<Byte>
    CHAR_ARRAY,     ///< Getter returns a List<Byte>
    SHORT_ARRAY,    ///< Getter returns a List<Short>
    INTEGER_ARRAY,  ///< Getter returns an List<Integer>
    LONG_ARRAY,     ///< Getter returns a List<Long>
    FLOAT_ARRAY,    ///< Getter returns a List<Float>
    DOUBLE_ARRAY,   ///< Getter returns a List<Double>
    STRING_ARRAY,   ///< Getter returns a List<String>
    TABLE;          ///< Getter or setter returns a PvaTable

    /**
     * Normative Type ID string for URIs
     */
    public static final String NTURI_ID = "epics:nt/NTURI:1.0";
    /**
     * Normative Type ID string for Scalars
     */
    public static final String NTSCALAR_ID = "epics:nt/NTScalar:1.0";
    /**
     * Normative Type ID string for Scalar Arrays
     */
    public static final String NTSCALARARRAY_ID = "epics:nt/NTScalarArray:1.0";
    /**
     * Normative Type ID string for Tables
     */
    public static final String NTTABLE_ID = "epics:nt/NTTable:1.0";

    // AIDA-PVA standard field names in normative types
    /**
     * Normative Type standard field name for value field
     */
    public static final String NT_FIELD_NAME = "value";
    /**
     * Normative Type standard field name for labels field
     */
    public static final String NT_LABELS_NAME = "labels";
    /**
     * Normative Type standard field name for description field
     */
    public static final String NT_DESCRIPTIONS_NAME = "descriptions";

    /**
     * Convert this AidaType to a PVField.
     * This method maps {@link AidaType}s to PVFields.
     *
     * @return the PVField that maps to this AidaType.
     */
    @SuppressWarnings("unchecked")
    public <T extends PVField> Class<T> toPVFieldClass() {
        switch (this) {
            case BOOLEAN:
                return (Class<T>) PVBoolean.class;
            case BYTE:
            case CHAR:
                return (Class<T>) PVByte.class;
            case SHORT:
                return (Class<T>) PVShort.class;
            case INTEGER:
                return (Class<T>) PVInt.class;
            case LONG:
                return (Class<T>) PVLong.class;
            case FLOAT:
                return (Class<T>) PVFloat.class;
            case DOUBLE:
                return (Class<T>) PVDouble.class;
            case STRING:
                return (Class<T>) PVString.class;
            case BOOLEAN_ARRAY:
                return (Class<T>) PVBooleanArray.class;
            case BYTE_ARRAY:
            case CHAR_ARRAY:
                return (Class<T>) PVByteArray.class;
            case SHORT_ARRAY:
                return (Class<T>) PVShortArray.class;
            case INTEGER_ARRAY:
                return (Class<T>) PVIntArray.class;
            case LONG_ARRAY:
                return (Class<T>) PVLongArray.class;
            case FLOAT_ARRAY:
                return (Class<T>) PVFloatArray.class;
            case DOUBLE_ARRAY:
                return (Class<T>) PVDoubleArray.class;
            case STRING_ARRAY:
                return (Class<T>) PVStringArray.class;
            case TABLE:
                return (Class<T>) PVStructure.class;
        }
        return null;
    }

    /**
     * Get the AidaType of the given @p pvStructure.
     * <p>
     * Look in the field that stores all Normative Type's
     *
     * @param pvStructure the pvStructure to determine the AidaType of
     * @return the AidaType corresponding to the given @p pvStructure
     */
    public static AidaType from(PVStructure pvStructure) {
        if (pvStructure == null) {
            return VOID;
        }

        return from(pvStructure, NT_FIELD_NAME);
    }

    /**
     * Get the AidaType of the @p fieldName in the given @p pvStructure.
     *
     * @param pvStructure the pvStructure to pull the @p fieldName out of
     * @param fieldName   the name of the field within the pvStructure to determine the AidaType of
     * @return the AidaType corresponding to the p fieldName in the given @p pvStructure
     */
    public static AidaType from(PVStructure pvStructure, String fieldName) {
        if (pvStructure.getStructure().getID().equals(NTTABLE_ID)) {
            return TABLE;
        }

        PVField field = pvStructure.getSubField(fieldName);
        if (field instanceof PVBoolean) {
            return BOOLEAN;
        } else if (field instanceof PVByte) {
            return BYTE;
        } else if (field instanceof PVShort) {
            return SHORT;
        } else if (field instanceof PVInt) {
            return INTEGER;
        } else if (field instanceof PVLong) {
            return LONG;
        } else if (field instanceof PVFloat) {
            return FLOAT;
        } else if (field instanceof PVDouble) {
            return DOUBLE;
        } else if (field instanceof PVString) {
            return STRING;
        } else if (field instanceof PVBooleanArray) {
            return BOOLEAN_ARRAY;
        } else if (field instanceof PVByteArray) {
            return BYTE_ARRAY;
        } else if (field instanceof PVShortArray) {
            return SHORT_ARRAY;
        } else if (field instanceof PVIntArray) {
            return INTEGER_ARRAY;
        } else if (field instanceof PVLongArray) {
            return LONG_ARRAY;
        } else if (field instanceof PVFloatArray) {
            return FLOAT_ARRAY;
        } else if (field instanceof PVDoubleArray) {
            return DOUBLE_ARRAY;
        } else if (field instanceof PVStringArray) {
            return STRING_ARRAY;
        } else {
            return VOID;
        }
    }
}
