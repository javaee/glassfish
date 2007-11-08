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

package com.sun.enterprise.admin.common;

//JMX imports
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import com.sun.enterprise.admin.util.ArgChecker;
import com.sun.enterprise.admin.common.exception.*;
import com.sun.enterprise.admin.common.ObjectNameHelper;

public class JMXExceptionTranslator
{
    private static final String NULL_ARGUMENT = "null-arg";

    public static AFException translate(Exception e)
    {
        ArgChecker.check((e != null), NULL_ARGUMENT);

        AFException afe = new AFException(e.getMessage());
        if (e instanceof javax.management.MBeanException)
        {
            Exception targetException = 
                ((javax.management.MBeanException)e).getTargetException();
            if (targetException instanceof AFException)
            {
                afe = (AFException) targetException;
            }
            // <addition> srini@sun.com server.xml verifier
            else if(targetException instanceof AFRuntimeException)
            {
                   throw (AFRuntimeException)targetException;
            }
	    else if (targetException instanceof javax.management.MBeanException)
	    {
                Exception excpn = 
			((javax.management.MBeanException)targetException).getTargetException();
		if (excpn != null) {
                   if (excpn instanceof javax.management.InvalidAttributeValueException) {
                      afe = new com.sun.enterprise.admin.common.exception.InvalidAttributeValueException(
				excpn.getLocalizedMessage());
		   }
		}
	    }
            // </addition>		
            else
            {
                afe = new AFOtherException(targetException);
            }
        }
        else if (e instanceof javax.management.InstanceNotFoundException)
        {
            String msg = convertInstanceNotFoundExceptionMessage(e);
            afe = new AFTargetNotFoundException(msg);
        }
        else if (e instanceof javax.management.ReflectionException)
        {
            afe = new AFOtherException(e);
        }
        else if (e instanceof javax.management.AttributeNotFoundException)
        {
            afe = new AttributeNotFoundException(e.getLocalizedMessage());
        }
        else if (e instanceof javax.management.InvalidAttributeValueException)
        {
            afe = new InvalidAttributeValueException(e.getLocalizedMessage());
        }
        else if (e instanceof java.lang.RuntimeException)
        {
            throw (java.lang.RuntimeException) e;
        }
        return afe;
    }

    private static String convertInstanceNotFoundExceptionMessage(Exception e)
    {
        /* Here we are trying to extract type and name from exception
         if message is valid ObjectName string representation */
        String type = null;
        String name = null;
        try
        {
            ObjectName objectName = new ObjectName(e.getMessage());
            type = ObjectNameHelper.getType(objectName);
            name = ObjectNameHelper.getName(objectName);
        }
        catch (MalformedObjectNameException mfone)
        {
        }
        String msg;
        if(type!=null)
        {
            if(name!=null)
                msg = type + " '" + name +"' is not found.";
            else
                msg = type + " is not found.";
        }
        else
        {
            //object name is malformed - leave original message
            msg = e.getLocalizedMessage();
        }
        return msg;
    }
}
