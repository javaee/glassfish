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
/**
 * Return the result associated with the supplied Server Variable.
 * 
 * @author Bip Thelin
 * @author Paul Speed
 * @author Dan Sandberg
 * @author David Becker
 * @version $Revision: 1.4 $, $Date: 2007/05/05 05:32:19 $
 */
public class SSIEcho implements SSICommand {
    protected final static String DEFAULT_ENCODING = "entity";
    protected final static String MISSING_VARIABLE_VALUE = "(none)";

    /**
     * @see SSICommand
     */
    public long process(SSIMediator ssiMediator, String commandName,
            String[] paramNames, String[] paramValues, PrintWriter writer) {
        String encoding = DEFAULT_ENCODING;
        String originalValue = null;
        String errorMessage = null; // delay the call of HtmlEntityEncoder.encode
        for (int i = 0; i < paramNames.length; i++) {
            String paramName = paramNames[i];
            String paramValue = paramValues[i];
            if (paramName.equalsIgnoreCase("var")) {
                originalValue = paramValue;
            } else if (paramName.equalsIgnoreCase("encoding")) {
                if (isValidEncoding(paramValue)) {
                    encoding = paramValue;
                } else {
                    ssiMediator.log("#echo--Invalid encoding: " + paramValue);
                    if (errorMessage == null) {
                        errorMessage = getEncodedConfigErrorMessage(ssiMediator);
                    }
                    writer.write(errorMessage);
                }
            } else {
                ssiMediator.log("#echo--Invalid attribute: " + paramName);
                if (errorMessage == null) {
                    errorMessage = getEncodedConfigErrorMessage(ssiMediator);
                }
                writer.write(errorMessage);
            }
        }
        String variableValue = ssiMediator.getVariableValue(
                originalValue, encoding);
        if (variableValue == null) {
            variableValue = MISSING_VARIABLE_VALUE;
        }
        writer.write(variableValue);
        return System.currentTimeMillis();
    }


    protected boolean isValidEncoding(String encoding) {
        return encoding.equalsIgnoreCase("url")
                || encoding.equalsIgnoreCase("entity")
                || encoding.equalsIgnoreCase("none");
    }

    private String getEncodedConfigErrorMessage(SSIMediator ssiMediator) {
        String errorMessage = ssiMediator.getConfigErrMsg();
        return HtmlEntityEncoder.encodeXSS(errorMessage);
    }
}
