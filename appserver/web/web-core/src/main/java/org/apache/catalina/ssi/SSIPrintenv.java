/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.ssi;


import org.glassfish.web.util.HtmlEntityEncoder;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
/**
 * Implements the Server-side #printenv command
 * 
 * @author Dan Sandberg
 * @author David Becker
 * @version $Revision: 1.3 $, $Date: 2007/02/13 19:16:21 $
 */
public class SSIPrintenv implements SSICommand {
    /**
     * @see SSICommand
     */
    public long process(SSIMediator ssiMediator, String commandName,
            String[] paramNames, String[] paramValues, PrintWriter writer) {
    	long lastModified = 0;
        //any arguments should produce an error
        if (paramNames.length > 0) {
            String errorMessage = ssiMediator.getConfigErrMsg();
            writer.write(HtmlEntityEncoder.encodeXSS(errorMessage));
        } else {
            Collection<String> variableNames = ssiMediator.getVariableNames();
            Iterator<String> iter = variableNames.iterator();
            while (iter.hasNext()) {
                String variableName = iter.next();
                String variableValue = ssiMediator
                        .getVariableValue(variableName);
                //This shouldn't happen, since all the variable names must
                // have values
                if (variableValue == null) {
                    variableValue = "(none)";
                }
                writer.write(variableName);
                writer.write('=');
                writer.write(variableValue);
                writer.write('\n');
                lastModified = System.currentTimeMillis();
            }
        }
        return lastModified;
    }
}
