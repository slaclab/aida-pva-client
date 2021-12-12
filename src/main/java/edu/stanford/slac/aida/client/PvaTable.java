package edu.stanford.slac.aida.client;

import lombok.ToString;
import org.epics.pvaccess.server.rpc.RPCRequestException;

import java.util.Map;
import java.util.Set;

import static org.epics.pvdata.pv.Status.StatusType.WARNING;

@ToString
public class PvaTable {
    public final Integer size;
    public final String[] labels;
    public final String[] descriptions;
    public final String[] units;
    public final Map<String, Object[]> values;

    /**
     * Constructor for PvaTable
     *
     * @param labels       labels array
     * @param units        units array
     * @param descriptions descriptions
     * @param values       values
     */
    PvaTable(String[] labels, String[] units, String[] descriptions, Map<String, Object[]> values) {
        this.labels = labels;
        this.units = units;
        this.descriptions = descriptions;
        this.values = values;
        Set<Map.Entry<String, Object[]>> entrySet = values.entrySet();
        if (!entrySet.isEmpty()) {
            this.size = entrySet.iterator().next().getValue().length;
        } else {
            this.size = 0;
        }
    }

    /**
     * Constructor for PvaTable
     *
     * @param labels labels array
     * @param units  units array
     * @param values values
     */
    PvaTable(String[] labels, String[] units, Map<String, Object[]> values) {
        this(labels, units, new String[0], values);
    }

    /**
     * Constructor for PvaTable
     *
     * @param labels labels array
     * @param values values
     */
    PvaTable(String[] labels, Map<String, Object[]> values) {
        this(labels, new String[0], values);
    }

    /**
     * Constructor for PvaTable
     *
     * @param values values
     */
    PvaTable(Map<String, Object[]> values) {
        this(new String[0], values);
    }

    /**
     * Get the specified vector from the PvaTable as a boolean array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of booleans
     * @throws RPCRequestException if the vector does not contain booleans
     */
    public Boolean[] getAsBooleans(String vector) throws RPCRequestException {
        return get(vector, Boolean[].class);
    }

    /**
     * Get the specified vector from the PvaTable as a byte array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of bytes
     * @throws RPCRequestException if the vector does not contain bytes
     */
    public Byte[] getAsBytes(String vector) throws RPCRequestException {
        return get(vector, Byte[].class);
    }

    /**
     * Get the specified vector from the PvaTable as a short array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of shorts
     * @throws RPCRequestException if the vector does not contain shorts
     */
    public Short[] getAsShorts(String vector) throws RPCRequestException {
        return get(vector, Short[].class);
    }

    /**
     * Get the specified vector from the PvaTable as a integer array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of integers
     * @throws RPCRequestException if the vector does not contain integers
     */
    public Integer[] getAsIntegers(String vector) throws RPCRequestException {
        return get(vector, Integer[].class);
    }

    /**
     * Get the specified vector from the PvaTable as a long array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of longs
     * @throws RPCRequestException if the vector does not contain longs
     */
    public Long[] getAsLongs(String vector) throws RPCRequestException {
        return get(vector, Long[].class);
    }

    /**
     * Get the specified vector from the PvaTable as a float array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of floats
     * @throws RPCRequestException if the vector does not contain floats
     */
    public Float[] getAsFloats(String vector) throws RPCRequestException {
        return get(vector, Float[].class);
    }

    /**
     * Get the specified vector from the PvaTable as a double array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of doubles
     * @throws RPCRequestException if the vector does not contain doubles
     */
    public Double[] getAsDoubles(String vector) throws RPCRequestException {
        return get(vector, Double[].class);
    }

    /**
     * Get the specified vector from the PvaTable as a string array.
     *
     * @param vector the name of the vector to retrieve
     * @return the array of strings
     * @throws RPCRequestException if the vector does not contain strings
     */
    public String[] getAsStrings(String vector) throws RPCRequestException {
        return get(vector, String[].class);
    }


    /**
     * Generic method for getting the specified vector as an array of the specified class
     *
     * @param vector vetor to retrieve
     * @param clazz  array class required
     * @param <T>    the class of the array
     * @return an array of the specified class
     * @throws RPCRequestException if the vector does not contain data of the specified class
     */
    @SuppressWarnings("unchecked")
    private <T> T[] get(String vector, Class<T[]> clazz) throws RPCRequestException {
        Object[] values = this.values.get(vector);
        return ((T[]) values);
    }
}
