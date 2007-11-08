/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3;

import java.lang.instrument.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * This agent is intended to be run prior to start up on a CMP3 JavaSE application.
 * It gets the globalInstrumentation and makes it available to TopLink's initialization code.
 * There are two kinds of initialization.  Normally, initialization occurs through reflective
 * creation and invokation of TopLink JavaSECMPInitializer.
 * It is possible to run it with the "main" argument to the agent in which case it will simply
 * try to set the globalInstrumentation field on the JavaSECMPInitializer.  This type of initialization
 * is useful when debugging, but imposes some restrictions on the user.  One such restriction is
 * that no domain classes that use lazy loading may be references in any way other than reflective in the application
 */
public class JavaSECMPInitializerAgent {
    public static void premain(String agentArgs, Instrumentation instr) throws Exception {
        // Reflection allows:
        //  JavaSECMPInitializerAgent to be the *ONLY* class is the jar file specified in -javaagent;
        //  Loading JavaSECMPInitializer class using SystemClassLoader.
        if ((agentArgs != null) && agentArgs.equals("main")) {
            initializeFromMain(instr);
        } else {
            initializeFromAgent(instr);
        }
    }
    
    public static void initializeFromAgent(Instrumentation instr) throws Exception {
            Class cls = Class.forName("oracle.toplink.essentials.internal.ejb.cmp3.JavaSECMPInitializer");
            Method method = cls.getDeclaredMethod("initializeFromAgent", new Class[] { Instrumentation.class });
            method.invoke(null, new Object[] { instr });
    }
    
    public static void initializeFromMain(Instrumentation instr) throws Exception {
            Class cls = Class.forName("oracle.toplink.essentials.internal.ejb.cmp3.JavaSECMPInitializer");
            Field field = cls.getField("globalInstrumentation");
            field.set(null, instr);        
    }
}
