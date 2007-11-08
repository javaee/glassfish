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



/**

 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.

 *

 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,

 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.

 * All rights reserved.

 */

package com.sun.enterprise.config.impl;



import java.io.Serializable;

import java.util.HashMap;

import java.util.Set;

import java.util.Iterator;



import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.util.LoggerHelper;
import com.sun.enterprise.config.util.ConfigXPathHelper;
import com.sun.enterprise.config.ConfigException;
import java.io.IOException;


/**

 * A configuration change for an element. Holds xpath, list of changed 

 * attributes, their old and new values.

 */

public class ConfigAddImpl extends ConfigChangeImpl implements com.sun.enterprise.config.ConfigAdd, Serializable {



    private ConfigBean cb;

    private String name;

    

    public String getConfigChangeType() {

        return TYPE_ADD;

    }

    

    public ConfigAddImpl(String parentXpath, String childXpath, String name, ConfigBean cb) {

        this.xpath = childXpath;

        this.cb = cb;

        this.name = name;

        this.parentXpath = parentXpath;

    }
    
    public ConfigAddImpl(ConfigContext ctx, String xpath) throws ConfigException {
        //this.cb = (ConfigBean) ctx.getRootConfigBean().clone();
        this.cb = (ConfigBean) ctx.getRootConfigBean();
        this.xpath = xpath;
        this.name = ConfigXPathHelper.getLastNodeName(xpath); 
        this.parentXpath = ConfigXPathHelper.getParentXPath(xpath); 
    }  

    public ConfigBean getConfigBean() {

        return cb;

    }

    

    public String getName() {

        return name;

    }

    /**
     * Serializes this object in a synchronized block. This is 
     * necessary since schema2beans is not thread safe. 
     *
     * Refer to bug: 6177778
     */
    private void writeObject(java.io.ObjectOutputStream out) 
            throws IOException {

        synchronized (this) {
            out.writeObject(cb);
            out.writeObject(name);
        }
    }

    /**
     * De-serializes this object in a synchronized block. This is 
     * necessary since schema2beans is not thread safe. 
     *
     * Refer to bug: 6177778
     */
    private void readObject(java.io.ObjectInputStream in) 
            throws IOException, ClassNotFoundException {

        synchronized (this) {
            cb = (ConfigBean) in.readObject();
            name = (String) in.readObject();
        }
    }

    public String toString() {

        String ret = cb.toString() + ":add to xpath=" + getParentXPath() +"\n"

                        + "childXPath = " + getXPath();

        return ret;

    }    

}

