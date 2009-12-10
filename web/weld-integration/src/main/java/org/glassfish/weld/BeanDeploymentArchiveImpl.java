/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.weld;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.weld.ejb.EjbDescriptorImpl;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.ejb.spi.EjbDescriptor;


/*
 * The means by which Weld Beans are discovered on the classpath. 
 */
public class BeanDeploymentArchiveImpl implements BeanDeploymentArchive {
   
    private Logger logger = Logger.getLogger(BeanDeploymentArchiveImpl.class.getName());

    private String id;
    private final List<Class<?>> wbClasses;
    private final List<URL> wbUrls;
    private final Collection<EjbDescriptor<?>> ejbDescImpls;
    private List<BeanDeploymentArchive> beanDeploymentArchives;

    private SimpleServiceRegistry simpleServiceRegistry = null;

    public BeanDeploymentArchiveImpl(String id, List<Class<?>> wbClasses, List<URL> wbUrls,
                                     Collection<com.sun.enterprise.deployment.EjbDescriptor> ejbs) {
        this.id = id;
        this.wbClasses = wbClasses;
        this.wbUrls = wbUrls;
        this.ejbDescImpls = new HashSet<EjbDescriptor<?>>();
        this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();

        for(com.sun.enterprise.deployment.EjbDescriptor next : ejbs) {

            EjbDescriptorImpl wbEjbDesc = new EjbDescriptorImpl(next);
            ejbDescImpls.add(wbEjbDesc);

        }

    }

    public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
        return beanDeploymentArchives;
    }

    public Collection<Class<?>> getBeanClasses() {
        return wbClasses;
    }

    public Collection<URL> getBeansXml() {
        return wbUrls;
    }

    /**
    * Gets a descriptor for each EJB
    *
    * @return the EJB descriptors
    */
    public Collection<EjbDescriptor<?>> getEjbs() {

       return ejbDescImpls;
    }

    public EjbDescriptor getEjbDescriptor(String ejbName) {
        EjbDescriptor match = null;

        for(EjbDescriptor next : ejbDescImpls) {
            if( next.getEjbName().equals(ejbName) ) {
                match = next;
                break;
            }
        }

        return match;
    }

    public ServiceRegistry getServices() {
        if (null == simpleServiceRegistry) {
            simpleServiceRegistry = new SimpleServiceRegistry();
        }
        return simpleServiceRegistry;
    }

    public String getId() {
        return id;
    }

    public String toString() {
        String val = "ID: "+getId()+" CLASSES: "+getBeanClasses()+"\n"; 
        Collection <BeanDeploymentArchive> bdas = getBeanDeploymentArchives();
        Iterator iter = bdas.iterator();
        while (iter.hasNext()) {
            BeanDeploymentArchive bda = (BeanDeploymentArchive)iter.next();
            val += "   ID: "+bda.getId()+" CLASSES: "+bda.getBeanClasses();
        }
        return val;
    }

}
