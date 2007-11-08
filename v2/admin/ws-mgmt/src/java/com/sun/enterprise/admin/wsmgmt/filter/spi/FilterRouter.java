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

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This class routes a web service management funtion to an appropriate 
 * filter.
 */
public class FilterRouter {

    /**
     * Applies filter for an endpoint during a particular stage of its
     * processing.
     */
    public void applyFilters(String stage, FilterContext context) {

        if ( context == null) {
            return;
        }

        String endpoint = context.getFullyQualifiedName();

        FilterRegistry fm = FilterRegistry.getInstance();

        if ( fm.isManaged(endpoint) == false) {
            return;
        }
        
        List filters = fm.getFilters(stage, endpoint); 
        if (filters == null) {
            return;
        }
        ListIterator filterItr = filters.listIterator();
        while ( filterItr.hasNext() ) {
            Filter f = (Filter) filterItr.next();
            try {
                f.process(stage, endpoint, context);
            } catch (Exception e) {
                // log a fine message
                _logger.fine("Filter execution for endpoint " + endpoint + " failed: " + e.getMessage());
            }
        }
    }

    public static synchronized FilterRouter getInstance() {
        if ( fr == null) {
            fr = new FilterRouter();
        }
        return fr;
    }

    private static FilterRouter fr = new FilterRouter();

    private FilterRouter() {}
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(FilterRouter.class);


}
