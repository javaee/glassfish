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
 * BooleanStringItemListener.java
 *
 * Created on March 18, 2001, 12:24 PM
 */

package com.sun.enterprise.tools.common;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
/**
 *
 * @author  vkraemer
 * @version 
 */
public class BooleanStringItemListener implements java.awt.event.ItemListener {
    
    Object target = null;
    java.lang.reflect.Method writer = null;

        private Object args[] = { target };
    

    /** Creates new BooleanStringItemListener */
    public BooleanStringItemListener(Object target, String destName) throws  java.beans.IntrospectionException {
        this.target = target;
        writer = PropertyUtils.getWriter(target,destName);
    }
    
    public BooleanStringItemListener(Object target) {
        this.target = target;
    }

    private static final String FALSE[] =  {"false" };//NOI18N
    private static final String TRUE[] = { "true" };//NOI18N
    
    public void itemStateChanged(java.awt.event.ItemEvent itemEvent) {
        try {
            java.lang.reflect.Method lwriter = writer;
            if (null == lwriter) {
                java.awt.Component src = (java.awt.Component) itemEvent.getSource();
                lwriter = PropertyUtils.getWriter(target, src.getName());
            }
            Object args[] = FALSE;
            if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED)
                args = TRUE;
            lwriter.invoke(target, args);
        }
        catch (Throwable t) {
            Reporter.critical(t); //NOI18N
        }
    }
    
}
