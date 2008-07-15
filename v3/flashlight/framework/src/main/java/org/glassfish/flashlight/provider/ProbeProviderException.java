package org.glassfish.flashlight.provider;

/**
 * @author Mahesh Kannan
 *         Date: May 29, 2008
 */
public class ProbeProviderException
    extends RuntimeException {

    public ProbeProviderException(String msg) {
            super(msg);
    }

    public ProbeProviderException(String msg, Throwable th) {
            super(msg, th);
    }            

}
