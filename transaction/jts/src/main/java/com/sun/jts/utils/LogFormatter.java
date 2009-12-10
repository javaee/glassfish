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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.jts.utils;


import org.omg.CosTransactions.*;
import javax.transaction.xa.*;
import java.util.*;
import java.util.logging.*;
import java.text.*;

/**
 * This class is used to format the trace record. 
 *
 * @author <a href="mailto:k.venugopal@sun.comi,kannan.srinivasan@sun.com">K Venugopal</a>
 * @version 1.0
 */
public class LogFormatter 
{
  /**
   * Helper method to convert a byte arror to string. This is typically used for printing Xids.
   *
   * @param byteArray a <code>byte[]</code> value
   * @return a <code>String</code> value
   */
  
    public static String convertToString(byte[] byteArray)
    {
        int i;
        StringBuffer strBuf=new StringBuffer();
        for(i = 0; i < byteArray.length; i++)
         {
            strBuf.append(byteArray[i]);    
        }    
        return strBuf.toString();
    }

  /**
   * Converts an array of xids to string that can be printed. Its a helper method.
   *
   * @param xidArray a <code>Xid[]</code> value
   * @return a <code>String</code> value
   */
    public static String convertXidArrayToString(Xid[] xidArray)
    {
	if(xidArray.length != 0)
	{
        	int i;
        	StringBuffer strBuf = new StringBuffer("Xid class name is " + xidArray[0].getClass().getName() + " Number of Xids are " + xidArray.length + " [ ");
        	for(i = 0; i < xidArray.length - 1; i++)
        	{
            		strBuf.append(xidArray[i]).append("\n");
        	}
        	strBuf.append(xidArray[xidArray.length - 1]).append(" ]");
        	return strBuf.toString();
	}
	else
		return " null ";
    }

  /**
   * Helper method to convert properties to string.
   *
   * @param prop a <code>Properties</code> value
   * @return a <code>String</code> value
   */
    public static String convertPropsToString(Properties prop)
    {
        if(prop==null){
	    return "{null}";
        }
        StringBuffer strBuf =  new StringBuffer("{ ");
        for(Enumeration enum1 = prop.propertyNames(); enum1.hasMoreElements(); )
        {
            Object obj = enum1.nextElement();
            strBuf.append("[ ").append(obj).append("->");
            Object val=prop.getProperty((String)obj);
            if(val==null)
	        strBuf.append("null");
            else
                strBuf.append((String)val);        
            strBuf.append(" ] ");
        }        
        strBuf.append("}");
        return strBuf.toString();
    }

	/**
	    getLocalizedMessage is used to localize the messages being used in
		exceptions
	**/

	public static String getLocalizedMessage(Logger logger , String key){
		try{
			ResourceBundle rb = logger.getResourceBundle();
			String message = rb.getString(key);
			return message;
		}catch ( Exception ex){
			logger.log(Level.FINE,"JTS:Error while localizing the log message");
			return key;
		}
	}
	
	/**
	    getLocalizedMessage is used to localize the messages being used in
		exceptions with arguments inserted appropriately.
	**/

	public static String getLocalizedMessage(Logger logger , String key,
											Object[] args){
		try{
			ResourceBundle rb = logger.getResourceBundle();
			String message = rb.getString(key);
			return MessageFormat.format(message,args);
		}catch ( Exception ex){
				logger.log(Level.FINE,"JTS:Error while localizing the log message");
			return key;
		}
	}
}
