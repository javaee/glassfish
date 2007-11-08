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
 * MVCUtils.java
 *
 * Created on March 18, 2001, 1:30 PM
 */

package com.sun.enterprise.tools.common.ui;

import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;

import javax.swing.JTable;
import javax.swing.JList;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
import com.sun.enterprise.tools.common.util.diagnostics.StackTrace;

import com.sun.enterprise.tools.common.BooleanStringItemListener;
import com.sun.enterprise.tools.common.Validator;

import com.sun.enterprise.tools.common.PropertyUtils;
/**
 *
 * @author  vkraemer
 * @version 
 */
public class MVCUtils {

    /** Creates new MVCUtils */
    private MVCUtils() {
    }

    private static final String TRUE = "true";  //NOI18N
    private static final String EMPTY = ""; //NOI18N
    
    public static void linkBooleanStringElement(Object model, javax.swing.AbstractButton view, String propName) 
        throws java.beans.IntrospectionException {
        view.addItemListener(new BooleanStringItemListener(model, propName));
        view.setSelected(getStringElementValue(model, propName).equals(TRUE));
    }
    
    public static void linkBooleanStringElement(Object model, javax.swing.AbstractButton view) 
        throws java.beans.IntrospectionException {
        String propName = view.getName();
        if (null != propName) {
            linkBooleanStringElement(model,view,propName);
        }
        else
            Reporter.critical("the name of the " + model + " is null"); //NOI18N
    }
    
    public static void linkStringElement(Object model, javax.swing.text.JTextComponent view, String propName) 
        throws java.beans.IntrospectionException {
        view.addKeyListener(new StringItemListener(model,propName));
        view.setText(getStringElementValue(model,propName));
    }
    
    public static void linkStringElement(Object model, javax.swing.text.JTextComponent view) 
        throws java.beans.IntrospectionException {
        String propName = view.getName();
        if (null != propName) {
            linkStringElement(model,view,propName);
        }
        else
            Reporter.critical("the name of the " + model + " is null"); //NOI18N
    }
    
    public static void linkIntStringElement(Object model, javax.swing.text.JTextComponent view, String propName) 
        throws java.beans.IntrospectionException {
        view.addKeyListener(new IntStringItemListener(model,propName));
        view.setText(getStringElementValue(model,propName));
    }

    public static void linkIntStringElement(Object model, 
        javax.swing.text.JTextComponent view, String propName, int defaultValue) 
        throws java.beans.IntrospectionException {
        view.addKeyListener(new IntStringItemListener(model,propName));
        String tval = getStringElementValue(model,propName);
        view.setText(getStringElementValue(model,propName));
        if (null == tval || tval.equals(EMPTY))
            view.setText(EMPTY + defaultValue);
    }

        public static void linkIntStringElement(Object model, 
        javax.swing.text.JTextComponent view, String propName, int min, int max) 
        throws java.beans.IntrospectionException {
        view.addKeyListener(new IntStringItemListener(model,propName, min, max));
//        String tval = getStringElementValue(model,propName);
        view.setText(getStringElementValue(model,propName));
//        if (null == tval || tval.equals(""))
//            view.setText("" + defaultValue);
    }

        public static void linkIntStringElement(Object model, 
        javax.swing.text.JTextComponent view, String propName, int defaultValue,
        int min, int max) 
        throws java.beans.IntrospectionException {
        view.addKeyListener(new IntStringItemListener(model,propName, min, max));
        String tval = getStringElementValue(model,propName);
        view.setText(getStringElementValue(model,propName));
        if (null == tval || tval.equals(EMPTY))
            view.setText(EMPTY + defaultValue);
    }

    // this should work for a JList and a JTable, though they don't implement a shared interface for these two
    // routines, but the two routines do implement the same interface....
    public static void selectionSensitive(Object hasListSelectionModel, java.awt.Component sensitiveItem) {
        try {
            Method getter = hasListSelectionModel.getClass().getMethod("getSelectionModel",null); //NOI18N
            javax.swing.ListSelectionModel lsm[] = new javax.swing.ListSelectionModel[1];
            Class lsmArgs[] = { javax.swing.ListSelectionModel.class };
            if (null != getter) {
                lsm[0] = (javax.swing.ListSelectionModel) getter.invoke(hasListSelectionModel,null);
                if (null == lsm[0]) {
                    lsm[0] = new javax.swing.DefaultListSelectionModel();
                    Method putter = hasListSelectionModel.getClass().getMethod("setSelectionModel", lsmArgs); //NOI18N
                    putter.invoke(hasListSelectionModel,lsm);
                }
                lsm[0].addListSelectionListener(new SelectionActivator(sensitiveItem, hasListSelectionModel));
            }
        }
        catch (Throwable t) {
            Reporter.critical(new StackTrace(t)); //NOI18N
        }
    }
    
    public static void validationSensitive(javax.swing.text.JTextComponent f, java.awt.Component sensitive, Validator v) {
        StringValidationListener svl = 
            new StringValidationListener(sensitive,v);
        
        f.addKeyListener(svl);        
    }
    
    
    private static String getStringElementValue(Object model, String propName) {
        String retVal = EMPTY;
        Method reader = null;
        try {
            PropertyDescriptor destPd = new PropertyDescriptor(propName, model.getClass());
            reader = destPd.getReadMethod();
            Object tmp = reader.invoke(model,null);
            if (null != tmp)
                retVal =tmp.toString();
        }
        catch (Throwable t) {
            Reporter.critical(new StackTrace(t)); //NOI18N
        }
        return retVal;
    }
    
