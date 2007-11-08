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
 * PortabilityUtils.java
 *
 * Created on January 6, 2002, 3:42 PM
 */

package com.sun.enterprise.resource;

import java.util.Timer;
import java.lang.reflect.Method;
import java.util.logging.*;
// START OF IASRI 4691850
import java.lang.reflect.Field;
// END OF IASRI 4691850


/**
 *
 * @author  kg88722
 * @version 
 */
/**
 *This is a utility class.
 */
public class PortabilityUtils extends java.lang.Object {

    /** Creates new PortabilityUtils */
    public PortabilityUtils() {
    }

    // START OF IASRI 4691850
    // Create logger object per Java SDK 1.4 to log messages
    // introduced Santanu De, Sun Microsystems, March 2002

    /*static Logger _logger = null;
    static{
           _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);
          }*/
    // END OF IASRI 4691850

    /**
     * @return Timer object.
     */
    public static Timer getTimer() {
        try {
            //try to get the Class object, if we are running in iAS this will work
            Class cls = Class.forName("com.sun.enterprise.Switch");
            Class param[] = new Class[0];
            //get the getSwitch method
            Method meth = cls.getMethod("getSwitch",param);
            //invoke the getSwitch method will return the Switch
            Object obj = meth.invoke(cls,(java.lang.Object[])param);
            //get the Switch class object
            //cls = obj.getClass();
            //get the getTimer method
            meth = cls.getMethod("getTimer",param);
            //invoke the getTimer method
            obj = meth.invoke(obj,(java.lang.Object[])param);
            //return the Timer
            return (Timer)obj;
        } catch (ClassNotFoundException ex) {
             //if we are not running in iAS then create a new Timer
             return new Timer(true);
        } catch (Exception ex) {
            //_logger.log(Level.FINE,ex.toString());
            return new Timer(true);
        }
      }
}
