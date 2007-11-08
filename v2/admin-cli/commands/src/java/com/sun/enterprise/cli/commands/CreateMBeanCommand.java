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

package com.sun.enterprise.cli.commands;

import com.sun.enterprise.cli.framework.*;

import com.sun.enterprise.config.serverbeans.ServerTags;
import java.util.Map;
import java.util.HashMap;

public class CreateMBeanCommand extends GenericCommand 
{
    
    public final static String TARGET_OPTION = "target";
    public final static String NAME_OPTION = "name";
    public final static String OBJECT_NAME_OPTION = "objectname";
    public final static String ATTRIBUTES_OPTION = "attributes";
    public final static String ATTRIBUTE_DELIMITER = ":";
    public final static String ATTRIBUTE_VALUE_DELIMITER = "=";

    /*
     * Returns the Params from the properties
     * @return params returns params
     */
    protected Object[] getParamsInfo()throws CommandException, CommandValidationException
    {
        Object[] paramsInfo = new Object[3];
        paramsInfo[0] = getOption(TARGET_OPTION);
        Map mbeanParams = new HashMap();
        if (getOption(NAME_OPTION) != null)
            mbeanParams.put(ServerTags.NAME, getOption(NAME_OPTION));
        if (getOption(OBJECT_NAME_OPTION) != null)
            mbeanParams.put(ServerTags.OBJECT_NAME, getOption(OBJECT_NAME_OPTION));
        mbeanParams.put(ServerTags.IMPL_CLASS_NAME, getOperands().get(0));
        paramsInfo[1] = mbeanParams;
        paramsInfo[2] = getAttributesList(getOption(ATTRIBUTES_OPTION));
        return paramsInfo;
    }

    
    /**
     * Formulate and Returns Properties from the given string
     * @return Properties
     */
    private Map getAttributesList(String attributesStr)
        throws CommandException, CommandValidationException
    {
        Map attributes = new HashMap();
        if (attributesStr == null) return attributes;
        final CLITokenizer attrTok = new CLITokenizer(attributesStr, ATTRIBUTE_DELIMITER);
        while (attrTok.hasMoreTokens()) {
            final String nameAndvalue = attrTok.nextToken();
            final CLITokenizer nameTok = new CLITokenizer(nameAndvalue, ATTRIBUTE_VALUE_DELIMITER);
            if (nameTok.countTokens() == 2)
            {
                attributes.put(nameTok.nextTokenWithoutEscapeAndQuoteChars(),
                               nameTok.nextTokenWithoutEscapeAndQuoteChars());
            }
            else
            {
                throw new CommandValidationException(getLocalizedString("InvalidAttributeSyntax"));
            }
        }
        CLILogger.getInstance().printDebugMessage("Got the Attributes List : " + attributes.toString());
        return attributes;
    }
}
