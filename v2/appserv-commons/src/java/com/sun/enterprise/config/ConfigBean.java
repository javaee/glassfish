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
package com.sun.enterprise.config;

import java.util.Vector;
import java.io.Serializable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Hashtable;
import com.sun.enterprise.config.pluggable.ConfigBeanInterceptor;
import com.sun.enterprise.config.pluggable.ConfigBeansSettings;
import com.sun.enterprise.config.pluggable.EnvironmentFactory;
import com.sun.enterprise.config.util.LoggerHelper;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import java.lang.reflect.Field;
import java.util.List;

/**
 * ConfigBean is the root class for all objects (beans)
 * representing Config information in server.xml
 *
 *@author Sridatta Viswanath
 */

public abstract class ConfigBean implements Serializable {
    
    /**
     * is pluggable for adding additional functionality
     * FIXME: to be initialized
     */
    transient private ConfigBeanInterceptor _interceptor = null;
    transient private ConfigBeansSettings _cbSettings = null;
    transient protected ConfigContext ctx = null;
    
    transient private Hashtable transientProperties = null;

    private static final String UNINITIALIZED_ATTRIBUTE_VALUE = "DONOTKNOW";
    private static final long UNINITIALIZED_LAST_MODIFIED_VALUE = -1;
    
    /**
     * lastModifed keeps track of the timestamp when it was
     * modified last. This is not user visible and is 
     * automatically updated and maintained. User gets
     * a StaleWriteConfigException if timestamp is different
     * while updating from config change List. Changes to 
     * this feature are in updateFromConfig* methods
     *
     * There is no meaning for lastModified in a deserialized
     * ConfigBean. So, it is transient. However, we do copy it
     * during the clone operation. 
     * -1 is uninitialized value.
     */
    transient private long thisLastModified = UNINITIALIZED_LAST_MODIFIED_VALUE;
    transient private long globalLastModified = UNINITIALIZED_LAST_MODIFIED_VALUE;
    
    private String xpath;
    
    /**
     * Get transient property value
     *
     * returns value for transientProperty or null if it did not have one
     **/
    public Object getTransientProperty(String name)
    {
        if(name==null || transientProperties==null)
            return null;
        return transientProperties.get(name);
    }
    /**
     * Set value for named transientProperty
     * if value==null then the property will be removed
     * returns the previous value of the specified key,
     * or null if it did not have one
     **/
    public Object setTransientProperty(String name, Object value)
    {
        if(name==null)
            return null;
        if(value==null)
        {
            if(transientProperties==null)
                return null;
            value =  transientProperties.get(name);
            if(value!=null)
                transientProperties.remove(name);
            return value;
        }
        if(transientProperties==null)
            transientProperties=new Hashtable();
        return transientProperties.put(name, value);
    }
    
    public void setXPath(String xpath) {
        this.xpath = (null != xpath ? xpath.trim() : xpath);
    }
    
    public String getXPath() {
        return (null != xpath ? xpath.trim() : xpath);
    }
    
   public ConfigContext getConfigContext() {
        return ctx;
    }
    
    public void setConfigContext(ConfigContext ctx) { 
        //FIXME make it package specific
        this.ctx = ctx;
    }

    //FIXME: reimplement??
    public void cleanup() {
	this.ctx = null;
    this._interceptor = null;
        setInvalidState();
    }    

    public ConfigBean() {
        setThisLastModified();   
        //_interceptor = getInterceptor();
        _cbSettings = getConfigBeansSettings();
    }
    
    /*
     * FIXME: TO BE REMOVED
     */
    
    public ConfigBean(Vector comps) {
            setThisLastModified();    
            //_interceptor = getInterceptor();
            _cbSettings = getConfigBeansSettings();
    }
   
     
            
