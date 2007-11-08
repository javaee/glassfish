/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.deployment.impl.reflection.annotation;

import com.sun.persistence.api.deployment.ObjectFactory;
import com.sun.persistence.api.deployment.MergeManager;
import com.sun.persistence.deployment.impl.LogHelperDeployment;
import com.sun.persistence.utility.logging.Logger;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.enterprise.deployment.annotation.AnnotationHandler;

import java.lang.annotation.Annotation;

/**
 * This is a base class for all the handlers.
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class HandlerBase implements AnnotationHandler {

    protected MergeManager mergeManager;

    protected static ObjectFactory of = new ObjectFactory();

    protected final static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    protected final static Logger logger= LogHelperDeployment.getLogger();

    protected HandlerBase(MergeManager mergeManager) {
        this.mergeManager = mergeManager;
    }

    public Class<? extends Annotation>[] getTypeDependencies() {
        return null;
    }

}
