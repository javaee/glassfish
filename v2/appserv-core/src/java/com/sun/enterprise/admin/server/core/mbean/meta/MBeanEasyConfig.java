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

package com.sun.enterprise.admin.server.core.mbean.meta;


import com.sun.enterprise.admin.util.PropertiesStringSource;

//JDK imports
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

//JMX imports
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanInfo;
import javax.management.IntrospectionException;

//Admin imports
//import com.sun.enterprise.admin.common.exception.ServerInstanceException;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/****************************************************************************************************************
    This <code>class</code> is for holding and providing <code>MBeanInfo</code> data
    <strong>for Dynamic MBeans</strong> (operations,attributes,MBean description...).
*/

public class MBeanEasyConfig //extends AdminBase implements MBeanRegistration
{
	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( MBeanEasyConfig.class );
 
  final static String DESCR_LINE_DELIMITER         = ",";
  
  //STRING SOURCE FILE
  final static String STRINGSOUREC_FILENAME         = "MBeanConfig.properties";
  
  //EXCEPTIONS 
  final static String EXCEPTION_LINE_EMPTY          = localStrings.getString( "admin.server.core.mbean.meta.empty_description_line" );
  final static String EXCEPTION_WRONG_LINE_FORMAT   = localStrings.getString( "admin.server.core.mbean.meta.wrong_fields_number_in_description_line" );
  final static String EXCEPTION_NAME_FLD_EMPTY      = localStrings.getString( "admin.server.core.mbean.meta.empty_name_in_description_line" );
  final static String EXCEPTION_CLASSNAME_FLD_EMPTY = localStrings.getString( "admin.server.core.mbean.meta.empty_classname_in_description_line" );
  final static String EXCEPTION_UNKNOWN_RWTYPE      = localStrings.getString( "admin.server.core.mbean.meta.unknown_rw_type_in_description_line" );
  final static String EXCEPTION_UNKNOWN_IMPACT_TYPE = localStrings.getString( "admin.server.core.mbean.meta.unknown_impact_type_in_description" );
  final static String EXCEPTION_OPER_NOT_FOUND      = localStrings.getString( "admin.server.core.mbean.meta.unfound_operation_in_bean_class" );
  
  final static String EXCEPTION_WRONG_PARAM_FORMAT  = localStrings.getString( "admin.server.core.mbean.meta.wrong_operation_parameter_in_description" );
  final static String EXCEPTION_WRONG_PARAM_DIM     = localStrings.getString( "admin.server.core.mbean.meta.wrong_operation_parameter_dimension_in_description" );
   final static String EXCEPTION_WRONG_PARAM_CLASS  = localStrings.getString( "admin.server.core.mbean.meta.wrong_parameter_attribute_class_in_description" );
 final static String EXCEPTION_EMPTY_PARAM_TYPE     = localStrings.getString( "admin.server.core.mbean.meta.empty_parameter_attribute_type_in_description_line" );
//IMPACT CODES
  final static String TYPE_INFO        = "INFO";
  final static String TYPE_ACTION      = "ACTION";
  final static String TYPE_ACTION_INFO = "ACTION_INFO";
  final static String TYPE_UNKNOWN     = "UNKNOWN";

  //info members
  private MBeanAttributeInfo[]		  m_attrs	 = new MBeanAttributeInfo[0];
  private MBeanConstructorInfo[]    m_cstrs  = new MBeanConstructorInfo[0];
  private MBeanOperationInfo[]		  m_opers	 = new MBeanOperationInfo[0];
  private MBeanNotificationInfo[]   m_notfs	 = new MBeanNotificationInfo[0];
  private Class      	 	            m_beanClass	 = null;   //configured MBean Class
  private String 		                m_descr  = null;   //MBean dscription