    /**
     * This method is used to convert a string value to boolean.
     * @return true if the value is one of true, on, yes, 1. Note
     * that the values are case sensitive. If it is not one of these
     * values, then, it returns false. A finest message is printed if
     * the value is null or a info message if the values are 
     * wrong cases for valid true values.
     */
    public static boolean toBoolean(final String value){
        final String v = (null != value ? value.trim() : value);
        return null != v && (v.equals("true")
                             || v.equals("yes")
                             || v.equals("on") 
                             || v.equals("1"));
    }

    
    public void setAttributeValue(String name, String value) {
        try {
            setAttributeValue(name, value, true);
        } catch(StaleWriteConfigException swce) {
            String url = (this.ctx==null)?" ":ctx.getUrl();
            LoggerHelper.finest("Detected external changes to config file \"" +
                        url +
                        "\". Ignoring the condition and proceeding");
        }
    }
    
    /** 
     * @deprecated. will be made private soon.
     */
     public void setAttributeValue(String name, String value , boolean overwrite)  
            throws StaleWriteConfigException {
         final String nm = (null != name ? name.trim() : name);
         final String vl = (null != value ? value.trim() : value);
         
             //FIXME: make this private.

             // Do nothing if the value hasn't actually changed
         if (getAttributeValueSafe(nm) != null && getAttributeValueSafe(nm).equals(vl)){
             return;
         }
         
             
         if(!overwrite && this.ctx != null && this.ctx.isFileChangedExternally()) {
             throw new StaleWriteConfigException
                ("ConfigBean: cannot change since FileChangedExternally");
         }         
         
         String oldValue = UNINITIALIZED_ATTRIBUTE_VALUE;
         //special case for description
         if(_cbSettings.isSpecialElement(nm)) {
             oldValue = preSetAttributeValueSpecial(nm);
             setValue(nm, vl);
             postSetAttributeValueSpecial(nm, vl, oldValue);
         } else {
            oldValue = preSetAttributeValue(nm, vl);
            setValue(nm, vl);
            postSetAttributeValue(nm, vl, oldValue);
         }
     }
     
     public void setValue(String nm, String vl)  {
         
         try {
             java.lang.reflect.Field f = getClass().getField(nm);
             if (f!=null) {
                 f.set(this, vl);
             }
         } catch(Exception e) {
         }
     }
      

     /**
      * to get the child element generically
      */
     public ConfigBean[] getChildBeansByName(String childBeanName) {
         validateState();

         if(childBeanName == null) return null;
         childBeanName = childBeanName.trim();
         
         childBeanName = _cbSettings.mapElementName(childBeanName);
         
         ConfigBean[] ret = null;
         try {
             ret = (ConfigBean[]) getValues(childBeanName);
         } catch(Exception e) {
             // is a single valued element
            ret = getChildBeanByName(childBeanName);
         }
         return ret;
     }
     
     /**
      * Return the values
      */
     public Object[] getValues(String name) {
         if (null != name) {
             name = name.trim();
         }
         
         Object value = getValue(name);
         if (value instanceof List) {
             List list = (List) value;
             return list.toArray(new Object[list.size()]); 
         }
         return new Object[0];
     }
     
     /**
      * get All child beans
      */
     public ConfigBean[] getAllChildBeans() {
         ArrayList cbRet = new ArrayList();
         String[] childNames = getChildBeanNames();
         if(childNames == null || childNames.length == 0) return null;
         
         for(int i = 0;i<childNames.length;i++) {
             ConfigBean[] cb = getChildBeansByName(childNames[i]);
             if (cb == null) continue;
             for(int k=0;k<cb.length;k++) {
                cbRet.add(cb[k]);
             }
         }
         return toConfigBeanArray(cbRet);
     }
     
     public String[] listProperties() {
         
         List<String> names = new ArrayList<String>();
         for (Field field  : getClass().getFields()) {
             XmlAttribute xmlAttr  = field.getAnnotation(XmlAttribute.class);
             if (xmlAttr!=null) {
                 names.add(_cbSettings.mapElementName(xmlAttr.name()));
             }
         }                  
         return names.toArray(new String[names.size()]);
     }
     
     /**
      * getAll child bean names
      */
     private String[] getChildBeanNames() {
         List<String> names = new ArrayList<String>();
         for (Field field  : getClass().getFields()) {
             XmlAttribute xmlAttr  = field.getAnnotation(XmlAttribute.class);
             if (xmlAttr!=null) {
                 names.add(xmlAttr.name());
             }
         }                  
         return names.toArray(new String[names.size()]);
     }
     
