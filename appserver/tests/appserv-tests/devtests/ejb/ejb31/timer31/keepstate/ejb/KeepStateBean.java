/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1asdev.ejb31.timer.keepstate;

import javax.ejb.*;
import java.util.List;
import java.util.ArrayList;

@Singleton
public class KeepStateBean implements KeepStateIF {
    private List<String> infos = new ArrayList<String>();
    
    /**
     * keepstate is passed from build.xml to appclient as application args, and to this 
     * business method. The test EAR app is deployed once first, then KeepStateIF.INFO
     * is modified, rebuild, and redeployed.  If redeployed with keepstate true, no new
     * auto timers are created, and existing timers are carried over.  So these timers
     * still have the old timer.getInfo(), which is different than the current KeepStateIF.INFO.
     * If redeployed with keepstate false, old timers are destroyed and new auto timers are
     * created, and their timer.getInfo() will equal to current KeepStateIF.INFO.
     */
    public String verifyTimers(boolean keepState) throws Exception {
        String result = "keepstate: " + keepState + ", current INFO: " + INFO + ", timer infos: " + infos;
        for(String s : infos) {
            if(keepState && s.equals(INFO)) {
                throw new Exception(result);
            }
            if(!keepState && !s.equals(INFO)) {
                throw new Exception(result);
            }
        }
        return result;
    }

    @Schedule(second="*", minute="*", hour="*", info=INFO)
    private void timeout(Timer t) {
        infos.add((String) t.getInfo());
        System.out.println("In timeout method for timer with info: " + t.getInfo());
    }
}