  /****************************************************************************************************************
    * Constructs of <code>MBeansEasyConfig</code> from provided
    * text descriptions of MBean components (operations, attributes,...).
    * @param configuringMBeanClass the configuring Dynamic MBean object Class.
    * @param attrDescr array of text decriptions of MBean attributes,
    * each string represents one attribute in format: 
    * <ul>&ltATTR_NAME>,&ltATTR_OBJECT_CLASS>, &ltATTR_RW_TYPE>, &ltATTR_DESCRIPTION_RESOURCE_ID>.
    * <br>E.g.
    * <ul>   {
    * <ul>     "courses,    java.lang.String[], RW, MBEAN_DESCR_ATTR_COURSES",
    * <br>     "beginTime,  java.util.Date,     RW, MBEAN_DESCR_ATTR_BEGINTIME",
    * <br>     "sum,        int,                R , MBEAN_DESCR_ATTR_SUM"
    * </ul><br>   };</ul></ul>
    * @param operDescr array of text decriptions of MBean operations, 
    * <ul>each string represents one operation in format: 
    * <br>&ltOP_NAME>,&ltOP_TYPE>,&ltOP_DESCRIPTION_RESOURCE_ID>
  *  e.g.:
  *  <br>   {  
  *  <ul>       "eat, ACTION, MBEAN_DESCR_OPER_EAT",
  *  <br>       "throw_up, ACTION_INFO, MBEAN_DESCR_OPER_THROW_UP",
  *  <br>       "calc, INFO, MBEAN_DESCR_OPER_CALC" 
  *  </ul>   }; (note that last description includes comma) </ul>
  *  @throws MBeanConfigException exception if the param parsing is not successful
  */
  public MBeanEasyConfig(Class configuringMBeanClass, String[] attrDescr, String[] operDescr, String descr) throws MBeanConfigException 
  {
      m_beanClass  = configuringMBeanClass; // configuring MBean save
      m_descr = descr; //save MBean descriptor
      if(m_descr==null || m_descr.length()==0)
         m_descr = ""+getPureClassName(configuringMBeanClass)+".mbean";
         
      // create INFOs
      m_attrs = createAttributesInfo(m_beanClass, attrDescr);
      m_cstrs = createConstructorsInfo(m_beanClass, null);
      m_opers = createOperationsInfo(m_beanClass, operDescr);
      //m_notfs = createNotificationsInfo(notfsDescr);
  }
		
  //**********************************************************************************************************************************************************************************************************************************************
  /****************************************************************************************************************
      Creates <code>MBeanAttributeInfo[]</code> array according to descriptions 
      given in text format (see constructor comments).
      @param configuringMBeanClass the configuring MBean Class object;
      @param attrDescr array of text decriptions of MBean <strong>attributes</strong>,
      @throws MBeanConfigException exception if the param parsing is not successful
  */
  public static MBeanAttributeInfo[] createAttributesInfo(Class configuringMBeanClass, String[] attrDescr) throws MBeanConfigException
	{
    if(attrDescr==null || attrDescr.length<1)
      return new MBeanAttributeInfo[0];
    MBeanAttributeInfo[] infos = new MBeanAttributeInfo[attrDescr.length];
    String beanName  = getPureClassName(configuringMBeanClass);
   // the weak restriction - all descr strings should be non-empty
    for(int i=0; i<attrDescr.length; i++)
      {
        infos[i] = parseAttributeDescrLine(beanName, attrDescr[i]);
      }
    return  infos;
  }  
    
  /****************************************************************************************************************
      Creates <code>MBeanOperationInfo[]</code> array according to descriptions 
      given in text format (see constructor comments) and to configuring
      MBean class reflection.
      @param configuringMBeanClass the configuring MBean object;
      @param operDescr array of text decriptions of MBean <strong>operations</strong>,
      @throws MBeanConfigException exception if the param parsing is not successful
  */
  public static MBeanOperationInfo[] createOperationsInfo(Class configuringMBeanClass, String[] operDescr) throws MBeanConfigException
	{
    if(configuringMBeanClass==null)
      return new MBeanOperationInfo[0];
    if(operDescr==null || operDescr.length<1)
      return new MBeanOperationInfo[0];
    ArrayList arr = new ArrayList();
    for(int i=0; i<operDescr.length; i++)
      {
        MBeanOperationInfo[] overloads = parseOperationDescrLine(configuringMBeanClass, operDescr[i]);
        for(int j=0; j<overloads.length; j++)
          arr.add(overloads[j]);
      }
        
    MBeanOperationInfo[] infos = new MBeanOperationInfo[arr.size()];
    // the weak restriction - all descr strings should be non-empty
    for(int i=0; i<infos.length; i++)
      {
        infos[i] = (MBeanOperationInfo)arr.get(i);
      }
    return  infos;
  }  
    
    
  /****************************************************************************************************************
      Creates <code>MBeanConstructorInfo[]</code> array according to configuring
      MBean class reflection.
      @param configuringMBeanClass the configuring MBean Class object
      @param constructorDescr constructors text decriptions
  */
  public static MBeanConstructorInfo[] createConstructorsInfo(Class configuringMBeanClass, String constructorDescr)
	{
    if(configuringMBeanClass==null)
      return new MBeanConstructorInfo[0];
    if(constructorDescr==null)
      {
         String nameDescr = getPureClassName(configuringMBeanClass)+".constructor";
         constructorDescr = getResourceString(nameDescr, nameDescr); 
      }
    //enumerate all constructors
    Constructor[] ctrs = configuringMBeanClass.getConstructors();
    MBeanConstructorInfo[] infos = new MBeanConstructorInfo[ctrs.length];
    for(int i=0; i<ctrs.length; i++)
      {
        infos[i] = new MBeanConstructorInfo(constructorDescr, ctrs[i]); 
      }
    return  infos;
  }  
    
