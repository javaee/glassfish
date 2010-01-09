/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admin.cli.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters following options from the input parameters- 
 * --target, --host and --port
 *
 * @author Rajeshwar Patil 
 */

public class OptionsFilter {

    public Object[] getFilteredParameters(String args[]) {
        // filter out --host parameter
        if (args.length > 0) {
            boolean ignore = false;
            String hosts = null;
            List<String> parameters = new ArrayList<String>();
            for (String str: args) {
                //check if the parameter is the --host value to be ignored  
                if (ignore) { 
                    ignore = false;
                    continue;
                }

                //skip the --target,--host or --port option
                if ((str.startsWith("--target")) ||
                    (str.startsWith("--host")) || (str.startsWith("--port"))) {
                    if (str.contains("=")) {
                        continue;
                    } else {
                        //ignore the next input parameter which can be 
                        //--target, --host or --port parameter value
                        ignore = true;
                        continue;
                    }
                } else {
                    //add the parameter to the non-host parameter list
                    parameters.add(str);
                }
            }

            Object[] parameterStrings = parameters.toArray(); 
            for (Object parameter: parameterStrings) { 
                System.out.print(parameter + " ");
            }
            return parameterStrings;
        }
        return null;
    }


    public static void main (String args[]) {
        try {
            OptionsFilter filter = new OptionsFilter();
            filter.getFilteredParameters(args); 
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
