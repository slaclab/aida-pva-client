/**
 * @file
 * @brief Build arguments for use with the AidaPvaRequest.
 */
package edu.stanford.slac.aida.client;

import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.pv.*;

import java.util.*;

import static edu.stanford.slac.aida.client.compat.ArrayUtils.toPrimitive;
import static org.epics.pvdata.pv.Status.StatusType.ERROR;

/**
 * Used to build arguments for use with the AidaPvaRequest.
 * <p>
 * This follows the builder pattern except that you need to call initialise after build,
 * so you need to keep the object around after build. And add methods don't return
 * the object.
 * <p>
 * e.g.
 * <pre>{@code
 *      Structure argumentBuilder = new ArgumentBuilder()
 *          .argumentBuilder.addArgument("TYPE", "FLOAT");
 *
 *      // Prepare the arguments
 *      Structure arguments = argumentBuilder.build();
 *
 *      Structure uriStructure =
 *         fieldCreate.createStructure("epics:nt/NTURI:1.0",
 *                 new String[]{"path", "query"&#125;,
 *                 new Field[]{fieldCreate.createScalar(ScalarType.pvString), arguments&#125;
 *         );
 *
 *      // Make the query (contains the uri and arguments
 *       PVStructure request = PVDataFactory
 *          .getPVDataCreate()
 *              .createPVStructure(uriStructure);
 *
 *      // Set the request path
 *      request.getStringField("path").put("XCOR:LI03:120:LEFF");
 *
 *      // Set the request query values
 *      PVStructure query = request.getStructureField("query");
 *      argumentBuilder.initializeQuery(query);
 *
 *      ...
 * }</pre>
 */
class ArgumentBuilder {
    /**
     * To be able to create fields in PV Access you need a field creator factory
     */
    private final static FieldCreate fieldCreate = FieldFactory.getFieldCreate();

    /**
     * Map of name setTo pairs for arguments and values
     */
    private final Map<String, Object> fieldMap = new HashMap<String, Object>();

    /**
     * Add an argument
     * See {@link ArgumentBuilder} for more information.
     *
     * @param name  name of argument
     * @param value value of argument
     */
    protected void addArgument(String name, Object value) {
        fieldMap.put(name, value);
    }

    /**
     * Builds the set of arguments based on the ones you've specified
     * See {@link ArgumentBuilder} for more information.
     *
     * @return EPICS PVStructure containing the fields you've specified
     */
    public Structure build() {
        return getStructure(fieldMap);
    }

    /**
     * Get a structure from the given value map
     *
     * @param valueMap the given value map
     * @return the corresponding structure
     */
    private Structure getStructure(Map<String, Object> valueMap) {
        List<String> names = new ArrayList<String>();
        List<Field> fields = new ArrayList<Field>();

        // Create the list of names and fields to create the structure with
        for (Map.Entry<String, Object> entrySet : valueMap.entrySet()) {
            names.add(entrySet.getKey());
            fields.add(getField(entrySet.getValue()));
        }

        // Create the query structure that will host the fields
        // note that you need to call initialise to fill in the actual values
        return FieldFactory.getFieldCreate().createStructure(names.toArray(new String[0]), fields.toArray(new Field[0]));
    }

    boolean contains(String argument) {
        return fieldMap.containsKey(argument);
    }