  /****************************************************************************************************************
      Get <code>MBeanInfo</code> description object for configurable MBean.
      @return <code>MBeanInfo</code> description object for configurable MBean.
  */
  public MBeanInfo getMBeanInfo()
  {
    if(m_beanClass==null)
      return null;
    else
      return new MBeanInfo(m_beanClass.getName(), m_descr, 
                            m_attrs, m_cstrs, m_opers, m_notfs);

  }

  /****************************************************************************************************************
      Sets (updates) description of configured  <code>MBean</code>
      @param beanDescription the descriptions of MBean.
  */
  public void setMBeanDescription(String beanDescription)
  {
      m_descr = beanDescription; //set MBean descriptor
  }

  /****************************************************************************************************************
      Sets (updates) <code>MBeanAttributeInfo[]</code> component of contained <code>MBeanInfo</code>
      @param attributesInfo array of attribute descriptions.
  */
  public void setAttributesInfo(MBeanAttributeInfo[] attributesInfo)
  {
     if(attributesInfo==null)
        attributesInfo = new MBeanAttributeInfo[0];
     m_attrs = attributesInfo;
  }
		
  /****************************************************************************************************************
      Sets (updates) <code>MBeanConstructorInfo[]</code> component of contained <code>MBeanInfo</code>
      @param constructorInfo array of operation descriptions.
  */
  public void setOperationsInfo(MBeanConstructorInfo[] constructorInfo)
  {
     if(constructorInfo ==null)
        constructorInfo = new MBeanConstructorInfo[0];
     m_cstrs = constructorInfo;
  }
		
  /****************************************************************************************************************
      Sets (updates) <code>MBeanOperationInfo[]</code> component of contained <code>MBeanInfo</code>
      @param operationsInfo array of operation descriptions.
  */
  public void setOperationsInfo(MBeanOperationInfo[] operationsInfo)
  {
     if(operationsInfo ==null)
        operationsInfo = new MBeanOperationInfo[0];
     m_opers = operationsInfo;
  }
		
  /*****************************************************************************************************************
      Sets ( updates) <code>MBeanNotificationInfo[]</code> component of contained <code>MBeanInfo</code>
      @param notificationsInfo array of notifications descriptions.
  */
  public void setNotificationsInfo(MBeanNotificationInfo[] notificationsInfo)
  {
     if(notificationsInfo==null)
        notificationsInfo = new MBeanNotificationInfo[0];
     m_notfs = notificationsInfo;
  }
		
        
  /*****************************************************************************************************************
      Converts string value presentation to target type (esp. useful for CLI).
      @param value The string value to convert
      @param targetType The string description of target value type (@see Class)
      @return The target type object which represents value in the proper type.
  */
  public static Object convertStringValueToProperType(String value, String targetType) throws MBeanConfigException
  {
    if(value==null) 
      return null;
    
    Class cl;
    try 
    {
        if((cl=getPrimitiveClass(targetType))!=null || (cl=getRelatedPrimitiveClass(targetType))!=null)
          {
            if(cl==Byte.TYPE)
                return new Byte(value);
            if(cl==Character.TYPE)
                return new Character(value.charAt(0));
            if(cl==Double.TYPE)
                return new Double(value);
            if(cl==Float.TYPE)
                return new Float(value);
            if(cl==Integer.TYPE)
                return new Integer(value);
            if(cl==Long.TYPE)
                return new Long(value);
            if(cl==Short.TYPE)
                return new Short(value);
            if(cl==Boolean.TYPE)
                return new Boolean(value);
          }
    }
    catch(java.lang.IllegalArgumentException e)
    {
		String msg = localStrings.getString( "admin.server.core.mbean.meta.convertstringvaluetopropertype_wrong_argument_type", e.getMessage() );
        throw new MBeanConfigException( msg );
    }
     //no arrays yet
     return value;
  }

