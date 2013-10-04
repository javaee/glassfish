/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.admin.rest.model;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.RestModel;
import org.glassfish.admin.rest.utils.JsonUtil;

public class ResponseBody {
    public static final String EVENT_NAME="response/body";
    private List<Message> messages = new ArrayList<Message>();
    private RestModel entity;

    public List<Message> getMessages() {
        return this.messages;
    }

    public void setMessages(List<Message> val) {
        this.messages = val;
    }

    public RestModel getEntity() {
        return entity;
    }

    public ResponseBody setEntity(RestModel entity) {
        this.entity = entity;
        return this;
    }

    public ResponseBody addSuccess(String message) {
        return add(Message.Severity.SUCCESS, message);
    }

    public ResponseBody addWarning(String message) {
        return add(Message.Severity.WARNING, message);
    }

    public ResponseBody addFailure(String message) {
        return add(Message.Severity.FAILURE, message);
    }

    public ResponseBody add(Message.Severity severity, String message) {
        return add(new Message(severity, message));
    }

    public ResponseBody add(Message message) {
        getMessages().add(message);
        return this;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        if (!messages.isEmpty()) {
            JSONArray array = new JSONArray();
            for (Message message : messages) {
                JSONObject o = new JSONObject();
                o.put("message", message.getMessage());
                o.put("severity", message.getSeverity().toString());
                array.put(o);
            }
            object.put("messages", array);
        }
        if (entity != null) {
            object.put("item", JsonUtil.getJsonObject(getEntity()));
        }
        return object;
    }
}
