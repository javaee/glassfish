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


import org.apache.catalina.util.Strftime;
import org.glassfish.web.util.HtmlEntityEncoder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;
/**
 * Implements the Server-side #flastmod command
 * 
 * @author Bip Thelin
 * @author Paul Speed
 * @author Dan Sandberg
 * @author David Becker
 * @version $Revision: 1.5 $, $Date: 2007/05/05 05:32:20 $
 */
public final class SSIFlastmod implements SSICommand {
    /**
     * @see SSICommand
     */
    public long process(SSIMediator ssiMediator, String commandName,
            String[] paramNames, String[] paramValues, PrintWriter writer) {
    	long lastModified = 0;
        String configErrMsg = null;
        for (int i = 0; i < paramNames.length; i++) {
            String paramName = paramNames[i];
            String paramValue = paramValues[i];
            String substitutedValue = ssiMediator
                    .substituteVariables(paramValue);
            try {
                if (paramName.equalsIgnoreCase("file")
                        || paramName.equalsIgnoreCase("virtual")) {
                    boolean virtual = paramName.equalsIgnoreCase("virtual");
                    lastModified = ssiMediator.getFileLastModified(
                            substitutedValue, virtual);
                    Date date = new Date(lastModified);
                    String configTimeFmt = ssiMediator.getConfigTimeFmt();
                    writer.write(formatDate(date, configTimeFmt));
                } else {
                    ssiMediator.log("#flastmod--Invalid attribute: "
                            + paramName);
                    if (configErrMsg == null) {
                        configErrMsg = getEncodedConfigErrorMessage(ssiMediator);
                    }
                    writer.write(configErrMsg);
                }
            } catch (IOException e) {
                ssiMediator.log(
                        "#flastmod--Couldn't get last modified for file: "
                                + substitutedValue, e);
                if (configErrMsg == null) {
                    configErrMsg = getEncodedConfigErrorMessage(ssiMediator);
                }
                writer.write(configErrMsg);
            }
        }
        return lastModified;
    }


    protected String formatDate(Date date, String configTimeFmt) {
        Strftime strftime = new Strftime(configTimeFmt, Locale.US);
        return strftime.format(date);
    }

    private String getEncodedConfigErrorMessage(SSIMediator ssiMediator) {
        String configErrMsg = ssiMediator.getConfigErrMsg();
        return HtmlEntityEncoder.encodeXSS(configErrMsg);
    }
}
