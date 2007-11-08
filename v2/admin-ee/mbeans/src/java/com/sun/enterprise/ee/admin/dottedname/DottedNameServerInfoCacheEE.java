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

package com.sun.enterprise.ee.admin.dottedname;

import com.sun.enterprise.admin.dottedname.DottedNameServerInfoCache;
import com.sun.enterprise.admin.dottedname.DottedNameLogger;

import java.util.Set;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jun 11, 2004
 * @version $Revision: 1.1.1.1 $
 */
public class DottedNameServerInfoCacheEE extends DottedNameServerInfoCache implements DottedNameServerInfoEE{
    Set			mUnclusteredServerNames;
    DottedNameServerInfoEE mSrc;
    private HashMap mAppsToServers;
    private HashMap mResToServers;

    public DottedNameServerInfoCacheEE(final DottedNameServerInfoEE src) {
        super(src);
        mSrc = src;
        mUnclusteredServerNames	= Collections.EMPTY_SET;
        mAppsToServers =  new HashMap();
        mResToServers =new HashMap();
    }

    public synchronized void refresh(){
        try {
            resetCache();
            mUnclusteredServerNames = mSrc.getUnclusteredServerNames();
            mapAppsToServers();
            mapResToServers();
            super.refresh();
        } catch (Exception e) {
            DottedNameLogger.logException( e );
        }
    }

    private void resetCache() {
        mUnclusteredServerNames	= Collections.EMPTY_SET;
        mAppsToServers.clear();
        mResToServers.clear();
    }

    public synchronized Set getUnclusteredServerNames(){
        return mUnclusteredServerNames;
    }

    public Set getApplicationNamesForServer(final String serverName) throws Exception {
        return (Set) mAppsToServers.get(serverName);
    }

    public Set getResourceNamesForServer(final String serverName) throws Exception {
        return (Set) mResToServers.get(serverName);
    }

    private void mapAppsToServers() throws Exception {
        final Iterator iter  = mUnclusteredServerNames.iterator();
        String name;
        Set apps;
        while(iter.hasNext()){
            name = (String)iter.next();
            apps = mSrc.getApplicationNamesForServer(name);
            if(apps != null){
                mAppsToServers.put(name, apps);
            }
        }
    }

    private void mapResToServers() throws Exception {
        final Iterator iter  = mUnclusteredServerNames.iterator();
        String name;
        Set res;
        while(iter.hasNext()){
            name = (String)iter.next();
            res = mSrc.getResourceNamesForServer(name);
            if(res != null){
                mResToServers.put(name, res);
            }
        }
    }
}
