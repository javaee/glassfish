/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.List;
import java.util.Set;

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
public class GetResultListHtmlProvider extends ProviderUtil implements MessageBodyWriter<GetResultList> {

     @Context
     protected UriInfo uriInfo;

     public long getSize(final GetResultList proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
          return -1;
     }


     public boolean isWriteable(final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType) {
         try {
             if (Class.forName("org.glassfish.admin.rest.provider.GetResultList").equals(genericType)) {
                 return mediaType.isCompatible(MediaType.TEXT_HTML_TYPE);
             }
         } catch (java.lang.ClassNotFoundException e) {
             return false;
         }
         return false; 
     }


     public void writeTo(final GetResultList proxy, final Class<?> type, final Type genericType,
               final Annotation[] annotations, final MediaType mediaType,
               final MultivaluedMap<String, Object> httpHeaders,
               final OutputStream entityStream) throws IOException, WebApplicationException {
         entityStream.write(getHtml(proxy).getBytes());
     }


     private String getHtml(GetResultList proxy) {
        String result = getHtmlHeader();
        result = result + "<h1>" + getTypeKey() + "</h1>";

        String command = proxy.getPostCommand();
        String postCommand = getHtmlRespresentationsForCommand(command, "post", "Create", uriInfo);
        result = getHtmlForComponent(postCommand, "Create " + getTypeKey(), result);

        String childResourceLinks = getResourcesLinks(proxy.getDomList(),
            proxy.getCommandResourcesPaths());
        result = getHtmlForComponent(childResourceLinks, "Child Resources", result);

        result = result + "</html></body>";
        return result;
    }


    private String getTypeKey() {
       return upperCaseFirstLetter(eleminateHypen(getName(uriInfo.getPath(), '/')));
    }


    private String getResourcesLinks(List<Dom> proxyList,
        String[][] commandResourcesPaths) {
        String result = "";
        for (Dom proxy: proxyList) { //for each element
            try {
                    result = result + "<a href=" + getElementLink(uriInfo, proxy.getKey()) + ">";
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
                result = result + "<a href=" + getElementLink(uriInfo, commandResourcePath[0]) + ">";
                result = result + commandResourcePath[0];
                result = result + "</a>";
                result = result + "<br>";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (result != "") {
            result = "<div>" + result + "</div>" + "<br>";
        }
        return result;
    }
}
