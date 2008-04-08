package com.sun.enterprise.v3.common;

/**
 * Used to carry a result or an exception justifying why a result could not be produced
 *
 * @author Jerome Dochez
 */
public class Result<T> {

    final T result;

    final Throwable error;

    public Result(T result) {
        this.result = result;
        error = null;
    }

    public Result(Throwable t) {
        result = null;
        this.error = t;
    }

    public boolean isSuccess() {
        return error==null;
    }

    public boolean isFailure() {
        return result==null;
    }

    public T result() {
        return result;
    }

    public Throwable exception() {
        return error;
    }
}
