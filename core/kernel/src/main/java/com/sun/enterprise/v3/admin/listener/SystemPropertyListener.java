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
package com.sun.enterprise.v3.admin.listener;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.component.PostConstruct;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import java.util.List;

/** Listens to changes made to the <code>&lt;system-property> </code> elements in domain.xml. All <code>&lt;system-property></code> elements
 *  are implemented to be Java System Properties (available via java.lang.System). Note however that there is a hierarchy
 *  in place i.e. a system-property defined on the <code>&lt;domain></code> element and then it is redefined on <code>&lt;server></code> element
 *  then the latter prevails. Thus, in case of a system-property defined like:
 *  <p>
 * <blockquote><pre>{@code
 *    <domain>
 *
 *      <server ...>
 *        <system-property name="PORT" value="1234">
 *      </server>
 *    ....
 *    </domain>
 * }
 * </pre>
 * </blockquote>
 * <p>
 * and then you defined a system-property on the target <code>domain</code> then this class should ignore it since the Java
 * system property that should be in effect is the one of element <code> server </code>.
 * <p> This class takes care of all those combinations for both create and delete cases and sets/clears the Java system
 * property accordingly.
 *
 * @author Kedar Mhaswade (km@dev.java.net)
 */
@Service
public class SystemPropertyListener implements ConfigListener, PostConstruct {

    /*
    Implementation note: I still think that this kind of code is an unfortunate result of how config system handles
    the dynamic reconfiguration. With current system, we need to have ConfigListener instances that listen to and
    get notified when a child element's or grand-child element's attribute is changed! An alternate design would have
    been annotating @Configured interfaces with listener classes, so that when a dynamic proxy is created for an
    @Configured interface, the listeners can listen for changes to it. IMO, a particular element's listener should get notified
    only when its attribute/text gets changed, its child gets created or its child gets deleted. Currently, you get
    notified even when something happens to your grandchild element or child element's attributes.

    The other problem with current system is that of creating the instances of ConfigListeners. All over the codebase,
    you'll see ConfigListener instances created explicitly. It's not clear when these instances should be created.
    And hence, the ConfigListener interfaces should have come into existence (and disappeared) along with the bean (proxy) instances.

    Date: 10/10/2009.
    */

    Logger logger = LogDomains.getLogger(SystemPropertyListener.class, LogDomains.ADMIN_LOGGER);

    @Inject
    private volatile Domain domain; //note: this should be current, and does contain the already modified values!
    
    @Override
    public void postConstruct() {
    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new Changed()
        {
            @Override
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                if (changedType == SystemProperty.class && type == TYPE.ADD) {  //create-system-properties
                    SystemProperty sp = (SystemProperty) changedInstance;
                    if (sp.getParent() instanceof Domain) {
                        return addToDomain(sp);
                    } else {
                        //this has to be instance of Server
                        return addToServer(sp);
                    }
                }
                if (changedType == SystemProperty.class && type == TYPE.REMOVE) { //delete-system-property
                    SystemProperty sp = (SystemProperty) changedInstance;
                    if (sp.getParent() instanceof Domain) {
                        return removeFromDomain(sp);
                    } else {
                        //this has to be instance of Server
                        return removeFromServer(sp);
                    }
                }
                if (changedType == SystemProperty.class && type == TYPE.CHANGE) { //set on the dotted name e.g. servers.server.server.system-property.foo.value
                    SystemProperty sp = (SystemProperty) changedInstance;
                    if (sp.getParent() instanceof Domain) {
                        return addToDomain(sp);
                    } else {
                        //this has to be instance of Server
                        return addToServer(sp);
                    }
                }
                return null; // know nothing about other cases, hope there aren't any
            }
        }, logger);
    }

    private NotProcessed removeFromServer(SystemProperty sp) {
        System.clearProperty(sp.getName());
        return null; //processed
    }

    private NotProcessed removeFromDomain(SystemProperty sp) {
        if(!serverHas(sp))
            System.clearProperty(sp.getName()); //if server overrides it anyway, this should be a noop
        return null; //processed
    }

    private NotProcessed addToServer(SystemProperty sp) {
        System.setProperty(sp.getName(), sp.getValue());
        return null; //processed
    }

    private NotProcessed addToDomain(SystemProperty sp) {
        if (!serverHas(sp))
            System.setProperty(sp.getName(), sp.getValue()); //if server overrides it anyway, this should be a noop
        return null; //processed
    }

    private boolean serverHas(SystemProperty sp) {
        Server s = domain.getServerNamed(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME);
        List<SystemProperty> ssps = s.getSystemProperty();
        if (ssps != null) {
            for (SystemProperty candidate : ssps) {
                if (candidate.getName().equals(sp.getName()))
                    return true;
            }
        }
        return false;
    }
}


