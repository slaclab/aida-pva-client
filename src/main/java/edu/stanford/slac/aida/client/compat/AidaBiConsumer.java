package edu.stanford.slac.aida.client.compat;

public abstract class AidaBiConsumer<T, U> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    public abstract void accept(T t, U u);

}
