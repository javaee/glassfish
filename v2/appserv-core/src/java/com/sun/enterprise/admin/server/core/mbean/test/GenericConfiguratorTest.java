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

package com.sun.enterprise.admin.server.core.mbean.test;

//JDK imports
import java.io.*;
import java.util.*;

//JMX imports
import javax.management.*;

//Admin imports
import com.sun.enterprise.admin.common.*;
import com.sun.enterprise.admin.common.constant.*;
import com.sun.enterprise.admin.util.*;
import com.sun.enterprise.admin.server.core.jmx.*;
import com.sun.enterprise.admin.server.core.mbean.config.*;
import com.sun.enterprise.admin.server.core.mbean.meta.*;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

public class GenericConfiguratorTest
{
	

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( GenericConfiguratorTest.class );

    /**
    	@param args the command line arguments
    */
    
	public static void main (String args[])
    {
        GenericConfiguratorTest testMain = new GenericConfiguratorTest();
        testMain.test();
    }

	private void test()
	{
		try
		{
                MBeanServer		mbs				= SunoneInterceptor.getMBeanServerInstance();
  		GenericConfigurator genConf = new GenericConfigurator();
   		mbs.registerMBean(new GenConfigTestMBean("str1", 2), new ObjectName("ias:instance-name=ias1,component=orb")); //a server-instance
   		mbs.registerMBean(new GenConfigTestMBean("str3", 4), new ObjectName("ias:instance-name=ias1,component=test")); //a server-instance
//   		mbs.registerMBean(genConf, new ObjectName("ias:type=configurator")); //a server-instance
			
			AttributeList attrList = genConf.getGenericAttributes(new String[]{"server.ias1.orb.name","server.ias1.orb.port","server.ias1.orb.*"});
//      AttributeList attrList = genConf.getGenericAttributes(new String[]{"server.ias1.*.*"});
      Iterator it = attrList.iterator();
      while (it.hasNext())
        {
          Attribute attribute = (Attribute) it.next();
          String name = attribute.getName();
          Object value = attribute.getValue();
          System.out.println("Attribute name="+name+" value="+value);
        }
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
  public class GenConfigTestMBean extends AdminBase
  {
    int mInt;
    String mStr;
    public GenConfigTestMBean(String strVal, int intVal)
    {
      mStr = strVal;
      mInt = intVal;
    }
    public Object getAttribute(String attributeName) throws
                   AttributeNotFoundException, MBeanException, ReflectionException
    {
System.out.println("TEST:getAttribute("+attributeName+")");
      if(attributeName.equals("port"))
         return new Integer(mInt);
      if(attributeName.equals("name"))
         return mStr;
System.out.println("TEST:getAttribute("+attributeName+")  pt2");
      throw new AttributeNotFoundException();
    }

    public AttributeList getAttributes(String[] attributeNames)
    {
		String msg = localStrings.getString( "admin.server.core.mbean.test.getattributes_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    /**
        Every resource MBean should override this method to execute specific
        operations on the MBean.
    */
    public Object invoke(String methodName, Object[] methodParams,
        String[] methodSignature) throws MBeanException, ReflectionException
    {
    	return null;
    }

    public void setAttribute(Attribute attribute) throws
        AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException
    {
		String msg = localStrings.getString( "admin.server.core.mbean.test.setattributes_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    public AttributeList setAttributes(AttributeList parm1)
    {
		String msg = localStrings.getString( "admin.server.core.mbean.test.setattributes_not_implemented" );
        throw new UnsupportedOperationException( msg );
    }

    //***************************************************  
    //static MBean attributes and opeartions descriptions
    String[] mAttrs = 
        {
          "name, String, RW",
          "port, int, RW",
        };
    String[] mOpers = new String[0];
  	
	  /** Implementation of <code>getMBeanInfo()</code>
	      Uses helper class <code>MBeanEasyConfig</code> to construct whole MBeanXXXInfo tree.
	      @return <code>MBeanInfo</code> objects containing full MBean description.
	  */
	  public MBeanInfo getMBeanInfo()
	  {
  	  
	    try 
	      {
	        return (new MBeanEasyConfig(getClass(), mAttrs, mOpers, null)).getMBeanInfo();
	      } 
	    catch(Exception e)
	      {
	        System.out.println("++++++++++++++++++++E X C E P T I O N+++++++++++++++++++++++++ getMBeanInfo():Exception:"+e);
	        e.printStackTrace();
	        return null;
	      }
	  }
          
          /** Abstract method that subclasses have to implement. This is the way for
           * invoke method to work, through reflection.
           */
          protected Class getImplementingClass() {
              return (this.getClass());
          }
          
          /** Every resource MBean should override this method to execute specific
           * operations on the MBean. This method is enhanced in 8.0. It was a no-op
           * in 7.0. In 8.0, it is modified to invoke the actual method through
           * reflection.
           * @since 8.0
           * @see javax.management.MBeanServer#invoke
           * @see #getImplementingClass
           */
          protected Object getImplementingMBean() {
              return ( this );
          }
          
 } 
 
}
