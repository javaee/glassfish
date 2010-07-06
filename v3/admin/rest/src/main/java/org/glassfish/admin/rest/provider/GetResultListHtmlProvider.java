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

import org.glassfish.admin.rest.results.GetResultList;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.jvnet.hk2.config.Dom;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import static org.glassfish.admin.rest.Util.*;
import static org.glassfish.admin.rest.provider.ProviderUtil.*;

/**
 *
 * @author Rajeshwar Patil
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class GetResultListHtmlProvider extends BaseProvider<GetResultList> {

    public GetResultListHtmlProvider() {
        super(GetResultList.class, MediaType.TEXT_HTML_TYPE);
    }

    @Override
    protected String getContent(GetResultList proxy) {
        String result = getHtmlHeader();
         final String typeKey = upperCaseFirstLetter((decode(getName(uriInfo.getPath(), '/'))));
         result = result + "<h1>" + typeKey + "</h1>";

      //  String command = proxy.getPostCommand();
        String postCommand = getHtmlRespresentationsForCommand(
            proxy.getMetaData().getMethodMetaData("POST"), "POST", "Create", uriInfo);
        result = getHtmlForComponent(postCommand, "Create " + typeKey, result);

        String childResourceLinks = getResourcesLinks(proxy.getDomList(),
            proxy.getCommandResourcesPaths());
        result = getHtmlForComponent(childResourceLinks, "Child Resources", result);

        result = result + "</html></body>";
        return result;
    }


//    private String getTypeKey() {
//       return upperCaseFirstLetter(eleminateHypen(getName(uriInfo.getPath(), '/')));
//    }


    private String getResourcesLinks(List<Dom> proxyList,
        String[][] commandResourcesPaths) {
        String result = "";
        for (Dom proxy: proxyList) { //for each element
            try {
                    result = result + "<a href=\"" + getElementLink(uriInfo, proxy.getKey()) + "\">";
                    result = result + proxy.getKey();
                    result = result + "</a>";
                    result = result +  "<br>";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //add command resources
        for (String[] commandResourcePath : commandResourcesPaths) {
            try {
                result = result + "<a href=\"" + getElementLink(uriInfo, commandResourcePath[0]) + "\">";
                result = result + commandResourcePath[0];
                result = result + "</a>";
                result = result + "<br>";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!result.equals("")) {
            result = "<div>" + result + "</div>" + "<br>";
        }
        return result;
    }
}
