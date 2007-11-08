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
package com.sun.enterprise.admin.wsmgmt.filter.spi;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * This class interacts with JAX RPC Monitoring SPI and registers Global
 * Listener to get callbacks. 
 */
public class FilterRegistry {

    public synchronized void registerFilter(String stage, String endpoint, 
        Filter filter) {

        List fl = (List) filters.get( stage + DELIM + endpoint);
        if ( fl == null ) {
            fl = new ArrayList();
        }
        fl.add(filter);
        filters.put(stage + DELIM + endpoint, fl); 
        setManaged(endpoint);
    }

    public void unregisterFilter(String stage, String endpoint, Filter filter) {
        List fl = (List) filters.get( stage + DELIM + endpoint);
        if ( fl == null ) {
            throw new IllegalArgumentException(" No registration exists for " +
                stage + DELIM + endpoint );
        }
        fl.remove(fl.indexOf(filter));
        if ( filters.isEmpty() ) {
            // this is the last filter, set this web service as un-managed
            setUnManaged(endpoint);
        }
    }

    public void unregisterFilterByName(String stage, String endpoint, 
        String filtername) {

        List fl = (List) filters.get( stage + DELIM + endpoint);
        if ( fl == null ) {
            throw new IllegalArgumentException(" No registration exists for " +
                stage + DELIM + endpoint );
        }
        Iterator fli = fl.iterator();
        while (fli.hasNext()) {
            Filter f = (Filter) fli.next();
            if (f.getName().equals(filtername)) {
                fli.remove();
                continue;
            }
        }

        if ( filters.isEmpty() ) {
            // this is the last filter, set this web service as un-managed
            setUnManaged(endpoint);
        }
    }

    public void unregisterAllFilters(String endpoint) {
        Iterator itr = filters.keySet().iterator();
        while ( itr.hasNext()) {
            String key = (String) itr.next();
            if ( key.contains( DELIM+endpoint) ) {
                itr.remove();
            }
        }
    }

    public List getFilters(String stage, String endpoint) {
        return (List) filters.get(stage + DELIM + endpoint);
    }

    // Is this method needed???
    public FilterRegistration[] getFilters(String endpoint) {
        return null;
    }

    public boolean isManaged(String endpoint) {
        if ( managedEndpoints == null)
            return false;
        else {
             Object o = managedEndpoints.get(endpoint);
             if ( o != null) {
                return true;
             } else {
                return false;
             }
        }
    }

    public void setManaged(String endpoint) {
        managedEndpoints.put(endpoint, Boolean.valueOf(true));
    }

    public void setUnManaged(String endpoint) {
        managedEndpoints.remove(endpoint);
    }

    public static synchronized FilterRegistry getInstance() {
        if ( fm == null) {
            fm = new FilterRegistry();
        }
        if ( managedEndpoints == null ) {
            managedEndpoints = new HashMap();
        }
        if ( filters == null ) {
            filters = new HashMap();
        }
        return fm;
    }

    private static FilterRegistry fm = null;

    private FilterRegistry() {}

    private static HashMap managedEndpoints = null;

    private static HashMap filters = null;

    public static final String DELIM = ":";
}
