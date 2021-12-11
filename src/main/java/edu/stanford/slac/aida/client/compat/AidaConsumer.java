package edu.stanford.slac.aida.client.compat;

public abstract class AidaConsumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    public abstract void accept(T t);
}