     public int removeChild(ConfigBean child) throws ConfigException {
         //FIXME: remove???
         return removeChild(child, true);
     }
     
     /**
      * generically remove child
      * returns the index of removed child
      */
     public int removeChild(ConfigBean child, boolean overwrite) throws ConfigException {
         //FIXME: to be removed??
         if(!overwrite && this.ctx != null && this.ctx.isFileChangedExternally()) {
             throw new StaleWriteConfigException
                ("ConfigBean: cannot change since FileChangedExternally");
         }
         
         if(child == null)
             throw new ConfigException("Cannot remove null child");

         return this.removeValue(child.name(), child);
     }
     
     /**
      * Returns the name of the xml element for this bean.
      * @return the element's name
      */
     public String name() {
         
         XmlRootElement rootElement = getClass().getAnnotation(XmlRootElement.class);      
         if (rootElement!=null) {
             return _cbSettings.mapElementName(rootElement.name());
         } else {
             return null;
         }
     }
     
     
     public int addValue(String name, Object value) {
         int i=0;
         try {
            i= addValue(name, value, true);
         } catch(StaleWriteConfigException swce) {
             //dont throw it!!
         }
         return i;
     }
     
     /**
      * throws StaleWriteConfigException for the case of checking timestamps
      * throws exception if overwrite is false and file changed
      * externally
      */
     public int addValue(String name, Object value, boolean overwrite) 
            throws StaleWriteConfigException {
         if (null != name){
             name = name.trim();
         }
         
         
         if(!overwrite && this.ctx != null && this.ctx.isFileChangedExternally()) {
             throw new StaleWriteConfigException
                ("ConfigBean: cannot change since FileChangedExternally");
         }

         int i=-1;
         preAddValue(name, value);
         try {
             Field f = getClass().getField(name);
             if (f!=null) {
                 Object fieldValue = f.get(this);
                 if (fieldValue!=null) {
                     if (fieldValue instanceof List) {
                         List values = (List) fieldValue;
                         values.add(value);
                         i = values.indexOf(value);
                     }
                 }
             }
         } catch(Exception e) {
             
         }
         postAddValue(name, value);
         return i;
     }
     
     public int removeValue(String name, Object value) {
         if (null != name){
             name = name.trim();
         }
         
         int i=0;
         try {
            i= removeValue(name, value, true);
         } catch (StaleWriteConfigException swce) {
             //dont throw it!!
         }
         return i;
     }
      public int removeValue(String name, Object value, boolean overwrite) 
                                            throws StaleWriteConfigException {
          if (null != name) {
              name = name.trim();
          }
          
         if(!overwrite && this.ctx != null && this.ctx.isFileChangedExternally()) {
             throw new StaleWriteConfigException
                ("ConfigBean: cannot change since FileChangedExternally");
         }
         
         int i=-1;
         preRemoveValue(name, value);
         try {
             Field f = getClass().getField(name);
             if (f!=null) {
                 Object fieldValue = f.get(this);
                 if (fieldValue!=null) {
                     if (fieldValue instanceof List) {
                         List values = (List) fieldValue;
                         i = values.indexOf(value);
                         values.remove(value);
                     }
                 }
             }
         } catch(Exception e) {
             
         }
         postRemoveValue(name, value);
         return i;
      }
     
      public Object getValue(String name)
      {
          if (null != name) {
              name = name.trim();
          }
          
          Object res = getValue(name);
          res = postGetValue(name, res);
          return res;
      }
      
      public Object getValue(String name, int index)
      {
          if (null != name) {
              name = name.trim();
          }
          
         Object res = getValue(name, index);
         res = postGetValue(name, res);
         return res;
      }
      

      public void setValue(String name, Object value) {
          if (null != name) {
              name = name.trim();
          }
          
          try {
            setValue(name, value, true);
          } catch(StaleWriteConfigException swce) {
                // dont throw it!!
          }
      }
      
