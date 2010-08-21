/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.admin.rest.provider;

import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.results.StringListResult;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import static org.glassfish.admin.rest.Util.*;
import static org.glassfish.admin.rest.provider.ProviderUtil.*;

/**
 * @author Rajeshwar Patil
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class StringListResultXmlProvider extends BaseProvider<StringListResult> {
    public StringListResultXmlProvider() {
        super(StringListResult.class, MediaType.APPLICATION_XML_TYPE);
    }

    @Override
    public String getContent(StringListResult proxy) {
        String result;
        String uri = uriInfo.getAbsolutePath().toString();
        String name = upperCaseFirstLetter(eleminateHypen(getName(uri, '/')));
        String indent = Constants.INDENT;
        result ="<" ;

        // TODO:  We need to rethink this strucure, as it's slightly different
        // than the JSON version.
        result = result + KEY_ENTITY;
        String attribute;
        if (proxy.isError()) {
            attribute = getAttribute("error", proxy.getErrorMessage());
            if ((attribute != null) && (attribute.length() > 1)) {
                result = result + " ";
                result = result + attribute;
            }
            result = result + ">";
        } else {
            result = result + ">";
            for (String message: proxy.getMessages()) {
                result = result + "\n" + indent;
                result = result + getStartXmlElement(proxy.getName());
                result = result + message;
                result = result + getEndXmlElement(proxy.getName());
            }
        }

        String methodDataXml = getXmlForMethodMetaData(proxy.getMetaData(), indent + Constants.INDENT);
        if (!methodDataXml.isEmpty()) {
            result = result + "\n\n" + indent;
            result = result + "<" + KEY_METHODS + ">";
            result = result + methodDataXml;
            result = result + "\n" + indent + "</" + KEY_METHODS + ">";
        }

        result = result + "\n\n" + "</" + KEY_ENTITY + ">";
        return result;
    }


//    private String getTypeKey(String name) {
//       return upperCaseFirstLetter(eleminateHypen(name));
//    }


    private String getAttribute(String name, String value) {
        String result ="";
        result = result + name + "=" + quote(value);
        return result;
    }
}
