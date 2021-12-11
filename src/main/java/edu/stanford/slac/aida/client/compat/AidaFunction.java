package edu.stanford.slac.aida.client.compat;

public abstract class AidaFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    public abstract R apply(T t);

}