      public void setValue(String name, Object value, boolean overwrite) throws StaleWriteConfigException {
          if (null != name) {
              name = name.trim();
          }
          
         
         if(!overwrite && this.ctx != null && this.ctx.isFileChangedExternally()) {
             throw new StaleWriteConfigException
                ("ConfigBean: cannot change since FileChangedExternally");
         }
         Object oldValue = getValue(name);
             
         preSetValue(name, value);
         setValue(name, value);
         if(oldValue!=null && oldValue!=value && oldValue instanceof ConfigBean)
            postRemoveValue(name, oldValue);
         postSetValue(name, value);
      }
      
      /**
       */
      //FIXME: to take care of overwrite and config change event
      public void setValue(String name, Object[] value) {
          if (null != name) {
              name = name.trim();
          }
          
         preSetArrayValue(name, value);
         setValue(name, value);
         postSetArrayValue(name, value);
         
         
          
      }
      
      /**
       * generic method to get default value from dtd. will be over ridden in beans
       * note that it is also static which can cause some confusion
       * @deprecated use getDefaultAttributeValue
       */
      public static String getDefaultAttributeValueFromDtd(String attr) {
          if (null != attr) {
              attr = attr.trim();
          }
          
          return getDefaultAttributeValue(attr);
      }
  
      /**
       * generic method to get default value from dtd. will be over ridden in beans
       * note that it is also static which can cause some confusion
       */
      public static String getDefaultAttributeValue(String attr) {
          return null;
      }
      
      /**
       * get the xpath representation for this element
       * returns something like abc[@name='value'] or abc
       * depending on the type of the bean
       */
      protected String getRelativeXPath() {
          return null;
      }
      
      /**
       * get absolute xpath given the parent xpath
       *
       */
      public String getAbsoluteXPath(String parentXpath) {
          if(xpath!=null)
              return this.xpath.trim();
          if(parentXpath==null)
              return null;
          String rel = getRelativeXPath();
          if(rel == null) return null;
          return parentXpath.trim() + "/" + getRelativeXPath().trim();
      }
     
        /**
         * All attribute values containing strings of form ${system-property}
         * are modified such that all occurrences of ${system-property} are 
         * replaced by the corresponding value of the specifed system 
         * property. If you wish to fetch the un-mapped value you must invoke
         * getRawAttributeValue instead.
         **/
        public synchronized String getAttributeValue(String name) {
          if (null != name) {
              name = name.trim();
          }
          
            String res = getRawAttributeValue(name);       
            return postGetAttributeValue(name, res);
        }
        
        /**
         * We must support a clone() operation in which attribute values
         * are not expanded. In other words the clone must have all system property
         * references kept in the form ${xxxx} without expanding xxxx. The clone 
         * operation is synchronized with getAttributeValue so that an invocation of 
         * get AttributeValue while a clone is in progress will not result in a 
         * raw attribute value.
         *
         * We also clone the last modified timestamp.
         */
        public synchronized Object clone() {  
            final Object orig = preClone();
            try {
                final ConfigBean result = (ConfigBean) super.clone();
                postClone(orig, result);
                result.setGlobalLastModified(getThisLastModified());
                return result;
            } catch(Exception e) {
                return null;
            }
        }

                
       public String getRawAttributeValue(String name) {
           if (null != name) {
               name = name.trim();
           }

               //FIXME: description fix later for config change event.
            if(_cbSettings.isSpecialElement(name)) {
                final String v =  (String) getValue(name);
                return (null != v ? v.trim() : v);
            }
            preRawGetAttributeValue(name);
            String s = getAttributeValue(name);
            postRawGetAttributeValue(name, s);
          return (null != s ? s.trim() : s);
       }
       
       public String[] getAttributeNames() {
           List<String> names = new ArrayList<String>();
           for (Field f : getClass().getFields()) {
               XmlAttribute xmlAttr = f.getAnnotation(XmlAttribute.class);
               if (xmlAttr!=null) {
                   names.add(f.getName());
               }
           }
           return names.toArray(new String[names.size()]);
       }

