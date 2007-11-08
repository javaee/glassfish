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

import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.management.ObjectName;
import com.sun.mfwk.MfDelegate;
import com.sun.mfwk.agent.appserv.delegate.DefaultDelegate;
import javax.management.MBeanServerConnection;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.logging.LogDomains;

import java.io.IOException;
import javax.management.MalformedObjectNameException;

/**
 * Factory class to convert the attribute mappings to a delegate.
 */
class DelegateFactory {

    /**
     * Constructor.
     *
     * @param  mbean  xml element representing an mbean
     * @param  delegate  mbean delegate
     * @param  ctx  modeler runtime context
     * @param  mbs  mbean server connection
     */
    DelegateFactory(Element mbean, MfDelegate delegate, ModelerContext ctx, 
            MBeanServerConnection mbs) {

        _mbean = mbean;
        _delegate = delegate;
        _context = ctx;
        _mbs = mbs;
    }

    /**
     * Returns a delegate object 
     *
     * @return  newly constructed delegate object
     * @throws  MalformedObjectNameException  if object name is incorrect
     * @throws  IOException  communication problem while talking to the 
     *          MBean server
     * @throws  NoSuchFieldException  if token in object name does not have 
     *          a corresponding value in the environment map
     */
    Object create() throws MalformedObjectNameException, IOException, 
            NoSuchFieldException {

        DefaultDelegate dd = null;

        if (_delegate instanceof DefaultDelegate) {
            dd = (DefaultDelegate) _delegate;

            LogDomains.getLogger().fine(
                "Delegate is instanceof DefaultDelegate");
        } else {
            LogDomains.getLogger().fine(
                "Delegate is NOT instanceof DefaultDelegate: " + _delegate);

            dd = new DefaultDelegate();
        } 

        NodeList attr = ConfigReader.getMBeanAttrMappings(_mbean);
        int idx = attr.getLength();

        for (int i=0; i<idx; i++) {
            Element e = (Element) attr.item(i);

            String mfAttr = e.getAttribute("id").trim();
            String asAttr = e.getAttribute("name").trim();

            // proxy object name
            String asObjName = e.getAttribute("type").trim();
            // tokenize 
            String tokenizedON = 
                ObjectNameHelper.tokenize(asObjName, _context.getTokens());
            // replace patterns with concrete object name
            ObjectName on = ObjectNameHelper.getObjectName(tokenizedON, _mbs);

            // adds the mapping for an attribute
            dd.addMappingEntry(mfAttr, asAttr, on);
        }

        // sets the name of the server instance
        String instanceName = _context.getServerName();
        dd.setServerName(instanceName);

        return dd;
    }

    // ---- Instances - Private -------------------------
    private Element _mbean = null;
    private MfDelegate _delegate = null;
    private ModelerContext _context = null;
    private MBeanServerConnection _mbs = null;
}
