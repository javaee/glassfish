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

package com.sun.enterprise.admin.common.domains.registry;
import java.io.Serializable;
import java.lang.CloneNotSupportedException;

/**
   Instances of this class represent the minimum data needed to
   contact an adminstration server remotely.
   <p>
   Instances of this class are immutable.
   @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
   @version $Revision: 1.4 $
*/


public class ContactData implements Cloneable, Serializable
{

	  /**
		 Construct an instance from the given arguments.
		 @param host the host name on which the admin server is
		 running.
		 @param port the port on which the admin server can be
		 contacted
		 @param useSSL indicate whether SSL should be used to contact
		 the admin server
		 @throws NullPointerException if either host or port are null
	  */
  public ContactData(String host,
					 String port,
					 boolean useSSL) throws NullPointerException {
	
	checkNull(host);
	this.host = host;
	checkNull(port);
	this.port = port;
	this.useSSL = useSSL;
  }

	  /**
		 Get the host of the receiver
		 @return the host of the receiver
	  */
  public String getHost(){
	return this.host;
  }

	  /**
		 Get the port of the receiver
		 @return the port of the receiver
	  */
  public String getPort(){
	return this.port;
  }
	/**
	   Indicate if ssl should be used to communicate to the admin server
	   @return true iff ssl should be used to communicate to the admin
	   server
	*/
  public boolean useSSL(){
	return this.useSSL;
  }

  public Object clone(){
	try {
	  return super.clone();
	}
	catch (CloneNotSupportedException e){ return null;}
  
  }

  public int hashCode(){
	final String s = host+port+(useSSL ? "t" : "f");
	return s.hashCode();
  }

  private void checkNull(String s) throws NullPointerException{
	if (s == null) {
	  throw new NullPointerException();
	}
  }
  
  private String host;
  private String port;
  private boolean useSSL;
}