  /****************************************************************************************************************
      Parsing the attr description string to create <code>MBeanAttributeInfo</code> object.
      @param  descrLine string with one attribute description (see constructor's comment).
      @return MBeanAttributeInfo object for given description line.
      @throws MBeanConfigException exception if the param parsing is not successful
  */
  private static MBeanAttributeInfo parseAttributeDescrLine(String beanName, String descrLine) throws MBeanConfigException
  {
    //line format: <ATTR_NAME>,<ATTR_OBJECT_CLASS>,<ATTR_RW_TYPE>,<ATTR_DESCRIPTION_RESOURCE_ID>.
    String name;
    String className;
    String rwType;
    String descr;
    boolean bReadable = false, bWritable = false;
       
    if(descrLine==null) {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_exception_line_empty", EXCEPTION_LINE_EMPTY );
      throw new MBeanConfigException( msg );
	}

    //EXTRACT line fields
    String[] flds = getLineFields(descrLine, 4);
    if(flds.length<3 || flds.length>4) {
	   String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_WRONG_LINE_FORMAT, descrLine );
       throw new MBeanConfigException( msg );
	}
    
    //NAME
    name = flds[0];
    if(name.length()==0) {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_NAME_FLD_EMPTY, descrLine );
      throw new MBeanConfigException( msg );
	}
       
    //OBJECT CLASS NAME
    className = flds[1];
    if(className.length()==0) {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_CLASSNAME_FLD_EMPTY, descrLine );
      throw new MBeanConfigException( msg );
	}
    className = convertTypeToSignatureClass(className).getName();
       
       
    //RWTYPE (R/W/RW)
    rwType = flds[2];   
    if(rwType.equals("R"))
      bReadable = true;
    else   
      if(rwType.equals("W"))
        bWritable = true;
      else   
        if(rwType.equals("RW") || rwType.equals("WR"))
          {
            bWritable = true;
            bReadable = true;
          }
        else {
		  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_UNKNOWN_RWTYPE, descrLine );
          throw new MBeanConfigException( msg );
		}
    
    //DESCRIPTION
    String defaultName = beanName+"."+name+".attribute";

    if(flds.length<4 || flds[3].length()==0)
      descr = getResourceString(defaultName, defaultName);
    else
      descr = getResourceString(flds[3], defaultName);
       
    return new MBeanAttributeInfo(name, className, descr, bReadable, bWritable, false);
  }

  /****************************************************************************************************************
      Parsing the operation description string to create <code>MBeanAttributeInfo[]</code> array
      (There is possible to have more than one resulting element even for one operation - 
      in case of the corresponding method has overloadings).
      @param configuringMBeanClass Mbean object Class for reflection.
      @param descrLine string with one operation description (see constructor's comment).
      @return array of MBeanAttributeInfo for given operation.
      @throws MBeanConfigException exception if the param parsing is not successful
  */
  private static MBeanOperationInfo[] parseOperationDescrLine(Class configuringMBeanClass, String descrLine) throws MBeanConfigException
  {
    //line format: <OP_NAME>,<OP_TYPE>,<OP_DESCRIPTION>
    String name;
    String typeName;
    int    type;
    String descr;
       
    if(descrLine==null) {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_exception_line_empty", EXCEPTION_LINE_EMPTY );
      throw new MBeanConfigException( msg );
	}
    
    //EXTRACT method_name(param1, param2, ...) part
    int idx1 = descrLine.indexOf('(');
    if(idx1<=0 || idx1==descrLine.length()-1) {
	   String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_WRONG_PARAM_FORMAT, descrLine );
       throw new MBeanConfigException( msg );
	}
    name = descrLine.substring(0, idx1).trim();
    if(name.length()==0) {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_NAME_FLD_EMPTY, descrLine );
      throw new MBeanConfigException( msg );
	}
    int idx2 = descrLine.indexOf(')', idx1+1);
    if(idx2<=0) {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_WRONG_PARAM_FORMAT, descrLine );
      throw new MBeanConfigException( msg );
	}
    
    Object[] params = decomposeParametersDescription(descrLine.substring(idx1+1,idx2));
    
    //EXTRACT line fields
    String[] flds = getLineFields(descrLine.substring(idx2), 3);
    if(flds.length<2) {
	   String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_WRONG_LINE_FORMAT, descrLine );
       throw new MBeanConfigException( msg );
	}
    
    
    //TYPE (INFO/ACTION...)
    typeName = flds[1];   
    if(typeName.equals(TYPE_INFO))
      type = MBeanOperationInfo.INFO;
    else   
      if(typeName.equals(TYPE_ACTION))
        type = MBeanOperationInfo.ACTION;
      else   
        if(typeName.equals(TYPE_ACTION_INFO))
          type = MBeanOperationInfo.ACTION_INFO;
        else   
          if(typeName.equals(TYPE_UNKNOWN))
            type = MBeanOperationInfo.UNKNOWN;
          else {
			String msg = localStrings.getString( "admin.server.core.mbean.meta.parseattributedescrline_wrong_exception_format", EXCEPTION_UNKNOWN_IMPACT_TYPE, descrLine );
            throw new MBeanConfigException( msg );
		  }
      
    //DESCRIPTION
    String beanName  = getPureClassName(configuringMBeanClass);
    String defaultName = beanName+"."+name+".operation";
    if(flds.length<3 || flds[2].length()==0)
      descr = getResourceString(defaultName, defaultName);
    else
      descr = getResourceString(flds[2], defaultName);
       

    //REFLECTION
    Class[] signature = new Class[params.length/3];
    for(int i=0; i<signature.length; i++)
      signature[i] = (Class)params[i*3];
    Method method;
    try {
      method  = configuringMBeanClass.getMethod(name, signature);
    } catch (NoSuchMethodException e)
    {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.parseoperationdescrline_wrong_exception_format", EXCEPTION_OPER_NOT_FOUND, descrLine );
      throw new MBeanConfigException( msg );
    }
    MBeanParameterInfo[] infos = new MBeanParameterInfo[signature.length];
    for(int i=0; i<signature.length; i++)
      {
        String pName = (String)params[i*3+1];
        if(pName==null)
           pName = ""; //get default name ?
        String pDescr = (String)params[i*3+2];
        if(pDescr==null) //get default descr ?
           {
              if(pName.length()>0)
                 pDescr = beanName+"."+name+"."+pName+".parameter";
              else
                 pDescr = "";
           }
        infos[i] = new MBeanParameterInfo(pName, signature[i].getName(), pDescr);
      }
    return new MBeanOperationInfo[] {new MBeanOperationInfo(name, descr, infos, method.getReturnType().getName(), type)};   
    

