/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package devtests.deployment.util;

import org.glassfish.apf.ErrorHandler;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.impl.AnnotationUtils;
                                                                                
/**
 * Standalone Implementation of ErrorHandler.
 *
 * @author Shing Wai Chan
 */
public class StandaloneErrorHandler implements ErrorHandler {
    /** Creates a new instance of StandaloneErrorHandler */
    public StandaloneErrorHandler() {
    }

    /**
     * Receive notication of a fine error message
     * @param ape The warning information
     * @throws any exception to stop the annotation processing 
     */ 
    public void fine(AnnotationProcessorException ape) throws
            AnnotationProcessorException {
        AnnotationUtils.getLogger().fine("Fine : " + ape);
    }

    /**
     * Receive notification of a warning
     * @param ape The warning information
     * @throws any exception to stop the annotation processing
     */
    public void warning(AnnotationProcessorException ape) throws
            AnnotationProcessorException {
        AnnotationUtils.getLogger().warning("Warning : " + ape);
    }

    /**
     * Receive notification of an error
     * @param ape The error information
     * @throws any exception to stop the annotation processing
     */
    public void error(AnnotationProcessorException ape) throws
            AnnotationProcessorException {
        AnnotationUtils.getLogger().severe("Error : " + ape);
        throw ape;
    }
}
