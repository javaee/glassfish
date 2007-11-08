/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.modeler;

import com.sun.mfwk.CMM_MBean;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import javax.management.ObjectName;
import com.sun.mfwk.agent.appserv.mapping.MappingQueryService;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

import java.io.IOException;
import java.util.Hashtable;
import com.sun.mfwk.agent.appserv.mapping.AS_ObjectNameNotFoundException;

/**
 * Main driver class for mbean instrumentation.
 */
public class MBeanModeler {
    
    /**
     * Creates a new instance of mbean modeler
     *
     * @param  ctx  modeler context
     */
    public MBeanModeler(ModelerContext ctx) {

        _context = ctx;
    }

    /**
     * Models an Application Server mbean with the given mapping service.
     *
     * @param   on   application server mbean object name
     * @param  mapping  mapping service that provides mapping information 
     *         between application server and CMM mbean
     *
     * @throws IOException  if an i/o error
     */
    public void model(ObjectName on, MappingQueryService mapping) 
            throws IOException, AS_ObjectNameNotFoundException {

        if ( (on == null) || (mapping == null) ) {
            throw new IllegalArgumentException();
        }

        NodeList cmmNodeList = mapping.getCMM_Mbeans(on.getCanonicalName());
        MBeanFactory mf = new MBeanFactory(_context);

        int size = cmmNodeList.getLength();
        for (int i=0; i<size; i++) {
            Element m = (Element) cmmNodeList.item(i);
            try {
                mf.model(on, m, mapping); 
            } catch (Exception e) {
                LogDomains.getLogger().log(Level.WARNING, 
                    "Error while modeling mbean " + on, e); 
            }
        }
    }
    
    /**
     * Starts the template processing
     *
     * @param  template  xml file that describes the mbeans
     * @param  dLocation  default location of the template
     *
     * @throws IOException  if an i/o error
     */
    public void load(String template, String dLocation) 
            throws IOException {

        NodeList list = ConfigReader.getMBeans(template, dLocation);
        int size = list.getLength();
        MBeanFactory mf = new MBeanFactory(_context);
        Logger l = LogDomains.getLogger();

        for (int i=0; i<size; i++) {
            Element m = (Element) list.item(i);

            try {
                CMM_MBean mbean = mf.create(m); 

                if ((l != null) && (mbean != null)) {
                    l.fine("Created MBean: " + mbean.getName());
                }

            } catch (Exception e) {
                LogDomains.getLogger().log(Level.WARNING, 
                    "Error while instrumenting mbean", e); 
            }
        }
    }

    static public Map getDelegateMap() {
        if (MBeanFactory.map == null) {
            MBeanFactory.map = new Hashtable();
        }
        return MBeanFactory.map;
    }

    // ---- PRIVATE - VARIABLES -------------------------------------
    private ModelerContext _context;
}
