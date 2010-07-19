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
package org.glassfish.admin.rest.logviewer;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.management.AttributeList;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.sun.enterprise.server.logging.logviewer.backend.LogFilter;
import java.io.Serializable;
import javax.management.Attribute;

/**
 * REST resource to get Log records
 * simple wrapper around internal  LogFilter query class
 *
 * @author ludovic Champenois
 */
public class StructuredLogViewerResource {

    @GET
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public String getJson(
            @QueryParam("logFileName") @DefaultValue("${com.sun.aas.instanceRoot}/logs/server.log") String logFileName,
            @QueryParam("startIndex") @DefaultValue("-1") long startIndex,
            @QueryParam("searchForward") @DefaultValue("false") boolean searchForward,
            @QueryParam("maximumNumberOfResults") @DefaultValue("40") int maximumNumberOfResults,
            @QueryParam("onlyLevel") @DefaultValue("true") boolean onlyLevel,
            @QueryParam("fromTime") @DefaultValue("-1") long fromTime,
            @QueryParam("toTime") @DefaultValue("-1") long toTime,
            @QueryParam("logLevel") @DefaultValue("INFO") String logLevel) throws IOException {

        return getWithType(
                logFileName,
                startIndex,
                searchForward,
                maximumNumberOfResults,
                fromTime,
                toTime,
                logLevel, onlyLevel, "json");

    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    public String getXML(
            @QueryParam("logFileName") @DefaultValue("${com.sun.aas.instanceRoot}/logs/server.log") String logFileName,
            @QueryParam("startIndex") @DefaultValue("-1") long startIndex,
            @QueryParam("searchForward") @DefaultValue("false") boolean searchForward,
            @QueryParam("maximumNumberOfResults") @DefaultValue("40") int maximumNumberOfResults,
            @QueryParam("onlyLevel") @DefaultValue("true") boolean onlyLevel,
            @QueryParam("fromTime") @DefaultValue("-1") long fromTime,
            @QueryParam("toTime") @DefaultValue("-1") long toTime,
            @QueryParam("logLevel") @DefaultValue("INFO") String logLevel) throws IOException {

        return getWithType(
                logFileName,
                startIndex,
                searchForward,
                maximumNumberOfResults,
                fromTime,
                toTime,
                logLevel,onlyLevel, "xml");

    }

    private String getWithType(
            String logFileName,
            long startIndex,
            boolean searchForward,
            int maximumNumberOfResults,
            long fromTime,
            long toTime,
            String logLevel, boolean onlyLevel, String type) throws IOException {


        Properties props = new Properties();
        final List<String> moduleList = null;

        boolean sortAscending = true;
        if (!searchForward) {
            sortAscending = false;
        }

        String anySearch = null;

        final AttributeList result = LogFilter.getLogRecordsUsingQuery(logFileName,
                startIndex,
                searchForward, sortAscending,
                maximumNumberOfResults,
                fromTime == -1 ? null : new Date(fromTime),
                toTime == -1 ? null : new Date(toTime),
                logLevel, onlyLevel, moduleList, props,anySearch);
        return convertQueryResult(result, type);

    }

    private <T> List<T> asList(final Object list) {
        return (List<T>) List.class.cast(list);
    }

    private String quoted(String s) {
        return "\"" + s + "\"";
    }

    private String convertQueryResult(final AttributeList queryResult, String type) {
        // extract field descriptions into a String[]
        StringBuilder sb = new StringBuilder();
        if (type.equals("json")) {
            sb.append("{\n").append(quoted("records")).append(": [");
        } else {
            sb.append("<records>\n");
        }

        final AttributeList fieldAttrs = (AttributeList) ((Attribute) queryResult.get(0)).getValue();
        String[] fieldHeaders = new String[fieldAttrs.size()];
        for (int i = 0; i < fieldHeaders.length; ++i) {
            final Attribute attr = (Attribute) fieldAttrs.get(i);
            fieldHeaders[i] = (String) attr.getValue();
        }

        List<List<Serializable>> srcRecords = asList(((Attribute) queryResult.get(1)).getValue());

        // extract every record
        for (int recordIdx = 0; recordIdx < srcRecords.size(); ++recordIdx) {
            List<Serializable> record = srcRecords.get(recordIdx);

            assert (record.size() == fieldHeaders.length);
            //Serializable[] fieldValues = new Serializable[fieldHeaders.length];

            LogRecord rec = new LogRecord();
            int fieldIdx = 0;
            rec.setRecordNumber(((Long) record.get(fieldIdx++)).longValue());
            rec.setLoggedDateTime((Date) record.get(fieldIdx++));
            rec.setLoggedLevel((String) record.get(fieldIdx++));
            rec.setProductName((String) record.get(fieldIdx++));
            rec.setLoggerName((String) record.get(fieldIdx++));
            rec.setNameValuePairs((String) record.get(fieldIdx++));
            rec.setMessageID((String) record.get(fieldIdx++));
            rec.setMessage((String) record.get(fieldIdx++));
            if (type.equals("json")) {
                sb.append(rec.toJSON());
                sb.append(",\n");
            } else {
                sb.append(rec.toXML());

            }

        }
        if (type.equals("json")) {
            sb.append("]\n");
            sb.append("}\n");
        } else {
            sb.append("\n</records>\n");

        }

        return sb.toString();
    }
}
