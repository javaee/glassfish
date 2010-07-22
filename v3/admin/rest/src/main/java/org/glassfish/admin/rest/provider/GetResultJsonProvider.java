/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.provider;

import org.glassfish.admin.rest.results.GetResult;
import java.util.Set;

import org.jvnet.hk2.config.Dom;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Produces;

import org.glassfish.admin.rest.Constants;
import static org.glassfish.admin.rest.Util.*;
import static org.glassfish.admin.rest.provider.ProviderUtil.*;

/**
 *
 * @author Rajeshwar Patil
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class GetResultJsonProvider extends BaseProvider<GetResult> {

    public GetResultJsonProvider() {
        super(GetResult.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    protected String getContent(GetResult proxy) {
        String result;
        String indent = Constants.INDENT;
        result ="{" ;
        result = result + "\n\n" + indent;

        result = result + quote(KEY_ENTITY) + ":{";
        result = result + getAttributes(proxy.getDom());
        result = result + "},";

        result = result + "\n\n" + indent;
        result = result + quote(KEY_METHODS) + ":{";
        result = result + getJsonForMethodMetaData(proxy.getMetaData(),
            indent + Constants.INDENT);
        result = result + "\n" + indent + "}";

        //do not display empty child resources array
        if ((proxy.getDom().getElementNames().size() > 0) || ("applications".equals(getName(uriInfo.getPath(), '/')))) {
            result = result + ",";
            result = result + "\n\n" + indent;
            result = result + quote(KEY_CHILD_RESOURCES) + ":[";
            result = result + getResourcesLinks(proxy.getDom(), indent + Constants.INDENT);
            result = result + "]";
        }

        if (proxy.getCommandResourcesPaths().length > 0)  {
            result = result + ",";
            result = result + "\n\n" + indent;
            result = result + quote(KEY_COMMANDS) + ":[";
            result = result + getCommandLinks(proxy.getCommandResourcesPaths(), indent + Constants.INDENT);
            result = result + "]";
        }

        result = result + "\n\n" + "}";
        return result;
    }

    private String getAttributes(Dom proxy) {
        StringBuilder result = new StringBuilder();
        Set<String> attributeNames = proxy.model.getAttributeNames();
        String sep = "";
        for (String attributeName : attributeNames) {
            result.append(sep)
                    .append(quote(eleminateHypen(attributeName)))
                    .append(":")
                    .append(quote(proxy.attribute(attributeName)));
        }

        return result.toString();
    }


    private String getResourcesLinks(Dom proxy, String indent) {
        StringBuilder result = new StringBuilder();
        Set<String> elementNames = proxy.getElementNames();
        String sep = "";

        //expose ../applications/application resource to enable deployment
        //when no applications deployed on server
        if (elementNames.isEmpty()) {
            if("applications".equals(getName(uriInfo.getPath(), '/'))) {
                elementNames.add("application");
            }
        }

        for (String elementName : elementNames) {
            try {
                result.append(sep)
                        .append("\n")
                        .append(indent)
                        .append(quote(getElementLink(uriInfo, elementName/*element*/)));
                sep = ",";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result.toString();
    }

    private String getCommandLinks(String[][] commandResourcesPaths, String indent) {
        StringBuilder result = new StringBuilder();
        String sep = "";

        //TODO commandResourcePath is two dimensional array. It seems the second e.x. see DomainResource#getCommandResourcesPaths().
        //The second dimension POST/GET etc. does not seem to be used. Discussed with Ludo. Need to be removed in a separate checkin.
        for (String[] commandResourcePath : commandResourcesPaths) {
            try {
                result.append(sep)
                        .append("\n")
                        .append(indent)
                        .append(quote(getElementLink(uriInfo, commandResourcePath[0])));
                sep = ",";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result.toString();
    }
}