       /**
        * overriding BaseBean.java to maskout user names and passwords
        */
       public void dumpAttributes(String name, int index, StringBuffer str,
                                                            String indent) {
/**          if (null != name) {
              name = name.trim();
          }
          
         String[] names = this.getAttributeNames(name);

         for (int i=0; i<names.length; i++) {
             String v = null;
             //To mask out Password and UserName
             if(names[i].indexOf("Password") != -1 || names[i].indexOf("UserName") != -1) {
                 v = "*****";
             }  else {
                v = this.getAttributeValue(name, index, names[i]);
             }
             
            if (v != null) {
                str.append(indent + "\t  attr: ");      // NOI18N
                str.append(names[i]);
                str.append("=");        // NOI18N
                str.append(v);
            }
         }
 ***/
       }
     
       
    // FIXME: TO BE REMOVED
    public void changed() {
    }

  //ROB: config changes - added to fix compilation errors in subclasses
    /**
     *
     */
    public boolean isEnabled() {
        //FIXME: to be removed
          return true;
    }


    //ROB: config changes - added to fix compilation errors in subclasses
    /**
     *
     */
    public void setEnabled(boolean enabled) {
                //FIXME: to be removed
      //do nothing
    }

// Temporary FIX for monitoring enabled. will be changed to use new monitoring service element later.
    public boolean isMonitoringEnabled() {
                //FIXME: to be removed
          return false;
    }

    //FIXME: move camelize into another class and make it the base class
    // and make the gen classes extend from that class.
    
    /**
     * camelize will convert the lower case into upper case in the following
     * format. Eg: user-group ==> UserGroup
     *
     * Convert a DTD name into a bean name:
     *
     * Any - or _ character is removed. The letter following - and _
     * is changed to be upper-case.
     * If the user mixes upper-case and lower-case, the case is not
     * changed.
     * If the Word is entirely in upper-case, the word is changed to
     * lower-case (except the characters following - and _)
     * The first letter is always upper-case.
     */
    //FIXME: TO BE REMOVED. Making it private.
    public static String camelize(String name)
    {
        if (null != name) {
            name = name.trim();
        }
          

        CharacterIterator  ci;
        StringBuffer    n = new StringBuffer();
        boolean    up = true;
        boolean    keepCase = false;
        char    c;
        
        ci = new StringCharacterIterator(name);
        c = ci.first();
        
        // If everything is uppercase, we'll lowercase the name.
        while (c != CharacterIterator.DONE)
        {
            if (Character.isLowerCase(c))
            {
                keepCase = true;
                break;
            }
            c = ci.next();
        }
        
        c = ci.first();
        while (c != CharacterIterator.DONE)
        {
            if (c == '-' || c == '_')
                up = true;
            else
            {
                if (up)
                    c = Character.toUpperCase(c);
                else
                    if (!keepCase)
                        c = Character.toLowerCase(c);
                n.append(c);
                up = false;
            }
            c = ci.next();
        }
        return n.toString();
    }
    
    /**
     * should be called from the constructor and
     * all the changeable methods
     * Sets the lastModified to current time.
     */
    private void setThisLastModified() {
        thisLastModified = System.currentTimeMillis();
    }
    
    /**
     * should be called ONLY from clone
     * sets the lastmodified to timestamp
     */
    private void setGlobalLastModified(long timestamp) {
        globalLastModified = timestamp;
    }
    
    /**
     * is package scope since it is called from configcontext
     * FIXME: temporarily making it public for testing
     */
    public long getGlobalLastModified() {
        return globalLastModified;
    }
    
    public long getThisLastModified() {
        return thisLastModified;
    }
    
    public synchronized void setInterceptor(ConfigBeanInterceptor cbi) {
        this._interceptor = cbi;
    }