    /**
     * Internal: From a given value determine the type of field that should be created to hold its value
     *
     * @param value the given value
     * @return the field to hold this value
     */
    @SuppressWarnings("unchecked")
    private Field getField(Object value) {
        if (value instanceof Boolean) {
            return (fieldCreate.createScalar(ScalarType.pvBoolean));
        } else if (value instanceof Byte) {
            return (fieldCreate.createScalar(ScalarType.pvByte));
        } else if (value instanceof Short) {
            return (fieldCreate.createScalar(ScalarType.pvShort));
        } else if (value instanceof Integer) {
            return (fieldCreate.createScalar(ScalarType.pvInt));
        } else if (value instanceof Long) {
            return (fieldCreate.createScalar(ScalarType.pvLong));
        } else if (value instanceof Float) {
            if (isInteger(((Float) value).doubleValue())) {
                return (fieldCreate.createScalar(ScalarType.pvInt));
            } else if (isLong(((Float) value).doubleValue())) {
                return (fieldCreate.createScalar(ScalarType.pvLong));
            } else {
                return (fieldCreate.createScalar(ScalarType.pvFloat));
            }
        } else if (value instanceof Double) {
            if (isInteger((Double) value)) {
                return (fieldCreate.createScalar(ScalarType.pvInt));
            } else if (isLong((Double) value)) {
                return (fieldCreate.createScalar(ScalarType.pvLong));
            } else {
                return (fieldCreate.createScalar(ScalarType.pvDouble));
            }
        } else if (value instanceof String || value instanceof Character || value instanceof Character[]) {
            return (fieldCreate.createScalar(ScalarType.pvString));
        } else if (value instanceof Object[]) {
            Object[] objects = (Object[]) value;
            boolean hasElements = objects.length > 0;
            if (value instanceof Boolean[] || (hasElements && objects[0] instanceof Boolean)) {
                return (fieldCreate.createScalarArray(ScalarType.pvBoolean));
            } else if (value instanceof Byte[] || (hasElements && objects[0] instanceof Byte)) {
                return (fieldCreate.createScalarArray(ScalarType.pvByte));
            } else if (value instanceof Short[] || (hasElements && objects[0] instanceof Short)) {
                return (fieldCreate.createScalarArray(ScalarType.pvShort));
            } else if (value instanceof Integer[] || (hasElements && objects[0] instanceof Integer)) {
                return (fieldCreate.createScalarArray(ScalarType.pvInt));
            } else if (value instanceof Long[] || (hasElements && objects[0] instanceof Long)) {
                return (fieldCreate.createScalarArray(ScalarType.pvLong));
            } else if (value instanceof Float[] || (hasElements && objects[0] instanceof Float)) {
                if (areIntegers(Arrays.asList(((Object[]) value)))) {
                    return (fieldCreate.createScalarArray(ScalarType.pvInt));
                } else if (areLongs(Arrays.asList(((Object[]) value)))) {
                    return (fieldCreate.createScalarArray(ScalarType.pvLong));
                } else {
                    return (fieldCreate.createScalarArray(ScalarType.pvFloat));
                }
            } else if (value instanceof Double[] || (hasElements && objects[0] instanceof Double)) {
                if (areIntegers(Arrays.asList(((Object[]) value)))) {
                    return (fieldCreate.createScalarArray(ScalarType.pvInt));
                } else if (areLongs(Arrays.asList(((Object[]) value)))) {
                    return (fieldCreate.createScalarArray(ScalarType.pvLong));
                } else {
                    return (fieldCreate.createScalarArray(ScalarType.pvDouble));
                }
            } else {
                return (fieldCreate.createScalarArray(ScalarType.pvString));
            }
        } else if (value instanceof List) {
            // determine type of list by getting first element.
            List<?> valueList = (List<?>) value;
            if (valueList.isEmpty()) {
                return fieldCreate.createScalarArray(ScalarType.pvString);
            }
            Object firstElement = valueList.get(0);
            if (firstElement instanceof Boolean) {
                return (fieldCreate.createScalarArray(ScalarType.pvBoolean));
            } else if (firstElement instanceof Byte) {
                return (fieldCreate.createScalarArray(ScalarType.pvByte));
            } else if (firstElement instanceof Short) {
                return (fieldCreate.createScalarArray(ScalarType.pvShort));
            } else if (firstElement instanceof Integer) {
                return (fieldCreate.createScalarArray(ScalarType.pvInt));
            } else if (firstElement instanceof Long) {
                return (fieldCreate.createScalarArray(ScalarType.pvLong));
            } else if (firstElement instanceof Float) {
                if (areIntegers((List<Object>) valueList)) {
                    return (fieldCreate.createScalarArray(ScalarType.pvInt));
                } else if (areLongs((List<Object>) value)) {
                    return (fieldCreate.createScalarArray(ScalarType.pvLong));
                } else {
                    return (fieldCreate.createScalarArray(ScalarType.pvFloat));
                }
            } else if (firstElement instanceof Double) {
                if (areIntegers((List<Object>) valueList)) {
                    return (fieldCreate.createScalarArray(ScalarType.pvInt));
                } else if (areLongs((List<Object>) value)) {
                    return (fieldCreate.createScalarArray(ScalarType.pvLong));
                } else {
                    return (fieldCreate.createScalarArray(ScalarType.pvDouble));
                }
            } else if (firstElement instanceof String || firstElement instanceof Character) {
                return (fieldCreate.createScalarArray(ScalarType.pvString));
            }
            return (fieldCreate.createScalar(ScalarType.pvString));
        } else if (value instanceof Map) {
            // For structures then we recurse through this creating subfields for each key
            // with fields for the associated values
            @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) value;
            return getStructure(map);
        } else {
            throw new RuntimeException("Unsupported type specified for argument value: " + value.getClass());
        }
    }

    /**
     * Internal: Set the field values in the arguments in the query.
     * See {@link ArgumentBuilder} for more information.
     *
     * @param query the query
     */
    void initializeQuery(PVStructure query) throws RPCRequestException {
        initializeStructure(query, fieldMap);
    }

    /**
     * Internal: Set the field values in the given structure.
     * Assumes that the structure has been created with field names that match the keys in the valueMap
     * Also assumes that the types of the values and structure fields match
     *
     * @param structure the given structure
     * @param valueMap  the values to set in the structure
     */
    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    private void initializeStructure(PVStructure structure, Map<String, Object> valueMap) throws RPCRequestException {
        for (Map.Entry<String, Object> entrySet : valueMap.entrySet()) {
            String name = entrySet.getKey();
            Object value = entrySet.getValue();
            PVField pvField = structure.getSubField(name);

            if (pvField instanceof PVBoolean) {
                ((PVBoolean) (pvField)).put((Boolean) value);
            } else if (pvField instanceof PVByte) {
                ((PVByte) (pvField)).put((Byte) value);
            } else if (pvField instanceof PVShort) {
                ((PVShort) (pvField)).put((Short) value);
            } else if (pvField instanceof PVInt) {
                if (value instanceof Float) {
                    ((PVInt) (pvField)).put(((Float) value).intValue());
                } else if (value instanceof Double) {
                    ((PVInt) (pvField)).put(((Double) value).intValue());
                } else {
                    ((PVInt) (pvField)).put((Integer) value);
                }
            } else if (pvField instanceof PVLong) {
                if (value instanceof Float) {
                    ((PVLong) (pvField)).put(((Float) value).longValue());
                } else if (value instanceof Double) {
                    ((PVLong) (pvField)).put(((Double) value).longValue());
                } else {
                    ((PVLong) (pvField)).put((Long) value);
                }
            } else if (pvField instanceof PVFloat) {
                ((PVFloat) (pvField)).put((Float) value);
            } else if (pvField instanceof PVDouble) {
                ((PVDouble) (pvField)).put((Double) value);
            } else if (pvField instanceof PVString) {
                if (value instanceof Character) {
                    ((PVString) (pvField)).put(((Character) value).toString());
                } else if (value instanceof Character[]) {
                    Character[] valueArray = ((Character[]) value);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (char character : valueArray) {
                        stringBuilder.append(character);
                    }
                    ((PVString) (pvField)).put(stringBuilder.toString());
                } else {
                    ((PVString) (pvField)).put((String) value);
                }
            } else if (pvField instanceof PVBooleanArray) {
                Boolean[] list;
                if (value instanceof Boolean[]) {
                    list = (Boolean[]) value;
                } else if (value instanceof Object[]) {
                    Object[] objects = (Object[]) value;
                    list = new Boolean[objects.length];
                    try {
                        System.arraycopy(objects, 0, list, 0, list.length);
                    } catch (Exception e) {
                        throw new RPCRequestException(ERROR, "Non-homogenous array detected while initialising NTURI");
                    }
                } else {
                    List<Boolean> valueList = (List<Boolean>) value;
                    list = valueList.toArray(new Boolean[0]);
                }
                ((PVBooleanArray) (pvField)).put(0, list.length, toPrimitive(list), 0);
            } else if (pvField instanceof PVByteArray) {
                Byte[] list;
                if (value instanceof Byte[]) {
                    list = (Byte[]) value;
                } else if (value instanceof Character[]) {
                    List<Byte> valueList = toByteList(Arrays.asList((Character[]) value));
                    list = valueList.toArray(new Byte[0]);
                } else if (value instanceof Object[]) {
                    Object[] objects = (Object[]) value;
                    list = new Byte[objects.length];
                    try {
                        System.arraycopy(objects, 0, list, 0, list.length);
                    } catch (Exception e) {
                        throw new RPCRequestException(ERROR, "Non-homogenous array detected while initialising NTURI");
                    }
                } else {
                    List<Byte> valueList = toByteList((List<?>) value);
                    list = valueList.toArray(new Byte[0]);
                }
                ((PVByteArray) (pvField)).put(0, list.length, toPrimitive(list), 0);
            } else if (pvField instanceof PVShortArray) {
                Short[] list;
                if (value instanceof Short[]) {
                    list = (Short[]) value;
                } else if (value instanceof Object[]) {
                    Object[] objects = (Object[]) value;
                    list = new Short[objects.length];
                    try {
                        System.arraycopy(objects, 0, list, 0, list.length);
                    } catch (Exception e) {
                        throw new RPCRequestException(ERROR, "Non-homogenous array detected while initialising NTURI");
                    }
                } else {
                    List<Short> valueList = (List<Short>) value;
                    list = valueList.toArray(new Short[0]);
                }
                ((PVShortArray) (pvField)).put(0, list.length, toPrimitive(list), 0);
            } else if (pvField instanceof PVIntArray) {
                Integer[] list;
                if (value instanceof Integer[]) {
                    list = (Integer[]) value;
                } else if (value instanceof Object[]) {
                    List<Integer> valueList = toIntegerList(Arrays.asList((Object[]) value));
                    list = valueList.toArray(new Integer[0]);
                } else {
                    List<Integer> valueList = toIntegerList((List<?>) value);
                    list = valueList.toArray(new Integer[0]);
                }
                ((PVIntArray) (pvField)).put(0, list.length, toPrimitive(list), 0);
            } else if (pvField instanceof PVLongArray) {
                Long[] list;
                if (value instanceof Long[]) {
                    list = (Long[]) value;
                } else if (value instanceof Object[]) {
                    List<Long> valueList = toLongList(Arrays.asList((Object[]) value));
                    list = valueList.toArray(new Long[0]);
                } else {
                    List<Long> valueList = toLongList((List<?>) value);
                    list = valueList.toArray(new Long[0]);
                }
                ((PVLongArray) (pvField)).put(0, list.length, toPrimitive(list), 0);
            } else if (pvField instanceof PVFloatArray) {
                Float[] list;
                if (value instanceof Float[]) {
                    list = (Float[]) value;
                } else if (value instanceof Object[]) {
                    Object[] objects = (Object[]) value;
                    list = new Float[objects.length];
                    try {
                        System.arraycopy(objects, 0, list, 0, list.length);
                    } catch (Exception e) {
                        throw new RPCRequestException(ERROR, "Non-homogenous array detected while initialising NTURI");
                    }
                } else {
                    // Some values may be given as doubles even though we want floats (e.g. PI) so we need to coerce all to Floats first
                    List<Object> values = (List<Object>) value;
                    list = new Float[values.size()];
                    for (int i = 0; i < values.size(); i++) {
                        Object val = values.get(i);
                        if (val instanceof Float) {
                            list[i] = ((Float) val);
                        } else if (val instanceof Double) {
                            list[i] = ((Double) val).floatValue();
                        } else {
                            list[i] = Float.parseFloat(val.toString());
                        }
                    }
                }
                ((PVFloatArray) (pvField)).put(0, list.length, toPrimitive(list), 0);
            } else if (pvField instanceof PVDoubleArray) {
                Double[] list;
                if (value instanceof Double[]) {
                    list = (Double[]) value;
                } else if (value instanceof Object[]) {
                    Object[] objects = (Object[]) value;
                    list = new Double[objects.length];
                    try {
                        System.arraycopy(objects, 0, list, 0, list.length);
                    } catch (Exception e) {
                        throw new RPCRequestException(ERROR, "Non-homogenous array detected while initialising NTURI");
                    }
                } else {
                    List<Double> valueList = (List<Double>) value;
                    list = valueList.toArray(new Double[0]);
                }
                ((PVDoubleArray) (pvField)).put(0, list.length, toPrimitive(list), 0);
            } else if (pvField instanceof PVStringArray) {
                String[] list;
                if (value instanceof String[]) {
                    list = (String[]) value;
                } else if (value instanceof Object[]) {
                    Object[] objects = (Object[]) value;
                    list = new String[objects.length];
                    try {
                        System.arraycopy(objects, 0, list, 0, list.length);
                    } catch (Exception e) {
                        throw new RPCRequestException(ERROR, "Non-homogenous array detected while initialising NTURI");
                    }
                } else {
                    List<String> valueList = (List<String>) value;
                    list = valueList.toArray(new String[0]);
                }
                ((PVStringArray) (pvField)).put(0, list.length, list, 0);
            } else if (pvField instanceof PVStructure) {
                Map<String, Object> subValueMap = (Map<String, Object>) value;
                initializeStructure((PVStructure) pvField, subValueMap);
            } else {
                throw new RuntimeException("Unknown type specified for argument value");
            }
        }
    }

    /**
     * Internal: When we need an integer list but may have been given floats and doubles
     * use this method to convert everything to integers.  Must only be used if
     * you've already called areIntegers() on the list.
     *
     * @param values the list of values to convert
     * @return the converted list
     */
    private List<Integer> toIntegerList(List<?> values) {
        List<Integer> list = new ArrayList<Integer>();
        for (Object value : values) {
            if (value instanceof Float) {
                list.add(((Float) value).intValue());
            } else if (value instanceof Double) {
                list.add(((Double) value).intValue());
            } else {
                list.add((Integer) value);
            }
        }
        return list;
    }

    /**
     * Internal: When we need a long list but may have been given floats and doubles
     * use this method to convert everything to longs.  Must only be used if
     * you've already called areLongs() on the list.
     *
     * @param values the list of values to convert
     * @return the converted list
     */
    private List<Long> toLongList(List<?> values) {
        List<Long> list = new ArrayList<Long>();
        for (Object value : values) {
            if (value instanceof Float) {
                list.add(((Float) value).longValue());
            } else if (value instanceof Double) {
                list.add(((Double) value).longValue());
            } else {
                list.add((Long) value);
            }
        }
        return list;
    }

    /**
     * Internal: When we need a byte list but may have been given chars
     * use this method to convert everything to bytes.  Must only be used if
     * you've already called areBytes() on the list.
     *
     * @param values the list of values to convert
     * @return the converted list
     */
    private List<Byte> toByteList(List<?> values) {
        List<Byte> list = new ArrayList<Byte>();
        for (Object value : values) {
            if (value instanceof Character) {
                list.add((byte) ((Character) value).charValue());
            } else {
                list.add((Byte) value);
            }
        }
        return list;
    }

    /**
     * Internal: To Coerce arrays into lists
     *
     * @param value the value
     * @return the coerced value if it was an array otherwise value
     */
    private Object coerceArrays(Object value) {
        if (value instanceof Boolean[]) {
            return Arrays.asList((Boolean[]) value);
        } else if (value instanceof Byte[]) {
            return Arrays.asList((Byte[]) value);
        } else if (value instanceof Short[]) {
            return Arrays.asList((Short[]) value);
        } else if (value instanceof Integer[]) {
            return Arrays.asList((Integer[]) value);
        } else if (value instanceof Long[]) {
            return Arrays.asList((Long[]) value);
        } else if (value instanceof Float[]) {
            return Arrays.asList((Float[]) value);
        } else if (value instanceof Double[]) {
            return Arrays.asList((Double[]) value);
        } else if (value instanceof String[]) {
            return Arrays.asList((String[]) value);
        } else if (value instanceof Object[]) {
            return Arrays.asList((Object[]) value);
        } else {
            return value;
        }
    }

    /**
     * Internal: Return true if the given double is really an integer value
     *
     * @param d the given double
     * @return true if the double is an integer value
     */
    private boolean isInteger(Double d) {
        return d < Integer.MAX_VALUE && d > Integer.MIN_VALUE && (d % 1) == 0;
    }

    /**
     * Internal: Return true if the given double is really a long integer value
     *
     * @param d the given double
     * @return true if the double is a long integer value
     */
    private boolean isLong(Double d) {
        return d < Long.MAX_VALUE && d > Long.MIN_VALUE && (d % 1) == 0;
    }

    /**
     * Internal: Return true if the given list are all integer values
     *
     * @param list the given list
     * @return true if the list contains only integers
     */
    private boolean areIntegers(List<Object> list) {
        for (Object d : list) {
            if (d instanceof Float) {
                if (!isInteger(((Float) d).doubleValue())) {
                    return false;
                }
            } else if (d instanceof Double) {
                if (!isInteger((Double) d)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Internal: Return true if the given list are all long integer values
     *
     * @param list the given list
     * @return true if the list contains only long integers
     */
    private boolean areLongs(List<Object> list) {
        for (Object d : list) {
            if (d instanceof Float) {
                if (!isLong(((Float) d).doubleValue())) {
                    return false;
                }
            } else if (d instanceof Double) {
                if (!isLong((Double) d)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstTime = true;
        for (Map.Entry<String, Object> entrySet : fieldMap.entrySet()) {
            String name = entrySet.getKey();
            Object value = coerceArrays(entrySet.getValue());
            if (!firstTime) {
                stringBuilder.append(", ");
            }
            firstTime = false;
            stringBuilder.append(name).append("=").append(value);
        }
        return stringBuilder.toString();
    }
}
