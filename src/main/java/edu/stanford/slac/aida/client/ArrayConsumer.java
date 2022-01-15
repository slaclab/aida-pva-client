package edu.stanford.slac.aida.client;

import edu.stanford.slac.aida.client.compat.AidaBiConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.epics.pvdata.pv.ArrayData;
import org.epics.pvdata.pv.PVArray;

@Getter
@RequiredArgsConstructor
public abstract class ArrayConsumer<A, D, T> {
    private final AidaBiConsumer<T, Integer> consumer;

    /**
     * Create a new data buffer to store ArrayData of type D
     *
     * @return new data buffer
     */
    public abstract D create();

    /**
     * Performs the data copy
     *
     * @param src    the array to copy the data from
     * @param offset the offset into the array data
     * @param length the length of data to copy
     * @param dst    the data buffer to copy the data into
     * @return the number of items copied to the destination buffer
     */
    public abstract int get(A src, int offset, int length, D dst);

    /**
     * Internal: Generic array consumer to iterate over any type of PVArray calling the consumer for each value
     *
     * @param array         the PVArray to iterate over
     * @param arrayConsumer the ArrayData consumer - this consumes PVArrays
     * @param <A>           the PVArray subtype e.g. PVBooleanArray
     * @param <D>           the ArrayData subtype e.g. BooleanArrayData
     * @param <T>           the type of data that the consumer expects e.g. Boolean
     */
    @SuppressWarnings("unchecked")
    static <A extends PVArray, D extends ArrayData<p[]>, T, p> void consumeArray(A array, ArrayConsumer<A, D, T> arrayConsumer) {
        int len = array.getLength(), offset = 0, index = 0;
        while (offset < len) {
            D data = arrayConsumer.create();
            int num = arrayConsumer.get(array, offset, (len - offset), data);
            for (int i = 0; i < num; i++, index++) {
                arrayConsumer.getConsumer().accept((T) data.data[i], index);
            }
            offset += num;
        }
    }
}