    public synchronized ConfigBeanInterceptor getInterceptor() {
        if (null != _interceptor) {
            return _interceptor;
        }
        ConfigBeanInterceptor cbi = null;
        //get interceptor of ctx.
        if (null != ctx) {
            /**
             * Should have used the ConfigContext interface. Too late and
             * risky to change the interface.
             */
            cbi = ctx.getConfigBeanInterceptor();
        }
		/*
        if (null == cbi) {
            cbi = EnvironmentFactory.getEnvironmentFactory().
                getConfigEnvironment().getConfigBeanInterceptor();
        }*/
        //_interceptor = cbi;
        return cbi;
    }
    
    private ConfigBeansSettings getConfigBeansSettings() {
        return EnvironmentFactory.
                getEnvironmentFactory().
                getConfigEnvironment().
                getConfigBeansSettings();
    }
    
    private Object postGetValue(String name, Object res) {
        addXPathToChild(res);
        return (getInterceptor()==null)
                ?res:getInterceptor().postGetValue(this, name, res);
    }
    private Object[] postGetValues(String name, Object[] res) {
        addXPathToChild(res);
        return (getInterceptor()==null)
                ?res:getInterceptor().postGetValues(name, res);
    }
    private String postGetAttributeValue(String name, String res) {
        String s = (getInterceptor()==null)
                ?res:getInterceptor().postGetAttributeValue(name, res);
	return (null != s ? s.trim() : s);
    }
    private Object preClone() {
        if(getInterceptor() != null)
            return getInterceptor().preClone();
        return null;
    }
    
    private void postClone(Object o, Object result) {
        final ConfigBeanInterceptor cbi = getInterceptor();
        if(cbi != null) {
            cbi.postClone(o);
        }
    }
    
    /** remove this if getattribute value is always safe
     */
    private String getAttributeValueSafe(String name) {
        try {
            return this.getAttributeValue(name);
        } catch(Throwable t){
           //ignore exceptions. all kind.
        }
        return null;
    }
    
    private void addToConfigChangeList(String xpath,
                                        String tag,
                                        String oldValue,
                                        String newValue) {
        ConfigChange cChange = null;
        if(ctx != null) {
            cChange = this.ctx.addToConfigChangeList(
                                        (null != xpath ? xpath.trim() : xpath),
                                        (null != tag ?   tag.trim() :   tag),
                                        (null != oldValue ? oldValue.trim() : oldValue),
                                        (null != newValue ? newValue.trim() : newValue));
            if(cChange!=null) //can be null for "description" change
                cChange.setGlobalLastModified(getGlobalLastModified());
        }
    }
    
    private String preSetAttributeValue(String name, String value) {
        preConfigChange(name, value, ConfigContextEvent.PRE_UPDATE_CHANGE, "UPDATE",
                        (null != this.name() ? this.name().trim() : this.name()));
        
        return this.getAttributeValueSafe(name);
    }
    
    private void preAddValue(String name, Object value) {
        preConfigChange(name, value, ConfigContextEvent.PRE_ADD_CHANGE, "ADD");
    }
    
     private void preRemoveValue(String name, Object value) {
         preConfigChange(name, value, ConfigContextEvent.PRE_DELETE_CHANGE, "DELETE");
    }
     
     private void preSetValue(String name, Object value) {
          preConfigChange(name, value, ConfigContextEvent.PRE_SET_CHANGE, "SET");
     }

    private void preConfigChange(String name, Object value, String type, String operation){
        preConfigChange(name, value, type,operation, null);
    }
    
    private void preConfigChange(String name, Object value, String type, String operation, String beanName) {
        validateState();
        
        if(ctx !=null) {
             ConfigContextEvent ccce = new ConfigContextEvent(ctx, type,name,value,operation, beanName);
             ccce.setClassObject(this);
             ctx.preChange(ccce);           
         }
    }
    
    private void postSetAttributeValue(String name, String value, String oldValue) {
        //reset the lastmodified timestamp
            setThisLastModified();
            
            ConfigChange cChange = null;
            
          if(ctx != null) {
                cChange = this.ctx.addToConfigChangeList(this.xpath, name, oldValue, value); 
                if(cChange != null) cChange.setGlobalLastModified(this.getGlobalLastModified());
                
                // <addition> srini@sun.com server.xml verifier
                //this.ctx.postChange(ConfigContextEvent.POST_UPDATE_CHANGE);
                ConfigContextEvent ccce = new ConfigContextEvent(ctx, ConfigContextEvent.POST_UPDATE_CHANGE,name,value,"UPDATE");
                this.ctx.postChange(ccce);
                // </addition> server.xml verifier  
            }
    }
    
