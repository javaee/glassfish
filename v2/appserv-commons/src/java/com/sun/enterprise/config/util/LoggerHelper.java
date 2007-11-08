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
 * LogHelper.java
 *
 * Created on December 13, 2000, 9:20 AM
 */

package com.sun.enterprise.config.util;

/**
 * LoggerHelper is needed since actual logger
 * may not be available during the server startup
 * This class does the switching to actual logger
 * when the time is right.
 *
 * @author  sridatta
 */
public class LoggerHelper {
    
    /** Creates a new instance of LogHelper */
    public LoggerHelper() {
    }
    
    public static void fine(String message) {
        //tbd FIXME NYI
//        System.out.println(message);
    }
    
    public static void finer(String message) {
        //tbd FIXME NYI
 //       System.out.println(message);
    }
    
    public static void finest(String message) {
        //tbd FIXME NYI
  //      System.out.println(message);
    }
    
    public static void info(String message) {
        //tbd FIXME NYI
        //System.out.println(message);
    }
    
    public static void info(String message, Throwable exception) {
        System.out.println(message);
        exception.printStackTrace();
    }
    
    public static void fine(String message, Throwable exception) {
        System.out.println(message);
        exception.printStackTrace();
    }
    public static void error(String message, Throwable exception) {
        System.out.println(message);
        exception.printStackTrace();
    }
    
     public static void warning(String message, Throwable exception) {
        System.out.println(message);
        exception.printStackTrace();
    }
     
       public static void warning(String message) {
        System.out.println(message);
    }
       
    
}
