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
 * ValidateeImpl.java        August 18, 2003, 4:58 PM
 */

package com.sun.enterprise.tools.common.validation.impl;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;


import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BaseProperty;

import com.sun.enterprise.tools.common.validation.Constants;
import com.sun.enterprise.tools.common.validation.util.BundleReader;
import com.sun.enterprise.tools.common.validation.util.Utils;
import com.sun.enterprise.tools.common.validation.Validatee;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ValidateeImpl implements Validatee {
    /* A class implementation comment can go here. */
    private BaseBean baseBean = null;
    
    private Utils utils = null;
    
    /** Creates a new instance of ValidateeImpl */
    public ValidateeImpl(Object baseBean){
        this.baseBean = (BaseBean)baseBean;
        utils = new Utils();
    }


    public ArrayList getElementNames(){
        ArrayList elements = new ArrayList();
        BaseProperty[] baseProperty = baseBean.listProperties();
        int size = baseProperty.length;
        for(int i=0; i<size; i++){
            elements.add(baseProperty[i].getName());
            ///String format = 
            ///    BundleReader.getValue("Name_Value_Pair_Format");     //NOI18N
            ///Object[] arguments = new Object[]{"Name",                //NOI18N
            ///    baseProperty[i].getName()};
            ///System.out.println(MessageFormat.format(format, arguments));
            ///arguments = 
            ///    new Object[]{"FullName", baseProperty[i].getFullName()};//NOI18N
            ///System.out.println(MessageFormat.format(format, arguments));
         }
        return elements;
    }


    public ArrayList getElementDtdNames(){
        ArrayList elements = new ArrayList();
        BaseProperty[] baseProperty = baseBean.listProperties();
        int size = baseProperty.length;
        for(int i=0; i<size; i++){
            elements.add(baseProperty[i].getDtdName());
            ///String format = 
            ///    BundleReader.getValue("Name_Value_Pair_Format");     //NOI18N
            ///Object[] arguments = new Object[]{"Dtd Name",            //NOI18N
            ///    baseProperty[i].getDtdName()};
            ///System.out.println(MessageFormat.format(format, arguments));         
        }
        return elements;
    }


    public boolean isIndexed(String elementName){
        BaseProperty baseProperty = baseBean.getProperty(elementName);
        boolean returnValue =  false;
        if(null != baseProperty) {
            returnValue =  baseProperty.isIndexed();
        } else {
            String format = 
                BundleReader.getValue("Error_does_not_exists");         //NOI18N
            Object[] arguments =    
                new Object[]{"Property", elementName};                  //NOI18N
            String message = MessageFormat.format(format, arguments);
            assert false : message;
        }
        return returnValue;
    }


    public int getElementCardinal(String elementName){
        BaseProperty baseProperty = baseBean.getProperty(elementName);
        int returnValue = -1;
        if(null != baseProperty) {
            returnValue =  baseProperty.getInstanceType();
        } else {
            String format = 
                BundleReader.getValue("Error_does_not_exists");         //NOI18N
            Object[] arguments =    
                new Object[]{"Property", elementName};                  //NOI18N
            String message = MessageFormat.format(format, arguments);
            assert false : message;
        }
        return returnValue;
    }


    public int getCardinal(){
        String name = baseBean.name();
        BaseBean parent = baseBean.parent();
        BaseProperty baseProperty = parent.getProperty(name);
        return baseProperty.getInstanceType();
    }


    public boolean isBeanElement(String elementName){
        BaseProperty baseProperty = baseBean.getProperty(elementName);
        boolean returnValue = false;
        if(null != baseProperty) {
            returnValue =  baseProperty.isBean();
        } else {
            String format = 
                BundleReader.getValue("Error_does_not_exists");         //NOI18N
            Object[] arguments =    
                new Object[]{"Property", elementName};                  //NOI18N
            String message = MessageFormat.format(format, arguments);
            assert false : message;
        }
        return returnValue;
    }


    public String getXPath(){
        ///String format = BundleReader.getValue("Name_Value_Pair_Format");//NOI18N
        ///Object[] arguments = new Object[]{"Name", baseBean.name()};  //NOI18N
        ///System.out.println(MessageFormat.format(format, arguments));
        ///arguments = new Object[]{"FullName", baseBean.fullName()};   //NOI18N
        ///System.out.println(MessageFormat.format(format, arguments));

        BaseBean bean =  baseBean;
        BaseBean parentBean = null;
        String xpath = bean.dtdName();
        ///boolean root =  bean.isRoot();
        boolean root = isRootElement(bean);
        parentBean = bean.parent();
        while(!root){
            xpath = parentBean.dtdName() + Constants.XPATH_DELIMITER + xpath;
            bean = parentBean;
            parentBean = bean.parent();
            ///root = bean.isRoot();
            root = isRootElement(bean);
        }
        xpath = Constants.XPATH_DELIMITER + xpath;
        return xpath;
    }


    public String getIndexedXPath() {
        BaseBean bean =  baseBean;
        BaseBean parentBean = null;
        String xpath = bean.dtdName();
        int index = getIndex(baseBean);
        if(index != -1){
            xpath = utils.getIndexedName(xpath,  index);
        }

        boolean root = isRootElement(bean);
        parentBean = bean.parent();

        String name = null;
        while(!root){
            name = parentBean.dtdName();
            index = getIndex(parentBean);
            if(index != -1) {
                name = utils.getIndexedName(name, index);
            }
            xpath = name + Constants.XPATH_DELIMITER + xpath;
            bean = parentBean;
            parentBean = bean.parent();
            root = isRootElement(bean);
        }
        xpath = Constants.XPATH_DELIMITER + xpath;
        return xpath;
    }


    boolean isRootElement(BaseBean bean){
        BaseBean parent = bean.parent();
        boolean root;
        if(parent.name().equals(bean.name())){
            root = true;
        } else {
            root = false;
        }
        return root;
    }


    int getIndex(BaseBean baseBean){
        int index = -1;
        boolean root = isRootElement(baseBean);
        if(!root){
            String name = baseBean.name();
            BaseBean parent = baseBean.parent();
            if(parent != null) {
                index = parent.indexOf(name, baseBean);
            }
        }
        return index;
    }    
    
    
    public Object getElement(String elementName, int index) {
            return utils.getElement(elementName, index, baseBean);
    }
    
    public Object getElement(String elementName) {
        return utils.getElement(elementName, baseBean);
    }

    public Object[] getElements(String elementName) {
        return utils.getElements(elementName, baseBean);
    }
    
    public Method getMethod(String methodName) {
        return utils.getMethod(utils.getClass(baseBean), methodName);
    }

    public Object invoke(Method method) {
        return utils.invoke(baseBean, method);
    }
}
