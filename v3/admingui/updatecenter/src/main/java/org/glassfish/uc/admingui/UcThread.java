/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.uc.admingui;

import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import com.sun.pkg.client.Image;
import javax.servlet.http.HttpSession;
import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author anilam
 */

/*
 * back and let the parent set/get it
[15:31] 	KenPaulsen	getSessionMap() returns a mutable Map... not the actual session map, though (I think).
[15:31] 	KenPaulsen	So maybe the request has completed and the Map you have is no longer valid.
[15:31] 	KenPaulsen	Maybe try getting the actual httpSession object?
[15:33] 	KenPaulsen	Yes, that appears to be the case.
[15:34] 	KenPaulsen	Instead try:
[15:34] 	KenPaulsen	FacesContext.getCurrentInstance().getExternalContext().getSession()
[15:35] 	KenPaulsen	Then cast that to an HttpSession
[15:35] 	KenPaulsen	Then call getValue("key") and/or setValue("key")
[15:36] 	KenPaulsen	Sorry... getAttribute("key")/ setAttribute("key", value)
[15:36] 	KenPaulsen	The only problem you'll run into is synchronizing access to Session.

 */


public  class UcThread extends Thread {
        private HttpSession session = null;

        /*
         * Need to pas in the HttpSession instead of sessionMap.  getSessionMap() returns a mutable Map, not the actual session map.
         * The request may have completed and the Map is no longer valid when
         * it gets to the run() method.
         */
        UcThread( HttpSession s1){
            this.session = s1;
        }

        @Override
        public void run() {
            int count = -1;
            int ss = 10000;
            try{
            Thread.sleep(ss);
            }catch(Exception ex){
                //
            }
            //session.setAttribute("_updateCountMsg", GuiUtil.getMessage(UpdateCenterHandlers.BUNDLE, "msg.checkForUpdates"));
            try{
                Integer countInt = null;
                Image image = UpdateCenterHandlers.getUpdateCenterImage( (String)session.getAttribute("topDir"), true);
                countInt = UpdateCenterHandlers.updateCountInSession(image);
                session.setAttribute("_updateCountMsg", "");
                count = countInt.intValue();
                if (count == 0){
                    session.setAttribute("_updateCountMsg", GuiUtil.getMessage(UpdateCenterHandlers.BUNDLE, "msg.noUpdates"));
                }else
                if (count > 0){
                    session.setAttribute("_updateCountMsg", GuiUtil.getMessage(UpdateCenterHandlers.BUNDLE, "msg.updatesAvailable", new String[]{""+count}));
                }
            }catch(Exception ex){
                if(GuiUtil.getLogger().isLoggable(Level.FINE)){
                    ex.printStackTrace();
                }
            }
        }
    }
