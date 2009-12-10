/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package javax.ejb.spi;

import java.util.Map;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.EJBException;

/**
 * The EJBContainerProvider SPI is used by the embeddable container bootstrap
 * class to initialize a suitable embeddable container.
 */
public interface EJBContainerProvider {

    /**
     * Called by the embeddable container bootstrap process to find a suitable embeddable
     * container implementation.  An embeddable container provider may deem itself as 
     * appropriate for the embeddable application if any of the following are true :
     *
     *   The javax.ejb.embeddable.initial property was included in the Map passed to 
     *   createEJBContainer and the value of the property is the provider's implementation 
     *   class.
     *
     *   No javax.ejb.embeddable.initial property was specified.
     *
     * If a provider does not qualify as the provider for the embeddable application, it 
     * must return null.
     *
     * @return EJBContainer instance or null
     */
    public EJBContainer createEJBContainer(Map<?,?> properties) throws EJBException;

}
