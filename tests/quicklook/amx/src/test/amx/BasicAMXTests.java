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
 
package amxtest;

import org.testng.annotations.Test;

import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.Attribute;
import javax.management.AttributeList;

import com.sun.appserv.management.DomainRoot;

import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Extra;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.SystemStatus;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.TimingDelta;
import com.sun.appserv.management.util.jmx.JMXUtil;


import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.JDBCConnectionPoolConfig;

import java.util.Set;
import java.util.Map;
import java.util.List;

/** 
	Basic AMX tests that verify connectivity and ability to
	traverse the AMX hierarchy and fetch all attributes.
 */
//@Test(groups={"amx"}, description="AMX tests", sequential=false, threadPoolSize=5)
@Test(groups={"amx"}, description="AMX tests")
public final class BasicAMXTests extends AMXTestBase {
	public BasicAMXTests()
	{
	}
	
    //@Test(timeOut=15000)
    public void bootAMX() throws Exception
    {
    	final DomainRoot domainRoot = getDomainRoot();
    	
    	// one basic call to prove it's there...
    	domainRoot.getAppserverDomainName();
    }
    
    @Test(dependsOnMethods="bootAMX")
    public void iterateAllSanityCheck()
    {
    	final TimingDelta timing = new TimingDelta();
    	final TimingDelta overall = new TimingDelta();
    	
    	final Set<AMX> all = getAllAMX();
    	assert all.size() > 20;
    	//debug( "BasicAMXTests: millis to get queryAllSet(): " + timing.elapsedMillis() );
    	
    	for( final AMX amx : all )
    	{
    		// if it's a Container, verify that containees can be fetched
    		if ( amx instanceof Container )
    		{
    			final Container c = (Container)amx;
    			final Set<String> j2eeTypes = c.getContaineeJ2EETypes();
    			final Map<String,Map<String,AMX>> containeeMap = c.getMultiContaineeMap(j2eeTypes);
    		}
    		//debug( "BasicAMXTests: millis to get getMultiContaineeMap(): " + timing.elapsedMillis() );
    		
    		// verify that all the attributes can be accessed
    		final Extra extra = Util.getExtra(amx);
    		final ObjectName objectName = extra.getObjectName();
    		final String[] attrNames = extra.getAttributeNames();
    		final Map<String,Object> attrsMap = extra.getAllAttributes();
    		
    		final Set<String> missing = GSetUtil.newStringSet(attrNames);
    		missing.removeAll(attrsMap.keySet());
    		if ( missing.size() != 0 )
    		{
    			final String missingStr = CollectionUtil.toString(missing, ", ");
    			System.err.println( "WARNING: could not retrieve attributes: {" + missingStr +
    				"} for MBean " + JMXUtil.toString(objectName));
    		}
    		else
    		{
    			//System.out.println( attrsMap.keySet().size() + " attrs fetched ok: " + JMXUtil.toString(Util.getExtra(amx).getObjectName()) );
    		}
    		//debug( "BasicAMXTests: millis to get verify attributes: " + timing.elapsedMillis() );
    	}
    	//debug( "BasicAMXTests.iterateAllSanityCheck() millis: " + overall.elapsedMillis() );
    }
    
    /*
	if ( getEffortLevel() == EffortLevel.EXTENSIVE )
	{
		...
	}
    */
    
    @Test(dependsOnMethods="bootAMX")
    public void iterateParentChild()
    {
    	final Set<AMX> all = getAllAMX();
    	for( final AMX amx : all )
    	{
    		_checkParentChild(amx);
    	}
    }
    private void _checkParentChild( final AMX amx )
    {
    	if ( ! (amx instanceof DomainRoot) )
    	{
    		final Container c = amx.getContainer();
    		final Set<AMX> s = c.getContaineeSet(amx.getJ2EEType());
    		assert s.contains(amx);
    		
    		final Map<String,AMX> m = c.getContaineeMap(amx.getJ2EEType());
    		assert m.containsKey(amx.getName());
    	}
    }
    
    
    
