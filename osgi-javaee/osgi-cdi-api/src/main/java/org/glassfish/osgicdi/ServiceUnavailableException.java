package org.glassfish.osgicdi;

import org.osgi.framework.ServiceException;

/**
 * This exception is thrown to indicate that the service is unavailable.
 * If an <code>OSGiService</code> service reference is marked as dynamic, 
 * an attempt is made to get a reference to the service in the OSGi Service 
 * Registry when the service is used, and then the method is 
 * invoked on the newly obtained service. If the service cannot be discovered
 * or a reference obtained, the <code>ServiceUnavailableException</code>
 * is thrown.
 * 
 * @author Sivakumar Thyagarajan
 */
public class ServiceUnavailableException extends ServiceException {

    private static final long serialVersionUID = -8776963108373969053L;

    /**
     * {@inheritDoc}
     */
    public ServiceUnavailableException(String msg, int type, Throwable cause) {
        super(msg, type, cause);
    }

}
