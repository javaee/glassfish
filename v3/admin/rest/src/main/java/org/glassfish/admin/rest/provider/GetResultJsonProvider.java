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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.results.GetResult;
import org.jvnet.hk2.config.Dom;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.Map;

import static org.glassfish.admin.rest.Util.eleminateHypen;
import static org.glassfish.admin.rest.provider.ProviderUtil.*;

/**
 *
 * @author Rajeshwar Patil
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Jason Lee
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class GetResultJsonProvider extends BaseProvider<GetResult> {
    public GetResultJsonProvider() {
        super(GetResult.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public String getContent(GetResult proxy) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(KEY_ENTITY, getAttributes(proxy.getDom()));
            obj.put(KEY_METHODS, getJsonForMethodMetaData(proxy.getMetaData()));
            obj.put(KEY_CHILD_RESOURCES, getResourcesLinks(proxy));
            obj.put(KEY_COMMANDS, getCommandLinks(proxy.getCommandResourcesPaths()));

            int indent = getFormattingIndentLevel();
            if (indent > -1) {
                return obj.toString(indent);
            } else {
                return obj.toString();
            }
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
    }

    private JSONObject getAttributes(Dom proxy) throws JSONException {
        JSONObject entity = new JSONObject();
        for (String attributeName : proxy.model.getAttributeNames()) {
            entity.put(eleminateHypen(attributeName), proxy.attribute(attributeName));
        }

        return entity;
    }

    private JSONArray getResourcesLinks(GetResult getResult) throws JSONException {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, String> link : getResourceLinks(getResult.getDom()).entrySet()) {
            array.put(link.getValue());
        }
        return array;
    }

    private JSONArray getCommandLinks(String[][] commandResourcesPaths) throws JSONException {
        JSONArray array = new JSONArray();
        boolean debug = isDebug();

        //TODO commandResourcePath is two dimensional array. It seems the second e.x. see DomainResource#getCommandResourcesPaths().
        //The second dimension POST/GET etc. does not seem to be used. Discussed with Ludo. Need to be removed in a separate checkin.
        for (String[] commandResourcePath : commandResourcesPaths) {
            if (!commandResourcePath[2].startsWith("_") || (debug == false)) {//hidden cli command name
                array.put(getElementLink(uriInfo, commandResourcePath[0]));
            }
        }
        return array;
    }
}
