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

package com.sun.enterprise.appverification.factory;

import com.sun.enterprise.util.LocalStringManagerImpl;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742

/**
 *
 * @author	Qingqing Ouyang
 */
public abstract class AppVerification {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);
    // END OF IASRI 4660742
    
    private static boolean instrument_on = false;
    private static boolean redeploy = false;

    private static InstrumentLogger instrumentLogger;
    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(AppVerification.class);
    
    public static void redeploy (boolean hasRedeploy) {
        redeploy = hasRedeploy;
    }
    
    public static boolean hasRedeployed () {
        return redeploy;
    }
    
    public static void setInstrument (boolean on) {
        if (on && getInstrumentLogger() == null) {
            instrument_on = false;
	         /** IASRI 4660742
            System.out.println(
                    localStrings.getLocalString(
                            "appverification.instrument.on.failed",
                            "J2EE instrumentation implementation class NOT defined."));
            System.out.println(
                    localStrings.getLocalString(
                            "appverification.instrument.off",
                            "J2EE Application Verification is OFF..."));
	         **/
	         // START OF IASRI 4660742
					if (_logger.isLoggable(Level.FINE)){
	         _logger.log(Level.FINE,localStrings.getLocalString(
                            "appverification.instrument.on.failed",
                            "J2EE instrumentation implementation class NOT defined."));
	         _logger.log(Level.FINE,localStrings.getLocalString(
                            "appverification.instrument.off",
                            "J2EE Application Verification is OFF..."));

					}
	         // END OF IASRI 4660742
        } else {
            instrument_on = on;
            if (instrument_on) {
		            /** IASRI 4660742
                System.out.println(
                        localStrings.getLocalString(
                                "appverification.instrument.on",
                                "J2EE Application Verification {0} is ON...",
                                new Object[] {instrumentLogger.getVersion()}));
		            **/
	              // START OF IASRI 4660742
					if (_logger.isLoggable(Level.FINE))
		            _logger.log(Level.FINE,localStrings.getLocalString(
                                "appverification.instrument.on",
                                "J2EE Application Verification {0} is ON...",
                                new Object[] {instrumentLogger.getVersion()}));
	              // END OF IASRI 4660742
            }
        }
    }
    
    public static boolean doInstrument() {
        return instrument_on;
    }
    
    public static InstrumentLogger getInstrumentLogger() {
        if (instrumentLogger == null) {
            String name = null;
            Class cls = null;
            try {
                name = System.getProperty(
                        "j2ee.instrument.logger", 
                        "com.sun.enterprise.appverification.tools.InstrumentLoggerImpl");
                cls = Class.forName(name);
                if (cls != null) {
                    instrumentLogger = (InstrumentLogger)cls.newInstance();
                }
            } catch (ClassNotFoundException e) {
		            /** IASRI 4660742
                System.err.println(
                        localStrings.getLocalString(
                                "appverification.class.notfound",
                                "Class {0} not found",
                                new Object[] {name}));
		            **/
	              // START OF IASRI 4660742
		            _logger.log(Level.SEVERE,"appverification.class_notfound", new Object[] {name});
	              // END OF IASRI 4660742
            } catch (InstantiationException e) {
		            /** IASRI 4660742
                System.err.println(
                        localStrings.getLocalString(
                                "appverification.class.instantiate.error",
                                "Could not instantiate class {0}",
                                new Object[] {name}));
		            **/
	              // START OF IASRI 4660742
		            _logger.log(Level.SEVERE,"appverification.class_instantiate_error", new Object[] {name});
	              // END OF IASRI 4660742
            } catch (IllegalAccessException e) {
		            /** IASRI 4660742
                System.err.println(
                        localStrings.getLocalString(
                                "appverification.class.access.error",
                                "Could not access class {0}",
                                new Object[] {name}));
		            **/
	              // START OF IASRI 4660742
		             _logger.log(Level.SEVERE,"appverification.class_access_error", new Object[] {name});

	               // END OF IASRI 4660742
            }
        }
        return instrumentLogger;
    }
}
