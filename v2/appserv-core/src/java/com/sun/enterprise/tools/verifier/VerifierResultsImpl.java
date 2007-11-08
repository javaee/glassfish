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

package com.sun.enterprise.tools.verifier;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.logging.*;
import com.sun.logging.*;

/**
 * Results of a Verifier Invocation from Backend
 * @author
 */
public  class VerifierResultsImpl implements VerifierResults {

  static Logger _logger=LogDomains.getLogger(LogDomains.APPVERIFY_LOGGER);

  private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(VerifierResultsImpl.class);

  private static Class resultsReportClass=null;
  private static Method failedCountMethod=null;
  private static Method errorCountMethod=null;
  private static Method warningCountMethod=null;

  private Object resultObj;

  public VerifierResultsImpl(Object result) {
     resultObj = result;
  }

 /** return number of  failures in verification */
 public int getFailedCount() {

    init();
    if ((failedCountMethod == null) || (resultObj == null)) {
       return -1;
    }
    String name = resultsReportClass.getName();

    try {
        Object result = failedCountMethod.invoke(resultObj, (Object[])null);
        return ((Integer)result).intValue();
     } catch (IllegalAccessException e) {
        _logger.log(Level.SEVERE,"verification.class.access.error", new Object[] {name});
     } catch (InvocationTargetException e) {
        _logger.log(Level.SEVERE,"verification.method.error", new Object[] {"verifyEar",
                                e.getMessage()});
     }
  
  return -1;
 }

 /** return number of  warnings in verification */
 public int getWarningCount() {
    init();
    if ((warningCountMethod == null) || (resultObj == null)) {
       return -1;
    }
    String name = resultsReportClass.getName();

    try {
        Object result = warningCountMethod.invoke(resultObj, (Object[])null);
        return ((Integer)result).intValue();
     } catch (IllegalAccessException e) {
        _logger.log(Level.SEVERE,"verification.class.access.error", new Object[] {name});
     } catch (InvocationTargetException e) {
        _logger.log(Level.SEVERE,"verification.method.error", new Object[] {"verifyEar",
                                e.getMessage()});
     }
  
  return -1;
 }

 /** return number of  errors in verification */
 public int getErrorCount() {
    init();
    if ((errorCountMethod == null) || (resultObj == null)) {
       return -1;
    }

    String name = resultsReportClass.getName();

    try {
        Object result = errorCountMethod.invoke(resultObj, (Object[])null);
        return ((Integer)result).intValue();
     } catch (IllegalAccessException e) {
        _logger.log(Level.SEVERE,"verification.class.access.error", new Object[] {name});
     } catch (InvocationTargetException e) {
        _logger.log(Level.SEVERE,"verification.method.error", new Object[] {"verifyEar",
                                e.getMessage()});
     }
  
  return -1;
 }

 private void init() {

    if (resultsReportClass != null) {
           return;
    }
    String name = null;
    try {
         name = System.getProperty("j2ee.verifier.ResultsReport",
         "com.sun.enterprise.tools.verifier.ResultsReport");
         resultsReportClass = Class.forName(name);
         warningCountMethod = resultsReportClass.getDeclaredMethod("getWarningCount", (Class[])null);
         failedCountMethod = resultsReportClass.getDeclaredMethod("getFailedCount", (Class[])null);
         errorCountMethod = resultsReportClass.getDeclaredMethod("getErrorCount", (Class[])null);

     } catch (ClassNotFoundException e) {
        _logger.log(Level.SEVERE,"verification.class.notfound", new Object[] {name});
     }
    catch (NoSuchMethodException e) {
      _logger.log(Level.SEVERE,"verification.method.notfound", new Object[] {e.getMessage() , name});
     }
 }

}