    @Test(dependsOnMethods="bootAMX")
    public void iterateContainer()
    {
    	final Set<Container> all = getAll(Container.class);
    	for( final Container amx : all )
    	{
    		_checkContainer( amx );
    	}
    }
    private void _checkContainer( final Container c )
    {
    	final Set<String> j2eeTypes = c.getContaineeJ2EETypes();
    	
    	// check equivalency of null and complete set of j2eeTypes
    	final Map<String,Map<String,AMX>> m1 = c.getMultiContaineeMap(j2eeTypes);
    	final Map<String,Map<String,AMX>> m2 = c.getMultiContaineeMap(null);
    	assert m1.keySet().equals(m2.keySet());
    	
    	// just verify it can be calle
    	final Set<AMX> s1 = c.getContaineeSet(j2eeTypes);
    	
    	// verified that every containee can be fetched by type and name
    	for( final String j2eeType : j2eeTypes )
    	{
    		final Set<AMX> byType = c.getContaineeSet(j2eeType);
    		for( final AMX amx : byType )
    		{
    			final AMX a = c.getContainee(j2eeType,amx.getName());
    			assert a == amx;
    		}
    	}
    }
    
    
    
    @Test(dependsOnMethods="bootAMX")
    public void iterateDefaultValues()
    {
    	final Set<AMXConfig> all = getAll(AMXConfig.class);
    	for( final AMXConfig amx : all )
    	{
    		_checkDefaultValues( amx );
    	}
    }
    
    private void _checkDefaultValues( final AMXConfig amxConfig )
    {
    	final String objectName = JMXUtil.toString(Util.getExtra(amxConfig).getObjectName());
    	
    	// test the Map keyed by XML attribute name
    	final Map<String,String> defaultValuesXML = amxConfig.getDefaultValues(false);
    	for( final String attrName : defaultValuesXML.keySet() )
    	{
    		// no default value should ever be null
    		assert defaultValuesXML.get(attrName) != null :
    			"null value for attribute " + attrName + " in " + objectName;
    			
    		final String value = amxConfig.getDefaultValue(attrName);
    		assert value != null :
    			"null value for XML attribute fetched singly: " + attrName + " in " + objectName;
    	}
    	
    	// test the Map keyed by AMX attribute name
    	final Map<String,String> defaultValuesAMX = amxConfig.getDefaultValues(true);
    	assert defaultValuesXML.size() == defaultValuesAMX.size();
    	for( final String attrName : defaultValuesAMX.keySet() )
    	{
    		// no default value should ever be null
    		assert defaultValuesAMX.get(attrName) != null :
    			"null value for attribute " + attrName + " in " + objectName;
    			
    		final String value = amxConfig.getDefaultValue(attrName);
    		assert value != null :
    			"null value for AMX attribute fetched singly: " + attrName + " in " + objectName;
    	}
    }
    
    
    @Test(dependsOnMethods="bootAMX")
    public void iterateAttributeResolver()
    {
    	final Set<AMXConfig> all = getAll(AMXConfig.class);
    	for( final AMXConfig amx : all )
    	{
    		_checkAttributeResolver( amx );
    	}
    }
    private void _checkAttributeResolver( final AMXConfig amxConfig )
    {
    	final String[] attrNames = Util.getExtra(amxConfig).getAttributeNames();
    	for( final String attrName : attrNames )
    	{
    		final String resolvedValue = amxConfig.resolveAttribute(attrName);
    		if ( resolvedValue != null )
    		{
    			// crude check
    			assert resolvedValue.indexOf("${") < 0 : 
    				"Attribute " + attrName + " did not resolve: " + resolvedValue;
    		}
    	}
    	
    	final AttributeList attrsList = amxConfig.resolveAttributes(attrNames);
    	for( final Object o : attrsList )
    	{
    		final Attribute a = (Attribute)o;
    		final String resolvedValue = "" + a.getValue();
    		if ( resolvedValue != null )
    		{
    			// crude check
				assert resolvedValue.indexOf("${") < 0 : 
					"Attribute " + a.getName() + " did not resolve: " + resolvedValue;
    		}
    	}
    }
    
    @Test(dependsOnMethods="bootAMX")
    public void testSystemStatus()
    {
    	final SystemStatus ss = getDomainRoot().getSystemStatus();
    	
    	final List<Object[]> changes = ss.getRestartRequiredChanges();
    	
    	final Set<JDBCConnectionPoolConfig> pools = getQueryMgr().queryJ2EETypeSet(JDBCConnectionPoolConfig.J2EE_TYPE);
    	
    	for( final JDBCConnectionPoolConfig pool : pools )
    	{
    		final Map<String,Object> result = ss.pingJDBCConnectionPool( pool.getName() );
    	}
    }
}





