//    Method[] methods  = getMethodsForName(configuringMBeanClass, name);
//    MBeanOperationInfo[] infos = new MBeanOperationInfo[methods.length];
//    for(int i=0; i<methods.length; i++)
//        infos[i] = new MBeanOperationInfo(descr, methods[i]);
        //NOTE: ACTION/INFO operation type is not used by this constructor. To do later.
//    return infos;
  }

  //****************************************************************************************************************
  // extracts up to <nMAx> fields separated by line delimiter
  // only nMax-th field can contain unmasked delimiters
  static private String[] getLineFields(String line, int nMax)
  {    
    ArrayList flds = new ArrayList();
    int idx1=0, idx2=100;
    if(line==null || line.length()==0 || nMax==0)
      return null;
    while(idx2>0 && nMax>flds.size() && idx1<line.length())
      {
        idx2 = line.indexOf(DESCR_LINE_DELIMITER, idx1);
        if(idx2<0)
          flds.add(line.substring(idx1).trim());
        else
          flds.add(line.substring(idx1, idx2).trim());
        idx1 = idx2 + 1;
        if(idx2==line.length())
           flds.add("");
      }
    String[] strs = new String[flds.size()];
    for(int i=0; i<strs.length; i++)
      {
        strs[i] = (String)flds.get(i);
      }
    return strs;
  }

  //********************************************************************************************************************
  static String getResourceString(String resourceID, String nullReplacer)
  {
    return resourceID; //TEMPORARY FOP TEST
    /* should be discussed with Kedar
       String res = null;
       try {
         PropertiesStringSource propSource = new PropertiesStringSource(STRINGSOUREC_FILENAME);
         res = propSource.getString(resourceID);
         if(res!=null)
            return res;
         if(nullReplacer!=null)
            return nullReplacer;
         else
            return resourceID;
       } catch(IOException e) {
         if(nullReplacer!=null)
            return nullReplacer;
         else
            return resourceID;
       }
  */
  }

  //*********************************************************************************************************************
  static private Method[] getMethodsForName(Class configuringMBeanClass, String name)
  {
    ArrayList overloads = new ArrayList();
    Method[] all = configuringMBeanClass.getMethods();
    if(all==null)
      return null;
    for(int i=0; i<all.length; i++)
      {
        if(name.equals(all[i].getName()))
          overloads.add(all[i]);
      }
    if(overloads.size()==0)
      return null;
    Method[] ret = new Method[overloads.size()];
    for(int i=0; i<ret.length; i++)
      ret[i] = (Method)overloads.get(i);
    return ret;
  }


  //****************************************************************************************************************
  // converts type from description to java signature style
  // 
  static private Class convertTypeToSignatureClass(String type) throws MBeanConfigException
  {    
    type = type.trim();
    int idx = type.indexOf("[");
    if(idx==0 || type.length()==0) {
	   String msg = localStrings.getString( "admin.server.core.mbean.meta.converttypetosignatureclass_wrong_exception_format", EXCEPTION_EMPTY_PARAM_TYPE, type );
       throw new MBeanConfigException( msg );
	}
    try 
    {
      if(idx>0)
        { // array
          String name = type.substring(0,idx).trim();
          if(name.length()==0) {
		    String msg = localStrings.getString( "admin.server.core.mbean.meta.converttypetosignatureclass_wrong_exception_format", EXCEPTION_EMPTY_PARAM_TYPE, type );
            throw new MBeanConfigException( msg );
		  }
          String code = getPrimitiveCode(name);
          if(code==null)
            {
              if(name.indexOf('.')<0)
                 name = "java.lang."+name;
              return Class.forName(getArrayPrefix(type, idx) + "L" + name + ";");
            }
          else
            return Class.forName(getArrayPrefix(type, idx) + code) ;
        }
      else
        {
          Class cl = getPrimitiveClass(type);
          if(cl!=null)
            return cl;
          //non-primitive plain class
          
          try {
            return Class.forName(type);
          }
          catch(ClassNotFoundException e)
          {
             if(type.indexOf('.')>0) {
				String msg = localStrings.getString( "admin.server.core.mbean.meta.converttypetosignatureclass_wrong_exception_format", EXCEPTION_WRONG_PARAM_CLASS, type );
                throw new MBeanConfigException( msg );
			 }
          }
          //here we are if no . in name
          return Class.forName("java.lang."+type);
        }
    } 
    catch(ClassNotFoundException e)
    {
	  String msg = localStrings.getString( "admin.server.core.mbean.meta.converttypetosignatureclass_wrong_exception_format", EXCEPTION_WRONG_PARAM_CLASS, type );
      throw new MBeanConfigException( msg );
    }
    
  }

  //****************************************************************************************************************
  static private String getArrayPrefix(String line, int idx)  throws MBeanConfigException
  {
    String pref = "";
    int dimCheck = 0;
    for( ; idx<line.length(); idx++)
      {
        if(line.charAt(idx)=='[')
          pref = pref + "[";
        else
          if(line.charAt(idx)==']')
            dimCheck++;
      }
    if(pref.length()!=dimCheck) {
	   String msg = localStrings.getString( "admin.server.core.mbean.meta.getarrayprefix_wrong_exception_format", EXCEPTION_WRONG_PARAM_DIM, line );
       throw new MBeanConfigException( msg );
	}
    return pref;
  }

  //****************************************************************************************************************
  static Object[][] convTable = new Object[][] {
    {"byte",    "B",    Byte.TYPE,        "java.lang.Byte"},
    {"char",    "C",    Character.TYPE,   "java.lang.Character"},
    {"double",  "D",    Double.TYPE,      "java.lang.Double"},
    {"float",   "F",    Float.TYPE,       "java.lang.Float"},
    {"int",     "I",    Integer.TYPE,     "java.lang.Integer"},
    {"long",    "J",    Long.TYPE,        "java.lang.Long"},
    {"short",   "S",    Short.TYPE,       "java.lang.Short"},
    {"boolean", "Z",    Boolean.TYPE,     "java.lang.Boolean"},
    {"void",    "V",    Void.TYPE,        "?"} };
    
  //****************************************************************************************************************
  static private Object findConvTableElemByType(String type, int idx)
  {
     for(int i=0; i<convTable.length; i++)
        {
           if(type.equals((String)convTable[i][0]))
              return convTable[i][idx];
        }
     return null;
  }    

  //****************************************************************************************************************
  static private Object findConvTableElemByAssoClassName(String assoClass, int idx)
  {
     for(int i=0; i<convTable.length; i++)
        {
           if(assoClass.equals((String)convTable[i][3]))
              return convTable[i][idx];
        }
     return null;
  }    

  //****************************************************************************************************************
  static private String getPrimitiveCode(String type)
  {
     return (String)findConvTableElemByType(type, 1);
  }    
      
  //****************************************************************************************************************
  static private Class getPrimitiveClass(String type)
  {
     return (Class)findConvTableElemByType(type, 2);
  }    
      
  //****************************************************************************************************************
  static private Class getRelatedPrimitiveClass(String className)
  {
     return (Class)findConvTableElemByAssoClassName(className, 2);
  }    
     
  //****************************************************************************************************************
  // extracts parameters description
  // 
  static private Object[] decomposeParametersDescription(String line) throws MBeanConfigException
  {    
    ArrayList flds = new ArrayList();
    int idx1=0, idx2=100;
    line = line.trim();
    int len = line.length();
    int parmEnd;
    if(len==0)
      return new Object[0];
    String type;
    while(idx1<len)
      {
        if((parmEnd=line.indexOf(',', idx1))<0)
           parmEnd = len;
        //TYPE
        idx2 = line.indexOf(' ', idx1);
        if(idx2>parmEnd)
           idx2=parmEnd;
        if(idx2<0)
          {
            flds.add(convertTypeToSignatureClass(line.substring(idx1)));
            idx2 = parmEnd;
          }
        else
          flds.add(convertTypeToSignatureClass(line.substring(idx1,idx2)));
        idx1 = idx2 + 1;
        while(idx1<parmEnd && line.charAt(idx1)==' ')
          idx1++;
        if(idx1>=parmEnd)
          {
            flds.add(null); //name
            flds.add(null); //descr
            if(idx1==parmEnd)
               idx1++;
            while(idx1<len && line.charAt(idx1)==' ')
              idx1++;
            continue;
          }
        
        //NAME
        idx2 = line.indexOf(' ', idx1);
        if(idx2>parmEnd)
           idx2=parmEnd;
        if(idx2<0)
          {
            flds.add(line.substring(idx1).trim());
            idx2 = parmEnd;
          }
        else
          flds.add(line.substring(idx1,idx2).trim());
        idx1 = idx2 + 1;
        while(idx1<parmEnd && line.charAt(idx1)==' ')
          idx1++;
        if(idx1>=parmEnd)
          {
            flds.add(null); //descr
            if(idx1==parmEnd)
               idx1++;
            while(idx1<len && line.charAt(idx1)==' ')
              idx1++;
            continue;
          }
        
        // DESCRIPTION
        idx2 = line.indexOf(',', idx1);
        if(idx2<0)
          {
            flds.add(line.substring(idx1).trim());
            idx2 = len;
          }
        else
          flds.add(line.substring(idx1,idx2).trim());
        idx1 = idx2 + 1;
        while(idx1<len && line.charAt(idx1)==' ')
          idx1++;
      }

    Object[] strs = new Object[flds.size()];
    for(int i=0; i<strs.length; i++)
      {
        strs[i] = flds.get(i);
      }
    return strs;
  }
  //****************************************************************************************************************
  private static String getPureClassName(Class configuringMBeanClass)
  {
     if(configuringMBeanClass==null)     
        return null;
     String className = configuringMBeanClass.getName();
     int idx = className.lastIndexOf('.');
     if(idx>=0)
        return className.substring(idx+1);
     return className;
  }
}


