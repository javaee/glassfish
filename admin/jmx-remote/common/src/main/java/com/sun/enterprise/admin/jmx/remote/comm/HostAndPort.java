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

package com.sun.enterprise.admin.jmx.remote.comm;
//JDK imports
import java.util.StringTokenizer;
import java.io.Serializable;

/** Represents a host and a port in a convenient package that also
 * accepts a convenient constructor.
 * @author Lloyd Chambers 
 * @since S1AS7.0
 * @version 1.1
 */
public class HostAndPort implements Serializable
{
	/* javac 1.3 generated serialVersionUID */
	public static final long	serialVersionUID			= 6708656762332072746L;
	protected String			mHost						= null;
	protected int				mPort;
  private boolean secure = false;


  public HostAndPort(String host, int port, boolean secure){
	this.mHost = host;
	this.mPort = port;
	this.secure = secure;
  }
  
  public HostAndPort(HostAndPort rhs){
	this(rhs.mHost, rhs.mPort, rhs.secure);
  }

	public HostAndPort( String host, int port ) {
	  this(host, port, false);
	}

  public boolean isSecure(){
	return this.secure;
  }
  
	
	
		public String
	getHost()
	{
		return( mHost );
	}
	
		public int
	getPort()
	{
		return( mPort );
	}
	
	/**
		Construct a new HostAndPort from a string of the form "host:port"
		
		@param	str	string of the form "host:port"
	*/
	public HostAndPort( String str )
	{
		StringTokenizer	tokenizer	=
			new StringTokenizer( str, ":", false);
		
		mHost	= tokenizer.nextToken();
		
		final String portString	= tokenizer.nextToken();
		mPort	= new Integer( portString ).intValue();
	}
	
		public String
	toString()
	{
		return( mHost + ":" + mPort );
	}
}
