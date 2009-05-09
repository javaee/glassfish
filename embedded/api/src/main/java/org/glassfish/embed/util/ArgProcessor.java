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
package org.glassfish.embed.util;

import java.util.*;

/**
 *
 * @author Byron Nevins
 */
public class ArgProcessor {
    public ArgProcessor (Arg[] myArgs, String[] cmdlineArgs) {
        this.allArgs = myArgs;
        this.cmdlineArgs = cmdlineArgs;
        process();
    }

    public Map<String,String> getParams() {
        return params;
    }
    
    public List<String> getOperands() {
        return operands;
    }
    
    private void process() {
        int numArgs = cmdlineArgs.length;
        
        for(int i = 0; i < numArgs; i++) {
            String arg = cmdlineArgs[i];
            
            if(!ok(arg))
                continue;
            
            if(isParam(arg))
                i += doParam(arg, i);
            else
                operands.add(arg);
        }
        processParams();
    }

    private int doParam(String s, int index) {
        for(Arg arg : allArgs) {
            if(arg.isThisYou(s)) {
                if(arg.requiresParameter()) {
                    if(index + 1 >= cmdlineArgs.length)
                        throw new IllegalArgumentException("no parameter for " + arg.longName);
                    arg.value = cmdlineArgs[index + 1];
                    return 1;
                }
                else {
                    int where = s.indexOf("=");
                    if(where >= 0)
                        arg.value = s.substring(where + 1);
                    else
                        arg.value = "true";
                }
            }
        }
        return 0;
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0;
    }

    private void processParams() {
        for(Arg arg : allArgs) {
            if(arg.value != null) {
                params.put(arg.longName, arg.value);
            }
            else if(arg.defaultValue != null) {
                arg.value = arg.defaultValue;
                params.put(arg.longName, arg.defaultValue);
            }
            else if(arg.isRequired())
                throw new IllegalArgumentException("Required parameter: " + arg.longName);
        }
    
    }

    private boolean isParam(String s) {
        return s.startsWith("-");
    }
    private Map<String,String> params = new HashMap<String,String>();
    private List<String> operands = new LinkedList<String>();
    private Arg[] allArgs;
    private String[] cmdlineArgs;
}
