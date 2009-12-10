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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import javax.ws.rs.core.Context;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.jvnet.hk2.config.Dom;

/**
 * Entity provider for encoding HK2 Dom beans.
 *
 * @author Ludovic Champenois ludo@dev.java.net
 */
@Produces("application/json")
@Provider
public class JsonProvider implements MessageBodyWriter<Dom> {
    @Context
    protected UriInfo uriInfo;
    @Override
    public void writeTo(Dom p,
            Class<?> type, Type genericType, Annotation annotations[],
            MediaType mediaType, MultivaluedMap<String, Object> headers,
            OutputStream out) throws IOException {

        out.write(("{\n " + p.typeName() + ":\n").getBytes());
        introspect(out, p);
        out.write( ",\n".getBytes());
        out.write("\n'resources' :\n".getBytes());
        introspectElements(out, p) ;
        out.write("\n".getBytes());

        out.write("}\n".getBytes());

    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {

        System.out.println("isWriteable called" + type);
        System.out.println("isWriteable called" + genericType);
        System.out.println("ret=" + Dom.class.isAssignableFrom(type));
        return Dom.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Dom p, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return -1;
    }

    private void introspect(OutputStream out, Dom proxy) throws IOException {
        Set<String> ss = proxy.model.getAttributeNames();

        boolean first = true;
        out.write("{".getBytes());
        for (String a : ss) {
            out.write("'".getBytes());
            out.write(a.getBytes());
            out.write("' : '".getBytes());

            out.write(proxy.attribute(a).getBytes());
            out.write("'".getBytes());
            if (first == false) {
                out.write(",".getBytes());
            }
            first = false;

        }
        out.write("}".getBytes());

        /*     Set<String> elem = proxy.getElementNames();
        //System.out.println(id+"set size is" + elem.size());

        for (String bb : elem) {
        System.out.println("<" + bb + ">");
        org.jvnet.hk2.config.ConfigModel.Property prop = proxy.model.getElement(bb);
        if (prop != null && proxy.model.getElement(bb).isLeaf()) {
        System.out.println("-1-1-1- " + proxy.leafElement(bb));
        } else {
        introspect(out, proxy.element(bb));
        }

        System.out.println("</" + bb + ">");
        System.out.println("    ");
        ///  }


        }*/
    }

    private String introspectElements(OutputStream out, Dom proxy) throws IOException{
        Set<String> elem = proxy.getElementNames();
        //System.out.println(id+"set size is" + elem.size());

        String ret = "";
        //    System.out.println( "--------" + proxy.model.key);
        boolean first = true;
        out.write("{\n".getBytes());
        for (String a : elem) {
            if (first == false) {
                out.write(",\n".getBytes());
            }
            out.write("'".getBytes());
            out.write(a.getBytes());
            out.write("' : '".getBytes());
            out.write((uriInfo.getAbsolutePath() + a).getBytes());


            out.write("'".getBytes());


            first = false;


        }
        ret = ret + "}";
        return ret;


//        for (String bb : elem) {
//            System.out.println("<" + bb + ">");
//            org.jvnet.hk2.config.ConfigModel.Property prop = proxy.model.getElement(bb);
//            if (prop != null && proxy.model.getElement(bb).isLeaf()) {
//                System.out.println("-1-1-1- " + proxy.leafElement(bb));
//            } else {
//                introspect(out, proxy.element(bb));
//            }
//
//            System.out.println("</" + bb + ">");
//            System.out.println("    ");
//            ///  }



    }
}
