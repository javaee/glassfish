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

package com.sun.enterprise.config.serverbeans.validation;


import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;

// Logging
import com.sun.enterprise.util.LocalStringManagerImpl;


public class ValidationContext {
    public Result result;
    public Object value;
    public Object classObject;
    public String beanName;
    public ConfigContext context;
    public String name;
    public String choice;
    public LocalStringManagerImpl smh;
    public String primaryKeyName;
    public ValidationDescriptor validationDescriptor;
    //-------------- prepared in GenericValidator
    public String       attrName;
    public Object       attrValue;
    
    


    public ValidationContext(Result result, Object value, Object classObject, String beanName, ConfigContext context, String name, String choice, String primaryKeyName, LocalStringManagerImpl smh, ValidationDescriptor validationDescriptor) {
        this.result =  result;
        this.value = value;
        this.beanName = beanName;
        this.context = context;
        this.name = name;
        this.choice = choice;
        this.smh = smh;
        this.primaryKeyName = primaryKeyName;
        this.classObject = classObject;
        this.validationDescriptor = validationDescriptor;
        attrName = null;
    }
    
    public void setAttrName(String name) {
        attrName = name;
    }
    
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }
    public boolean isUPDATEorSET()
    {
        return (choice != null && choice.equals(StaticTest.UPDATE) || choice.equals(StaticTest.SET));
    }
    public boolean isUPDATE()
    {
        return (choice != null && 
               (choice.equals(StaticTest.UPDATE) ||
                (choice.equals(StaticTest.SET) && 
                 ((value instanceof String)) || (value instanceof String[]))) );
    }
    public boolean isSET()
    {
        return (choice != null && choice.equals(StaticTest.SET) && (value instanceof ConfigBean) );
    }
    public boolean isADD()
    {
        return (choice != null && choice.equals(StaticTest.ADD));
    }
    public boolean isDELETE()
    {
        return (choice != null && choice.equals(StaticTest.DELETE));
    }
    public boolean isVALIDATE()
    {
        return (choice != null && choice.equals(StaticTest.VALIDATE));
    }
    public ConfigBean getTargetBean()
    {
        if ((isSET()||isADD()||isDELETE()||isVALIDATE()) && (value instanceof ConfigBean) )
            return (ConfigBean)value;
        if ((isUPDATE()) && (classObject instanceof ConfigBean))
            return (ConfigBean)classObject;
        return null;
    }
    public ConfigBean getParentBean()
    {
        if ((isSET()||isADD()) && (classObject instanceof ConfigBean) )
            return (ConfigBean)classObject;
        ConfigBean self = getTargetBean();
        try {
            return (ConfigBean)self.parent();
        } catch(Throwable t) {}
        return null;
    }
        
    public NameListMgr getNameListMgr()
    {
        return validationDescriptor.domainMgr._nameListMgr;
    }
}    