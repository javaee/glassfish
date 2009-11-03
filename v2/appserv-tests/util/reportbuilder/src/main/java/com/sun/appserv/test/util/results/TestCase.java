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
    private String expected;
    private String actual;

    public TestCase() {
    }

    public TestCase(String id) {
        this.id = id == null ? ReporterConstants.NA : id.trim();
    }

    public TestCase(String id, String name) {
        this(id);
        this.name = name == null ? ReporterConstants.NA : name.trim();
    }

    public TestCase(String id, String name, String description) {
        this(id, name);
        this.description = description.trim();
    }

    public void setStatus(String status) {
        this.status = status == null ? ReporterConstants.NA : status.trim();
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription == null ? ReporterConstants.NA : statusDescription.trim();
        expected = null;
        actual = null;
    }

    public void setExpected(String expected) {
        this.expected = expected == null ? ReporterConstants.NA : expected.trim();
    }

    public void setActual(String actual) {
        this.actual = actual == null ? ReporterConstants.NA : actual.trim();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String value) {
        name = value;
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

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
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
        } else if ((expected != null) && (actual != null)) {
            buffer.append("<status value=\"" + status.trim() + "\"> <expected><![CDATA[" + expected.trim()
                + "]]></expected><actual><![CDATA[" + actual.trim() + "]]></actual></status>\n");
        } else {
            buffer.append("<status value=\"" + status.trim() + "\">" + "</status>\n");
        }
        buffer.append("</testcase>\n");

        return buffer.toString();
    }
}