    private void postAddValue(String name, Object value) {
        
        //reset the lastmodified timestamp
         setThisLastModified();
         
        //notification
         if(ctx != null) {
         if(value instanceof ConfigBean) {
             try {
                 //FIXME: clone?
                ConfigChange cChange = 
                    this.ctx.addToConfigChangeList(
                        this.xpath, 
                        ((ConfigBean)value).getAbsoluteXPath(this.xpath), 
                        name, 
                        //this clone adds a lot of overhead and was there
                        //because of a bug in schema2beans
                        //((ConfigBean)this.ctx.getRootConfigBean().clone())); 
                        ((ConfigBean)this.ctx.getRootConfigBean())); 
                if(cChange != null) cChange.setGlobalLastModified(this.getGlobalLastModified());
             } catch(Exception ce) {
                 ce.printStackTrace();
             }
            
            ((ConfigBean)value).setConfigContext(this.ctx);
            ((ConfigBean)value).setXPath(((ConfigBean)value).getAbsoluteXPath(this.xpath));
         }
            
            ConfigContextEvent ccce = new ConfigContextEvent(ctx, ConfigContextEvent.POST_ADD_CHANGE,name,value,"ADD");
            this.ctx.postChange(ccce);
            
         } else {
			 //_logger.log(Level.INFO,"config.change_not_registered");
         }
    }
    
    private void postSetValue(String name, Object value) {
         //reset the lastmodified timestamp
         setThisLastModified();
         
         if(ctx != null) {
            // this.ctx.addToConfigChangeList(this.xpath, name, value, null); 
            //FIXME: remove later
            if(value instanceof ConfigBean) {
                //FIXME: clone?
                ConfigChange cChange = this.ctx.addToConfigChangeList(this.xpath, name, ((ConfigBean)value).clone(), null); 
                if(cChange != null) cChange.setGlobalLastModified(this.getGlobalLastModified());
                
                ((ConfigBean)value).setConfigContext(this.ctx); //incase of description
                ((ConfigBean)value).setXPath(((ConfigBean)value).getAbsoluteXPath(this.xpath));
                // <addition> srini@sun.com
                //ctx.postChange(ConfigContextEvent.POST_SET_CHANGE);
                ConfigContextEvent ccce = new ConfigContextEvent(ctx,ConfigContextEvent.POST_SET_CHANGE,name,value,"SET");
                ctx.postChange(ccce);
                // </addition> server.xml verifier
                
            } else {
                ConfigChange cChange = this.ctx.addToConfigChangeList(this.xpath, name, value, null); 
                if(cChange != null) cChange.setGlobalLastModified(this.getGlobalLastModified());
            }
         } else {
			 //_logger.log(Level.INFO,"config.change_not_registered");
         }
    }
    
    private void postRemoveValue(String name, Object value) {
        //reset the lastmodified timestamp
          setThisLastModified();
          
          //notification
         if(value instanceof ConfigBean){
         if(ctx != null) {
            ConfigChange cChange = this.ctx.addToConfigChangeList(((ConfigBean)value).getXPath()); 
            if(cChange != null) cChange.setGlobalLastModified(this.getGlobalLastModified());
            
            // <addition> srini@sun.com server.xml verifier
            //ctx.postChange(ConfigContextEvent.POST_DELETE_CHANGE);
            ConfigContextEvent ccce = new ConfigContextEvent(ctx, ConfigContextEvent.POST_DELETE_CHANGE,name,value,"DELETE");
            ctx.postChange(ccce);
            // </addition> server.xml verifier
            
         } else {
			 //_logger.log(Level.INFO,"config.change_not_registered");
         }
         }
    }
    private String preSetAttributeValueSpecial(String name) {
        return getAttributeValueSafe(name);
    }

