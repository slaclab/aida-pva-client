/**
 * @file
 * @brief Utilities to manipulate PVField.
 */
package edu.stanford.slac.aida.client;

import edu.stanford.slac.aida.client.compat.AidaBiConsumer;
import edu.stanford.slac.aida.client.compat.AidaConsumer;
import org.epics.pvdata.pv.*;

import static edu.stanford.slac.aida.client.ArrayConsumer.consumeArray;


/**
 * @brief Utilities to manipulate PVField.
 */
public class PVUtils {

    /**
     * Internal: An iterator to iterate over PVArrays.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     * <p>
     * e.g.
     * <pre>{@code
     *      PVBooleanArray booleanPvArray = pvStruct.getSubfield(PVBooleanArray.class, "value");
     *      arrayIterator(booleanPvArray, (bool) -> System.out.println("Value: " + bool));
     * }</pre>
     *
     * @param array    the array you provide to iterate over
     * @param consumer the consumer function you provide to process the elements.
     */
    static <T extends PVField> void arrayIterator(PVScalarArray array, final AidaConsumer<Object> consumer) {
        arrayLoop(array, new AidaBiConsumer<Object, Integer>() {
            @Override
            public void accept(Object s, Integer i) {
                consumer.accept(s);
            }
        });
    }

    /**
     * Internal: Sometimes you want to process an array but have an index counter automatically maintained for
     * you so that you can know which element you're processing.  For that you'll use the
     * array loop.
     * <p>
     * e.g.
     * <pre>{@code
     *      PVStringArray stringPvArray = pvStruct.getSubfield(PVStringArray.class, "value");
     *      arrayLoop(stringPvArray, (s, i) -> System.out.println("Value[" + i + "]=\"" + a "\""));
     * }</pre>
     *
     * @param array    the array you provide to iterate over
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     */
    static <T extends PVScalarArray> void arrayLoop(T array, final AidaBiConsumer<Object, Integer> consumer) {
        if (array instanceof PVBooleanArray) {
            booleanArrayLoop((PVBooleanArray) array, new AidaBiConsumer<Boolean, Integer>() {
                @Override
                public void accept(Boolean s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        } else if (array instanceof PVShortArray) {
            shortArrayLoop((PVShortArray) array, new AidaBiConsumer<Short, Integer>() {
                @Override
                public void accept(Short s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        } else if (array instanceof PVIntArray) {
            integerArrayLoop((PVIntArray) array, new AidaBiConsumer<Integer, Integer>() {
                @Override
                public void accept(Integer s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        } else if (array instanceof PVLongArray) {
            longArrayLoop((PVLongArray) array, new AidaBiConsumer<Long, Integer>() {
                @Override
                public void accept(Long s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        } else if (array instanceof PVFloatArray) {
            floatArrayLoop((PVFloatArray) array, new AidaBiConsumer<Float, Integer>() {
                @Override
                public void accept(Float s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        } else if (array instanceof PVDoubleArray) {
            doubleArrayLoop((PVDoubleArray) array, new AidaBiConsumer<Double, Integer>() {
                @Override
                public void accept(Double s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        } else if (array instanceof PVStringArray) {
            stringArrayLoop((PVStringArray) array, new AidaBiConsumer<String, Integer>() {
                @Override
                public void accept(String s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        } else if (array instanceof PVByteArray) {
            byteArrayLoop((PVByteArray) array, new AidaBiConsumer<Byte, Integer>() {
                @Override
                public void accept(Byte s, Integer i) {
                    consumer.accept(s, i);
                }
            });
        }
    }

    /**
     * Internal: An iterator to iterate over PVBooleanArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv boolean array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void booleanArrayLoop(PVBooleanArray array, AidaBiConsumer<Boolean, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVBooleanArray, BooleanArrayData, Boolean>(consumer) {
            @Override
            public BooleanArrayData create() {
                return new BooleanArrayData();
            }

            @Override
            public int get(PVBooleanArray src, int offset, int length, BooleanArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: An iterator to iterate over PVStringArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv string array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void stringArrayLoop(PVStringArray array, AidaBiConsumer<String, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVStringArray, StringArrayData, String>(consumer) {
            @Override
            public StringArrayData create() {
                return new StringArrayData();
            }

            @Override
            public int get(PVStringArray src, int offset, int length, StringArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: An iterator to iterate over PVDoubleArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv double array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void doubleArrayLoop(PVDoubleArray array, AidaBiConsumer<Double, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVDoubleArray, DoubleArrayData, Double>(consumer) {
            @Override
            public DoubleArrayData create() {
                return new DoubleArrayData();
            }

            @Override
            public int get(PVDoubleArray src, int offset, int length, DoubleArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: An iterator to iterate over PVFloatArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv float array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void floatArrayLoop(PVFloatArray array, AidaBiConsumer<Float, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVFloatArray, FloatArrayData, Float>(consumer) {
            @Override
            public FloatArrayData create() {
                return new FloatArrayData();
            }

            @Override
            public int get(PVFloatArray src, int offset, int length, FloatArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: An iterator to iterate over PVLongArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv lon array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void longArrayLoop(PVLongArray array, AidaBiConsumer<Long, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVLongArray, LongArrayData, Long>(consumer) {
            @Override
            public LongArrayData create() {
                return new LongArrayData();
            }

            @Override
            public int get(PVLongArray src, int offset, int length, LongArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: An iterator to iterate over PVIntArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv integer array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void integerArrayLoop(PVIntArray array, AidaBiConsumer<Integer, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVIntArray, IntArrayData, Integer>(consumer) {
            @Override
            public IntArrayData create() {
                return new IntArrayData();
            }

            @Override
            public int get(PVIntArray src, int offset, int length, IntArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: An iterator to iterate over PVShortArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv short array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void shortArrayLoop(PVShortArray array, AidaBiConsumer<Short, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVShortArray, ShortArrayData, Short>(consumer) {
            @Override
            public ShortArrayData create() {
                return new ShortArrayData();
            }

            @Override
            public int get(PVShortArray src, int offset, int length, ShortArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: An iterator to iterate over PVByteArray.  You can provide a consumer of the items to carry out
     * any action you want on the array elements.
     *
     * @param array    the pv byte array
     * @param consumer the consumer function you provide to process the elements.
     *                 The consumer signature
     *                 <pre>@{code aidaBiConsumer(Object o, Integer i); }</pre>
     */
    static void byteArrayLoop(PVByteArray array, AidaBiConsumer<Byte, Integer> consumer) {
        consumeArray(array, new ArrayConsumer<PVByteArray, ByteArrayData, Byte>(consumer) {
            @Override
            public ByteArrayData create() {
                return new ByteArrayData();
            }

            @Override
            public int get(PVByteArray src, int offset, int length, ByteArrayData dst) {
                return src.get(offset, (length - offset), dst);
            }
        });
    }

    /**
     * Internal: Get a value from the given PVField by using the PVField class provided as the determinant
     *
     * @param value the value to extract
     * @return the extracted value
     */
    static Object extractScalarValue(PVField value) {
        if (value instanceof PVBoolean) {
            return ((PVBoolean) value).get();
        } else if (value instanceof PVShort) {
            return ((PVShort) value).get();
        } else if (value instanceof PVInt) {
            return ((PVInt) value).get();
        } else if (value instanceof PVLong) {
            return ((PVLong) value).get();
        } else if (value instanceof PVFloat) {
            return ((PVFloat) value).get();
        } else if (value instanceof PVDouble) {
            return ((PVDouble) value).get();
        } else if (value instanceof PVString) {
            return ((PVString) value).get();
        } else if (value instanceof PVByte) {
            return ((PVByte) value).get();
        }
        return null;
    }
}
