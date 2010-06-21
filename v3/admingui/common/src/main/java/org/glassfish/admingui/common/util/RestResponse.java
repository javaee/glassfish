/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.common.util;

import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author jasonlee
 */
public abstract class RestResponse {
    public abstract int getResponseCode();
    public abstract String getResponseBody();

    public static RestResponse getRestResponse(ClientResponse response) {
        return new JerseyRestResponse(response);
    }

    public boolean isSuccess() {
        int status = getResponseCode();
        return (status >= 200) && (status <= 299);
    }

    public List<String> getMessageParts() {
        Document document = MiscUtil.getDocument(getResponseBody());
        List<String> parts = null;

        Element root = document.getDocumentElement();
        NodeList nl = root.getElementsByTagName("message-part");
        if (nl.getLength() > 0) {
            parts = new ArrayList<String>();
            Node child;
            for (int i = 0; i < nl.getLength(); i++) {
                child = nl.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    parts.add(((Element) child).getAttribute("message"));
                }
            }
        }


        return parts;
    }
}


class JerseyRestResponse extends RestResponse {
    protected ClientResponse response;

    public JerseyRestResponse(ClientResponse response) {
        this.response = response;
    }

    @Override
    public String getResponseBody() {
        return response.getEntity(String.class);
    }

    @Override
    public int getResponseCode() {
        return response.getStatus();
    }

}