    private void postSetAttributeValueSpecial(String name, 
                            String value, String oldValue) {
        //reset the lastmodified timestamp
        setThisLastModified();
             
        addToConfigChangeList(this.xpath,
                                     name,
                                     oldValue,
                                     value);   
    }

     private ConfigBean[] toConfigBeanArray(ArrayList cbRet) {
          ConfigBean[] ret = new ConfigBean[cbRet.size()];
                for(int j=0;j<cbRet.size();j++) {
                    ret[j] = (ConfigBean) cbRet.get(j);
                }
         return ret;
     }
     
     private ConfigBean[] getChildBeanByName(String childBeanName) {
         ConfigBean[] ret = null;
          try {
                ConfigBean cb = (ConfigBean) getValue(childBeanName);
                if(cb!=null)
                {
                    ret = new ConfigBean[1];
                    ret[0] = cb; 
                }
             } catch (Exception c) {}
          return ret;
     }
      private void addXPathToChild(Object obj)
      {
          if(obj!=null && obj instanceof ConfigBean)
          {
            //set XPath for child bean
            ConfigBean cb = (ConfigBean)obj;
            if(cb.xpath==null && this.xpath!=null)
               cb.setXPath(cb.getAbsoluteXPath(this.xpath));          
          }
      }
      
      private void addXPathToChild(Object[] obj)
      {
          if(obj==null)
              return;
          for(int i=0; i<obj.length; i++)
          {
              addXPathToChild(obj[i]);
          }
      }
      private void preRawGetAttributeValue(String name) {
          validateState();
          
          if(ctx !=null) {
             ConfigContextEvent ccce = new ConfigContextEvent(ctx, ConfigContextEvent.PRE_ACCESS);
             ccce.setClassObject(this);
             ctx.preChange(ccce);
         }
      }
      
      private void postRawGetAttributeValue(String name, String s) {
          if(ctx !=null) { 
             ConfigContextEvent ccce = new ConfigContextEvent(ctx, ConfigContextEvent.POST_ACCESS);
             ctx.postChange(ccce);
         }
      }
      
      private void preSetArrayValue(String name, Object[] value) {
          validateState();
          
           if(ctx !=null) {
             ConfigContextEvent ccce = 
                new ConfigContextEvent(ctx, 
                                    ConfigContextEvent.PRE_SET_CHANGE,
                                    name,
                                    value,
                                    "SET");
             ccce.setClassObject(this);
             ccce.setBeanName(this.name());
             ctx.preChange(ccce);           
         }
      }
      
      private void postSetArrayValue(String name, Object[] value) {
      
          //reset the lastmodified timestamp
          setThisLastModified();
          if(ctx != null) {
              ConfigChange cChange = 
                this.ctx.addToConfigChangeList(this.xpath, name, null, value);
              if(cChange != null) {
                    cChange.setGlobalLastModified(this.getGlobalLastModified());
              }
              
              ConfigContextEvent ccce = 
                new ConfigContextEvent(ctx, 
                                    ConfigContextEvent.POST_SET_CHANGE,
                                    name,
                                    value,
                                    "SET");
             //ccce.setClassObject(this); //why FIXME??
             ccce.setBeanName(this.name());
             try {
                ctx.postChange(ccce);           
             } catch(Exception e) {
                 //catch for now. may remove later.
                 //e.printStackTrace(); //FIXME
             }
          } else          {
			  //_logger.log(Level.INFO,"config.change_not_registered");
          }
      }
      
      /**
       * state of the config beans
       */
      transient private String _state = VALID_STATE;
      
      /*
       * possible states
       */
      private static final String VALID_STATE = "valid_state";
      private static final String INVALID_STATE = "invalid_state";
      
      /**
       * is used to validate state before any operation to the config bean.
       * If the state is not valid, a runtime exception will be thrown
       */
      private void validateState() {
          if(!VALID_STATE.equals(_state)) {
              throw new ConfigRuntimeException("Config API Usage Error: State of ConfigBean is INVALID. No operations are permitted");
          }
      }
      
      private void setInvalidState() {
          _state = INVALID_STATE;
      }
      

}