    /*private static String getIntStringElementValue(Object model, String propName) {
        String retVal = "";
        Method reader = null;
        try {
            PropertyDescriptor destPd = new PropertyDescriptor(propName, model.getClass());
            reader = destPd.getReadMethod();
            Object tmp = reader.invoke(model,null);
            if (null != tmp)
                retVal =tmp.toString();
        }
        catch (Throwable t) {
            Reporter.critical(new StackTrace(t)); //NOI18N
        }
        return retVal;
    }*/

    static class StringValidationListener extends java.awt.event.KeyAdapter {
        java.awt.Component sensitive;
        Validator validation;
        public StringValidationListener(java.awt.Component sensitive, Validator validation) {
            this.validation = validation;
            this.sensitive = sensitive;
        }
        
        public void keyReleased(java.awt.event.KeyEvent ev) {
            javax.swing.text.JTextComponent tc = (javax.swing.text.JTextComponent) ev.getSource();
            if (validation.isValid(tc.getText()))
                sensitive.setEnabled(true);
            else
                sensitive.setEnabled(false);
        }

    }
    
    static class SelectionActivator implements javax.swing.event.ListSelectionListener {
        java.awt.Component comp = null;
        Object foo;
        
        public SelectionActivator (java.awt.Component comp, Object foo) {
            this.comp = comp;
            this.foo = foo;
        }
        
        public void valueChanged(javax.swing.event.ListSelectionEvent lse) {
            Object src = lse.getSource();
            Reporter.verbose(src); //NOI18N
            int selected = -1;
            if (foo instanceof javax.swing.JTable) {
                JTable t = (JTable) foo;
                selected = t.getSelectedRow();
            }
            else if (src instanceof JList) {
                JList l = (JList) foo;
                selected = l.getSelectedIndex();
            }
            if (-1 != selected)
                comp.setEnabled(true);
            else
                comp.setEnabled(false);
        }
    }
   
    static class StringItemListener extends java.awt.event.KeyAdapter {
        Object target = null;
        java.lang.reflect.Method writer = null;

        protected Object args[] = { target };
    

    /** Creates new BooleanStringItemListener */
        public StringItemListener(Object target, String destName) throws  java.beans.IntrospectionException {
            this.target = target;
            writer = PropertyUtils.getWriter(target,destName);
            
        }
        
        public void keyReleased(java.awt.event.KeyEvent ev) {
            try {
                //java.lang.reflect.Method lwriter = writer;
                javax.swing.text.JTextComponent src = (javax.swing.text.JTextComponent) ev.getSource();
                /*if (null == lwriter) {
                    java.awt.Component src = (java.awt.Component) itemEvent.getSource();
                    lwriter = PropertyUtils.getWriter(target, src.getName());
                }*/
                //Object args[] = FALSE;
                //if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED)
                    //args = TRUE;
                args[0] = src.getText();
                if (null == args[0]) {
                    args[0] = EMPTY;
                }
                writer.invoke(target, args);
            }
            catch (Throwable t) {
                Reporter.critical(new StackTrace(t)); //NOI18N
            }
        }
    }
    
    static class IntStringItemListener extends StringItemListener {
        java.lang.reflect.Method reader = null;
        /*Object target = null;
        java.lang.reflect.Method writer = null;*/

        //private Object args[] = { target };
        
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
    

    /** Creates new BooleanStringItemListener */
        public IntStringItemListener(Object target, String destName) throws  java.beans.IntrospectionException {
            super(target,destName);
            //this.target = target;
            //writer = PropertyUtils.getWriter(target,destName);
        }
        /**/
        public IntStringItemListener(Object target, String destName, int min, int max) 
            throws  java.beans.IntrospectionException {
            super(target,destName);
            this.min = min;
            this.max = max;
            reader = PropertyUtils.getReader(target,destName);
            //this.target = target;
            //writer = PropertyUtils.getWriter(target,destName);
        }

        public void keyReleased(java.awt.event.KeyEvent ev) {
            try {
                //java.lang.reflect.Method lwriter = writer;
                javax.swing.text.JTextComponent src = (javax.swing.text.JTextComponent) ev.getSource();
                /*if (null == lwriter) {
                    java.awt.Component src = (java.awt.Component) itemEvent.getSource();
                    lwriter = PropertyUtils.getWriter(target, src.getName());
                }*/
                //Object args[] = FALSE;
                //if (itemEvent.getStateChange() == java.awt.event.ItemEvent.SELECTED)
                    //args = TRUE;
                //try {
                    args[0] = new Integer(src.getText());
                    Integer argVal = (Integer) args[0];
                    int argIVal = argVal.intValue();
                    /*args[0] = src.getText();
                    if (null == args[0]) {
                        args[0] = "";
                    }*/
                
                    if (argIVal >= min  && argIVal <= max)
                        writer.invoke(target, args);
                    else {
                        if (null != reader) {
                            Integer prev = (Integer) reader.invoke(target,null);
                            src.setText(prev.toString());
                        }
                    }
                //}
                //catch (
            }
            catch (Throwable t) {
                Reporter.critical(new StackTrace(t)); //NOI18N
            }
        }
    }    
}
