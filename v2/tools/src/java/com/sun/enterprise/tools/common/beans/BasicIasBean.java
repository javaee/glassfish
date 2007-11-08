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
 * BaicIasBean.java
 *
 * Created on April 4, 2002, 9:47 AM
 */

package com.sun.enterprise.tools.common.beans;

import java.util.ResourceBundle;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.modules.schema2beans.BaseBean;
/**
 *
 * @author  vkraemer
 */
public abstract class BasicIasBean {

    protected static final VetoableChangeListener greaterThanNegOne =
        new PositiveValueListener();
    protected static final VetoableChangeListener notNull =
        new NotEmptyValueListener();

    private PropertyChangeSupport pcs;
    private VetoableChangeSupport vcs;

    protected static final ResourceBundle bundle =
        ResourceBundle.getBundle("com.sun.enterprise.tools.common.beans.Bundle"); //NOI18N


    /** Creates a new instance of BaicIasBean */
    protected BasicIasBean() {
    }
    
    public PropertyChangeSupport getPCS() {
        if (null == pcs)
            pcs = new PropertyChangeSupport(this);
        return pcs;
    }
    
    public VetoableChangeSupport getVCS() {
        if (null == vcs)
            vcs = new VetoableChangeSupport(this);
        return vcs;
    }

     protected void doAttrSetProcessing(BaseBean bean, String newVal, String attrName, String propName)
        throws PropertyVetoException {
        String oldName = bean.getAttributeValue(attrName); //NOI18N
        fireMyVetoableChange(propName, oldName, newVal);
        bean.setAttributeValue(attrName,newVal); //NOI18N
        fireMyPropertyChange(propName, oldName, newVal);
    }
     
     protected void doElementSetProcessing(BaseBean bean, String newVal, String subElement, String propName)
        throws PropertyVetoException {
        String oldName = (String) bean.getValue(subElement);
        fireMyVetoableChange(propName, oldName, newVal);
        bean.setValue(subElement,newVal);
        fireMyPropertyChange(propName, oldName, newVal);
    }
     
     protected void doAttrSetProcessing(BaseBean bean, int newVal, String attrName, String propName) 
        throws PropertyVetoException {
        int oldVal = Integer.parseInt(bean.getAttributeValue(attrName)); //NOI18N
        fireMyVetoableChange(propName, oldVal, newVal);
        bean.setAttributeValue(attrName, ""+newVal); //NOI18N
        fireMyPropertyChange(propName, oldVal, newVal);
    }

     protected void fireMyVetoableChange(String name, Object oldV, Object newV) throws PropertyVetoException {
        if (null == vcs) {
            vcs = new VetoableChangeSupport(this);
        }
        vcs.fireVetoableChange(name,oldV,newV);
    }
    
    protected void fireMyVetoableChange(String name, int oldV, int newV) throws PropertyVetoException {
        if (null == vcs) {
            vcs = new VetoableChangeSupport(this);
        }
        vcs.fireVetoableChange(name,oldV,newV);
    }
    
    protected void fireMyPropertyChange(String name, Object oldV, Object newV) { // throws PropertyVetoException {
        if (null == pcs) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.firePropertyChange(name,oldV,newV);
    }
    
    protected void fireMyPropertyChange(String name, int oldV, int newV) { // throws PropertyVetoException {
        if (null == pcs) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.firePropertyChange(name,oldV,newV);
    }
        
    /** Add a property listener to this bean.
     * @param pcl PropertyChangeListener to add
     */    
   public void addPropertyChangeListener(PropertyChangeListener pcl) {
       if (null == pcs)
           pcs = new PropertyChangeSupport(this);
       pcs.addPropertyChangeListener(pcl);
   }

   /** Remove this listener.
    * @param pcl Listener to remove.
    */   
   public void removePropertyChangeListener(PropertyChangeListener pcl) {
       if (null != pcs) 
           pcs.removePropertyChangeListener(pcl);
   }

    /** Add a Vetoable listener to this bean.
     * @param pcl VetoableChangeListener to add
     */    
   public void addVetoableChangeListener(VetoableChangeListener pcl) {
       if (null == vcs)
           vcs = new VetoableChangeSupport(this);
       vcs.addVetoableChangeListener(pcl);
   }

   /** Remove this listener.
    * @param pcl Listener to remove.
    */   
   public void removeVetoableChangeListener(VetoableChangeListener pcl) {
       if (null != vcs) 
           vcs.removeVetoableChangeListener(pcl);
   }

   /////////////////////
   
   public abstract void outTo(java.io.OutputStream os) throws java.io.IOException;

   static class PositiveValueListener implements VetoableChangeListener {
        public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
            try {
                Integer valI= (Integer) pce.getNewValue();
                int val = valI.intValue();
                if (0 > val) {
                    String messFormat = bundle.getString("ERROR_MUST_BE_POSITIVE");
                    String mess = java.text.MessageFormat.format(messFormat, new Object[]  { pce.getPropertyName() });
                    throw new PropertyVetoException(mess, pce);
                }
            }
            catch (Throwable t) {
                throw new PropertyVetoException(t.getLocalizedMessage(), pce);
            }
        }
    }
    
   static class NotEmptyValueListener implements VetoableChangeListener {
        public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
            try {
                String val= (String) pce.getNewValue();
                //int val = valI.intValue();
                if (null == val || val.trim().length() == 0) {
                    String messFormat = bundle.getString("ERROR_MUST_HAVE_VALUE");
                    String mess = java.text.MessageFormat.format(messFormat, new Object[]  { pce.getPropertyName() });
                    throw new PropertyVetoException(mess, pce);
                }
            }
            catch (Throwable t) {
                throw new PropertyVetoException(t.getLocalizedMessage(), pce);
            }
        }
    }
   
}
