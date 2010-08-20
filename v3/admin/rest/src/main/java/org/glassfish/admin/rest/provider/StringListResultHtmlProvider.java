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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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
@Produces(MediaType.TEXT_HTML)
public class StringListResultHtmlProvider extends BaseProvider<StringListResult> {

    public StringListResultHtmlProvider() {
        super(StringListResult.class, MediaType.TEXT_HTML_TYPE);
    }

    @Override
    public String getContent(StringListResult proxy) {
        String result = getHtmlHeader();
        String uri = uriInfo.getAbsolutePath().toString();
        String name = upperCaseFirstLetter(eleminateHypen(getName(uri, '/')));

        result = result + "<h1>" + name + "</h1>";
        if (proxy.isError()) {
            result = result + "<h2>Error:</h2>";
            result = result + proxy.getErrorMessage() + "<br>";
        } else {
            result = result + "<h2>" + proxy.getName() + "</h2>";
            for (String message : proxy.getMessages()) {
                result = result + message + "<br>";
            }
        }
        result = "<div>" + result + "</div>" + "<br>";

        String command = proxy.getPostCommand();
        if (command != null) {
            String postCommand = getHtmlRespresentationsForCommand(
                    proxy.getMetaData().getMethodMetaData("POST"), "POST", "Create", uriInfo);
            result = getHtmlForComponent(postCommand, "Create " + proxy.getName(), result);
        }

        command = proxy.getDeleteCommand();
        if (command != null) {
            String deleteCommand = getHtmlRespresentationsForCommand(
                    proxy.getMetaData().getMethodMetaData("DELETE"), "DELETE", "Delete", uriInfo);
            result = getHtmlForComponent(deleteCommand, "Delete " + proxy.getName(), result);
        }

        result = result + "</body></html>";
        return result;
    }
}
