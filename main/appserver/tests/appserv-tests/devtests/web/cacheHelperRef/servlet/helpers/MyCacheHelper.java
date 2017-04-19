/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * Test Custom Servlet Caching
 * Author Davis Nguyen
 */

package helpers;

import com.sun.appserv.web.cache.*;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class MyCacheHelper implements CacheHelper {

    // Values is initialized  to CacheAble
    private static String cacheKey="myKey"; 
    private static boolean isCacheAble=true;
    private static boolean isRefreshNeeded=false;
    private static int timeOut=20;
  
    
    public void init (ServletContext context, Map props) throws Exception {
        System.out.println("myCacheHelper2:init");
        // Nothing to initialize
    }
    
    public void destroy() throws Exception {
        System.out.println("myCacheHelper2:exit");
    }
    
    public String getCacheKey(HttpServletRequest req) {
        System.out.println("myCacheHelper2:getCacheKey");
        String key = req.getParameter("cacheKey");
        if (cacheKey != null) // key not null
            return key;
        else
            // If user do not enter a key, the default value is "myKey"
        return cacheKey;
    }
    
    public int getTimeout(HttpServletRequest req) {
        String timeOutStr = null;
        int time = 0;
        System.out.println("myCacheHelper2:getTimeout");
        timeOutStr = req.getParameter("timeOut");
        if (timeOutStr != null){
            try {
                time = Integer.parseInt(timeOutStr);
            } catch (NumberFormatException nfe) {
                System.out.println("Number Format Exception Occurs: "
                                   + nfe.getMessage());
            }
            // Negative time out value treat as 0 
            if (time > 0)
                return time;
            else return 0;
        }
        // if user do not enter time out, use default timeOut value
        else return timeOut;
    }
    
    public boolean isCacheable(HttpServletRequest req) {
        String isCacheAbleStr = null;
        System.out.println("myCacheHelper2:isCacheable");
        isCacheAbleStr = (String)req.getParameter("isCacheAble");
        // Check if isCacheAble null 
        if ((isCacheAbleStr != null)
                && (isCacheAbleStr.compareTo("false") != 0))
            return false;
        else return isCacheAble;
    }
    
    public boolean isRefreshNeeded(HttpServletRequest req) {
        String isRefreshNeededStr = null;
        System.out.println("myCacheHelper2:isRefreshNeeded");
        isRefreshNeededStr = req.getParameter("isRefreshNeeded");
        if ((isRefreshNeededStr != null)
                && (isRefreshNeededStr.compareTo("true") !=0))
            return true;
        else return isRefreshNeeded;
    }   
}

