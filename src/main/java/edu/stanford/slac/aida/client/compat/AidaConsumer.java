package edu.stanford.slac.aida.client.compat;

public interface AidaConsumer<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t);
}
