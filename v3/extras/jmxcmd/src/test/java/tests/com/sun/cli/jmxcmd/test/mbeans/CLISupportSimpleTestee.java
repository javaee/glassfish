/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/mbeans/CLISupportSimpleTestee.java,v 1.7 2004/04/24 02:10:19 llc Exp $
 * $Revision: 1.7 $
 * $Date: 2004/04/24 02:10:19 $
 */
 
package  com.sun.cli.jmxcmd.test.mbeans;


import java.io.Serializable;
import java.net.URL;
import java.net.URI;


import javax.management.AttributeChangeNotification;
import javax.management.NotificationBroadcasterSupport;


public class CLISupportSimpleTestee 
	extends NotificationBroadcasterSupport
	implements CLISupportSimpleTesteeMBean 
	
{
		public CLISupportSimpleTesteeMBean.NonSerializable
	getNonSerializable()
	{
		return( new CLISupportSimpleTesteeMBean.NonSerializable() );
	}

	
	volatile long	mNotifMillis	= 1000;
	volatile long	mNumNotifsEmitted;
	EmitterThread	mEmitterThread;
    
		public
	CLISupportSimpleTestee()
	{
	}

		public long	
    getCurrentMillis()
    {
    	return( System.currentTimeMillis() );
    }
    
    
    	public boolean
	testboolean( boolean arg )
	{
		return( arg );
	}
	
		public Boolean
	testBoolean( Boolean arg )
	{
		return( arg );
	}
	
	public char	testchar( char arg )
	{
		return( arg );
	}
	public short	testshort( short arg )
	{
		return( arg );
	}
	public int	testint( int arg )
	{
		return( arg );
	}
	public long	testlong( long arg )
	{
		return( arg );
	}
	
	 
    	public void
	testbooleanVoid( boolean arg )
	{
		System.out.println( "" + arg );
	}
	
		public void
	testBooleanVoid( Boolean arg )
	{
		System.out.println( "" + arg );
	}
	
	public void	testcharVoid( char arg )
	{
		System.out.println( "" + arg );
	}
	public void	testshortVoid( short arg )
	{
		System.out.println( "" + arg );
	}
	public void	testintVoid( int arg )
	{
		System.out.println( "" + arg );
	}
	public void	testlongVoid( long arg )
	{
		System.out.println( "" + arg );
	}
	

		public long	
    getCurrentSeconds()
    {
    	return( System.currentTimeMillis() / 1000 );
    }
	
    	public void
   	test11ObjectArgs( String a1, Boolean a2, Character a3, Byte a4, Short a5,
    			Integer a6, Long a7, Float a8, Double a9,
    			java.math.BigInteger a10, java.math.BigDecimal a11)
    {
    	// no need to do anything
    }
    
    	public void
   	test11MixedArgs( String a1, boolean a2, char a3, byte a4, short a5,
    					int a6, long a7, float a8, double a9,
    					java.math.BigInteger a10, java.math.BigDecimal a11)
    {
    	// no need to do anything
    }
    
    	public String
   	testString( String s )
   	{
   		return( s );
   	}
   	
    	public Object
    testObject( Object obj )
    {
    	return( obj );
    }

    	public Integer
    testInteger( Integer i )    
	{
		return( i );
	}
	
		public int
    test_int( int i )
    {
    	return( i );
    }
    
		public Object []
	testObjectArray( Object [] objects )
	{
		return( objects );
	}
	
	
    	public String
    testcasesensitivity1()
   	{
   		return( "testcasesensitivity1" );
   	}
    	public String
    testCASESENSITIVITY1()
   	{
   		return( "testCASESENSITIVITY1" );
   	}
    	public String
    testCaseSensitivity1()
   	{
   		return( "testCaseSensitivity1" );
   	}
   	
    	public String
    testCaseSensitivity2()
   	{
   		return( "testCaseSensitivity2" );
   	}
   	
   	
   		public URL
    testURL( URL u )
    {
    	return( u );
    }

		public URI
    testURI( URI u )
    {
    	return( u );
    }
    
    
    private static class AintGonnaHaveIt implements Serializable
    {
    		public
    	AintGonnaHaveIt()
    	{
    	}
    };
    
    	public Object
    testUnknownType()
    {
    	return( new AintGonnaHaveIt() );
    }
    	public Object
    getUnknownType()
    {
    	return( new AintGonnaHaveIt() );
    }
	
	
//------------------------------------------------------------------------
							
    public long getNotifMillis()
    {
        return mNotifMillis;
    }

    public void setNotifMillis(long millis)
    {
       	mNotifMillis	= millis;
       	
       	if ( mEmitterThread != null )
       	{
       		// restart with new interval
       		stopNotif();
       		startNotif();
       	}
    }
    
    public long		getNotifsEmitted()
    {
    	return( mNumNotifsEmitted );
    }


    public void resetNotifsEmitted()
    {
    	mNumNotifsEmitted	= 0;
    }

    
    synchronized public void startNotif()
    {
    	stopNotif();
    	
    	System.out.println( "emitting notifications once per this many ms: " + mNotifMillis );
		mEmitterThread	= new EmitterThread( this, mNotifMillis);
		mEmitterThread.start();
    }

    public void stopNotif()
    {
    	if ( mEmitterThread != null )
    	{
    		EmitterThread thread	= mEmitterThread;
    	
    		mEmitterThread	= null;
    		try
    		{
    			thread.join( );
    		}
    		catch( InterruptedException e )
    		{
    		}
		}
    }
    
    boolean emitNotif( EmitterThread thread )
    {
    	if ( mEmitterThread == null )
    	{
    		return( false );
    	}
    		
		final AttributeChangeNotification	notif	= new AttributeChangeNotification( this,
			mNumNotifsEmitted,	// use this as sequence number
			System.currentTimeMillis(),
			"did it again",
			"NotifsEmitted",
			"java.lang.long",
			new Long( mNumNotifsEmitted ),
			new Long( mNumNotifsEmitted + 1));
		
		++mNumNotifsEmitted;
		
		sendNotification( notif );
		
		return( true );
    }
}

    
class EmitterThread extends Thread
{
	CLISupportSimpleTestee	mHost;
	final long					mSleepMillis;
	
	EmitterThread( CLISupportSimpleTestee host, long sleepMillis )
	{
		super( "Emitter Thread" );
		mHost	= host;
		mSleepMillis	= sleepMillis;
	}
	
	
		public void
	run()
	{
		boolean	keepGoing	= true;
		
		final long	sleepMillis	= mSleepMillis;
		
		while ( keepGoing )
		{
			if ( sleepMillis != 0 )
			{
				try
				{
					Thread.sleep( sleepMillis );
				}
				catch( InterruptedException e )
				{
				}
			}
			
			keepGoing	= mHost.emitNotif( this );
		}
	}
};



