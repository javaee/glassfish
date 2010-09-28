/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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


package org.glassfish.admingui.common.handlers;

import java.util.*;

import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import org.glassfish.admingui.common.handlers.RestApiHandlers;
import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author Siraj
 */
public class ScheduleHandlers {

    @Handler(id = "gf.getScheduleData",
        input = {
            @HandlerInput(name = "scheduleName", type = String.class)},
        output = {
            @HandlerOutput(name = "dayOfWeek", type = java.util.Map.class),
            @HandlerOutput(name = "dayOfMonth", type = java.util.Map.class),
            @HandlerOutput(name = "month", type = java.util.Map.class)})

    public static void getScheduleData(HandlerContext handlerCtx) {
        String scheduleName = (String) handlerCtx.getInputValue("scheduleName");
        String dayOfWeek = "*", dayOfMonth = "*", month = "*";

        if (scheduleName != null) {
            String endPoint = GuiUtil.getSessionValue("REST_URL") + "/configs/config/server-config/schedules/schedule/" +
                    scheduleName;

            Map attribs = RestApiHandlers.getAttributesMap(endPoint);

            dayOfWeek = (String)attribs.get("dayOfWeek");
            dayOfMonth = (String)attribs.get("dayOfMonth");
            month = (String)attribs.get("month");
        }
        Map dayOfWeekMap = getDataMap(dayOfWeek);
        Map dayOfMonthMap = getDataMap(dayOfMonth);
        Map monthMap = getDataMap(month);

        handlerCtx.setOutputValue("dayOfWeek", dayOfWeekMap);
        handlerCtx.setOutputValue("dayOfMonth", dayOfMonthMap);
        handlerCtx.setOutputValue("month", monthMap);

    }

    private static Map getDataMap(String data) {
        List<String> dataList = GuiUtil.parseStringList(data, ",");
        Map dataMap = new HashMap();
        for (String dataItem : dataList) {
            dataMap.put(dataItem, "true");
        }
        return dataMap;
    }
    @Handler(id = "gf.convertBooleanMapToString",
        input = {
            @HandlerInput(name = "map", type = java.util.Map.class, required=true),
            @HandlerInput(name = "delimiter", type = String.class)
        },
        output = {
            @HandlerOutput(name = "str", type = String.class)
        })

    public static void convertBooleanMapToString(HandlerContext handlerCtx) {
        Map<String, String> map = (Map) handlerCtx.getInputValue("map");
        String delimiter = (String)handlerCtx.getInputValue("delimiter");
        if (delimiter == null)
            delimiter =",";
        String str = "";

        for (String key : map.keySet()) {
            String val = map.get(key);
            if (val != null && val.equals("true")) {
                if (key.equals("*")) {
                    str = "*";
                    break;
                }
                if (str.length() > 0)
                    str = str + ",";
                str = str + key;
            }
        }
        if (str.length() == 0)
            str = "*";
        handlerCtx.setOutputValue("str", str);
    }
}
