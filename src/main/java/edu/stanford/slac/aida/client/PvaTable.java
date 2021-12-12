package edu.stanford.slac.aida.client;

import lombok.ToString;

import java.util.Map;
import java.util.Set;

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
     * Get the specified vector as an array
     *
     * @param vector vector to retrieve
     * @return an array
     */
    public Object[] get(String vector) {
        return this.values.get(vector);
    }
}
