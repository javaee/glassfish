package org.glassfish.api;

import java.util.concurrent.Future;
import java.util.List;

/**
 * Some operations may be asynchronous and need to provide their results
 * as a list of future objects
 *
 * @author Jerome Dochez
 */
public interface FutureProvider<T> {

    public List<Future<T>> getFutures();

}
