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
 * sample.java        May 8, 2003, 11:23 AM
 */

package com.sun.enterprise.tools.common.validation.samples.simple;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;

import com.sun.enterprise.tools.common.validation.samples.simple.beans.*;
import com.sun.enterprise.tools.common.validation.util.BundleReader;
import com.sun.enterprise.tools.common.validation.util.Display;
import com.sun.enterprise.tools.common.validation.util.Utils;
import com.sun.enterprise.tools.common.validation.ValidationManager;
import com.sun.enterprise.tools.common.validation.ValidationManagerFactory;



/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class Application {
    
    /** Creates a new instance of sample */
    public Application() {
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RootElement rootElement =  null;
        
        String fileBeingValidated = "com/sun/enterprise/tools/" +       //NOI18N
            "common/validation/samples/simple/simple.xml";              //NOI18N

        String validationFile = "com/sun/enterprise/tools/" +           //NOI18N
            "common/validation/samples/simple/validation.xml";          //NOI18N
///    String validationFile = "C:/testframe/tests/com/sun/" +          //NOI18N
///        "enterprise/tools/common/validation/samples/simple/" +       //NOI18N
///            "validation.xml";                                        //NOI18N


//      You should set impl.file to fully qualified file name of the 
//      impl(implementation) file
///        String implFile = "com.sun.enterprise.tools." +              //NOI18N
///            "common.XYZImpl";                                        //NOI18N
///     System.setProperty("impl.file", implFile);                      //NOI18N


//      You can set constraints.file to either absolute or relative path of the
//      Constraints file
///     String cosntriantsFile = "com/sun/enterprise/tools/" +          //NOI18N
///         "common/testXYZ.xml";                                       //NOI18N
///     String cosntriantsFile = "C:/testframe/src/java/com/sun/" +     //NOI18N
///         "enterprise/tools/XYZ/testXYZ.xml";                         //NOI18N
///     System.setProperty("constraints.file", cosntriantsFile);        //NOI18N


        //Create an InpurtStream object
        Utils utils = new Utils();
        InputStream inputStream = utils.getInputStream(fileBeingValidated);

        //Create graph
        if(inputStream != null) {
            try {
                rootElement = RootElement.createGraph(inputStream);
            } catch(Exception e) {
               System.out.println(e.getMessage());
            }
        } else {
            String format = 
                BundleReader.getValue("MSG_Unable_to_use_file");        //NOI18N
            Object[] arguments = new Object[]{fileBeingValidated};
            System.out.println(MessageFormat.format(format, arguments));
        }

        if(rootElement != null){
            ValidationManagerFactory validationManagerFactory = 
                new ValidationManagerFactory();

            //you can pass either absolute or relative path of the validation
            //file to getValidationManager()
            ValidationManager validationManager = 
                validationManagerFactory.getValidationManager(validationFile);

            Collection failures =
                validationManager.validate(rootElement);

            Display display = new Display();
            display.text(failures);
            display.gui(failures);
        }
    }
}
