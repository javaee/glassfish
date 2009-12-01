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
package org.glassfish.admin.rest.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.Dom;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Rajeshwar Patil
 * @author Ludovic Champenois ludo@dev.java.net

 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class GetResultHtmlProvider extends ProviderUtil implements MessageBodyWriter<GetResult> {

    @Context
    protected UriInfo uriInfo;

    @Override
    public long getSize(final GetResult proxy, final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType) {
        try {
            if (Class.forName("org.glassfish.admin.rest.provider.GetResult").equals(genericType)) {
                return mediaType.isCompatible(MediaType.TEXT_HTML_TYPE);
            }
        } catch (java.lang.ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    @Override
    public void writeTo(final GetResult proxy, final Class<?> type, final Type genericType,
            final Annotation[] annotations, final MediaType mediaType,
            final MultivaluedMap<String, Object> httpHeaders,
            final OutputStream entityStream) throws IOException, WebApplicationException {
        entityStream.write(getHtml(proxy).getBytes());
    }

    private String getHtml(GetResult proxy) {
        String result = getHtmlHeader();
        result = result + "<h1>" + getTypeKey(proxy.getDom()) + "</h1>";

        String attributes = getHtmlRespresentationForAttributes((ConfigBean)proxy.getDom(), uriInfo);
        result = getHtmlForComponent(attributes, "Attributes", result);

        String command = proxy.getDeleteCommand();
        String deleteCommand = getHtmlRespresentationsForCommand(
                proxy.getMetaData().getMethodMetaData("DELETE"), "DELETE", "Delete", uriInfo);
        result = getHtmlForComponent(deleteCommand, "Delete " + getTypeKey(proxy.getDom()), result);

        String childResourceLinks = getResourcesLinks(proxy.getDom(),
            proxy.getCommandResourcesPaths());
        result = getHtmlForComponent(childResourceLinks, "Child Resources", result);

        result = result + "</body></html>";
        return result;
    }

    private String getTypeKey(Dom proxy) {
        String uri = uriInfo.getAbsolutePath().toString();
        return upperCaseFirstLetter(eleminateHypen(getName(uri, '/')));
    }


    private String getResourcesLinks(Dom proxy, String[][] commandResourcesPaths) {
        String result = "";
        Set<String> elementNames = proxy.getElementNames();

        //expose ../applications/application resource to enable deployment
        //when no applications deployed on server
        if (elementNames.isEmpty()) {
            if("applications".equals(getName(uriInfo.getPath(), '/'))) {
                elementNames.add("application");
            }
        }

        for (String elementName : elementNames) { //for each element
            try {
                result = result + "<a href=\"" + getElementLink(uriInfo, elementName) + "\">";
                result = result + elementName;
                result = result + "</a>";
                result = result + "<br>";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //add command resources
        for (String[] commandResourcePath : commandResourcesPaths) {
            try {
                result = result + "<a href=\"" +
                    getElementLink(uriInfo, commandResourcePath[0]) + "\">";
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

    private String getStartHtmlElement(String name) {
        assert ((name != null) && name.length() > 0);
        String result = "<";
        result = result + name;
        result = result + ">";
        return result;
    }

    private String getEndHtmlElement(String name) {
        assert ((name != null) && name.length() > 0);
        String result = "<";
        result = result + "/";
        result = result + name;
        result = result + ">";
        return result;
    }
}
