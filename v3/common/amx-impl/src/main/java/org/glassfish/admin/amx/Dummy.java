package org.glassfish.admin.amx;

import javax.management.ObjectName;
import javax.management.DynamicMBean;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;

import org.jvnet.hk2.config.ConfigBean;


/**
 * @author llc
 */
final class Dummy implements DynamicMBean
{
    final ConfigBean mConfigBean;
    
    public Dummy( final ConfigBean cb )
    {
        mConfigBean = cb;
    }
    
    public Object getAttribute(String attribute)
        throws MBeanException, AttributeNotFoundException
    {
        throw new AttributeNotFoundException();
    }
    
    public MBeanInfo getMBeanInfo()
    {
        return new MBeanInfo( this.getClass().getName(), "dummy", null, null, null, null );
    }
    

    public AttributeList getAttributes(String[] attributes)
    {
        return new AttributeList();
    }
    
    	public void
	setAttribute( final Attribute attr )
		throws AttributeNotFoundException, InvalidAttributeValueException
	{
        throw new AttributeNotFoundException( attr.getName() );
    }
    
    	public AttributeList
	setAttributes( final AttributeList attrs )
	{
		final int			numAttrs	= attrs.size();
		final AttributeList	successList	= new AttributeList();
		
		for( int i = 0; i < numAttrs; ++i )
		{
			final Attribute attr	= (Attribute)attrs.get( i );
			try
			{
				setAttribute( attr );
				successList.add( attr );
			}
			catch( Exception e )
			{
				// ignore, as per spec
			}
		}
		return( successList );
	}
    
        public Object
    invoke(String actionName, Object[] params, String[] signature)
    {
        throw new UnsupportedOperationException( actionName );
    }
}
