/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2001-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.test.util.results;

/**
 * @Class: TestCase
 * @Description: Class holding One TestCase info.
 * @Author : Ramesh Mandava
 * @Last Modified :Initial creation By Ramesh on 10/24/2001
 * @Last Modified : By Justin Lee on 10/05/2009
 */
public class TestCase {
    private String id;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private String status = ReporterConstants.DID_NOT_RUN;
    private String statusDescription = ReporterConstants.NA;

    public TestCase() {
    }

    public TestCase(String name) {
        this();
        this.name = SimpleReporterAdapter.checkNA(name);
        id = name;
    }

    public TestCase(String name, String description) {
        this(name);
        this.description = SimpleReporterAdapter.checkNA(description);
    }

    public void setStatus(String status) {
        this.status = SimpleReporterAdapter.checkNA(status);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        name = SimpleReporterAdapter.checkNA(value);
        id = name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    @Override
    public String toString() {
        return "TestCase{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", status='" + status + '\'' +
            '}';
    }
    
    public String toXml() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<testcase>\n");
        buffer.append("<id>" + id + "</id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        if (!statusDescription.equals(ReporterConstants.NA)) {
            buffer.append("<status value=\"" + status.trim() + "\"><![CDATA[" + statusDescription.trim() + "]]></status>\n");
        } else {
            buffer.append("<status value=\"" + status.trim() + "\">" + "</status>\n");
        }
        buffer.append("</testcase>\n");

        return buffer.toString();
    }
}
