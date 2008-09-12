/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.TimingDelta;
import com.sun.appserv.management.util.jmx.JMXUtil;

import java.util.Set;
import java.util.Map;

/** 
	Basic AMX tests that verify connectivity and ability to
	traverse the AMX hierarchy and fetch all attributes.
 */
public final class BasicAMXTests extends AMXTestBase {
	public BasicAMXTests()
	{
	}
	
    @Test(groups={"pulse"})	// what does "pulse" mean?
    public void bootAMX() throws Exception
    {
    	final DomainRoot domainRoot = getDomainRoot();
    	
    	// one basic call to prove it's there...
    	domainRoot.getAppserverDomainName();
    }
    
    @Test(groups={"pulse"})	// what does "pulse" mean?
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
    			System.out.println( attrsMap.keySet().size() + " attrs fetched ok: " +
    				JMXUtil.toString(Util.getExtra(amx).getObjectName()) );
    		}
    		//debug( "BasicAMXTests: millis to get verify attributes: " + timing.elapsedMillis() );
    	}
    	debug( "BasicAMXTests.iterateAllSanityCheck() millis: " + overall.elapsedMillis() );
    }
    
    /*
	if ( getEffortLevel() == EffortLevel.EXTENSIVE )
	{
		...
	}
    */
    
    @Test(groups={"pulse"})	// what does "pulse" mean?
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
    
    
    
    @Test(groups={"pulse"})	// what does "pulse" mean?
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
    
    
    
    @Test(groups={"pulse"})	// what does "pulse" mean?
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
    
    
    
    @Test(groups={"pulse"})	// what does "pulse" mean?
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
    /*
	if ( getEffortLevel() != EffortLevel.EXTENSIVE ) { return; }
    */
    
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
    
    
    
}
























