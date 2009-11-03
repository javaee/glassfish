package com.sun.appserv.test.util.results;
/**
 @Class: Test
 @Description: Class holding One Test info.
 @Author : Ramesh Mandava
 @Last Modified :Initial creation By Ramesh on 10/24/2001
 @Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of
 testcases used a separate testCaseIdVector
 @Last Modified : By Justin Lee on 10/05/2009
 */

import java.util.LinkedHashMap;
import java.util.Map;

public class Test {
    private String id;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private String status = ReporterConstants.OPTIONAL;
    private String statusDescription = ReporterConstants.OPTIONAL;
    private String expected;
    private String actual;
    private Map<String, TestCase> testCases = new LinkedHashMap<String, TestCase>();

    public Test() {
    }

    public Test(String id) {
        this.id = id.trim();
    }

    public Test(String id, String name) {
        this(id);
        this.name = name == null ? ReporterConstants.NA : name.trim();
    }

    public Test(String id, String name, String description) {
        this(id, name);
        this.description = description == null ? ReporterConstants.NA : description.trim();
    }

    public void setStatus(String status) {
        this.status = status == null ? ReporterConstants.NA : status.trim();
    }

    public void setStatusDescription(String desc) {
        statusDescription = desc == null ? ReporterConstants.NA : desc.trim();
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

    public Map<String, TestCase> getTestCases() {
        return testCases;
    }

    public void addTestCase(TestCase testCase) {
        if(testCases.get(testCase.getName()) == null) {
            testCases.put(testCase.getName(), testCase);
        } else {
            testCase.setName(testCase.getName() + " -- DUPLICATE");
            testCases.put(testCase.getName() + " -- DUPLICATE", testCase);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Test");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", actual='").append(actual).append('\'');
        sb.append(", expected='").append(expected).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", statusDescription='").append(statusDescription).append('\'');
        sb.append(", testCases='").append(testCases).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String toXml() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<test>\n");
        buffer.append("<id>" + id + "</id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        if (!status.equals(ReporterConstants.OPTIONAL)) {
            buffer.append("<status value=\"" + status + "\">");
            if (!description.equals(ReporterConstants.OPTIONAL)) {
                buffer.append("<![CDATA[" + description.trim() + "]]>");
            } else if ((expected != null) && (actual != null)) {
                buffer.append("<expected><![CDATA[" + expected.trim() + "]]></expected>"
                    + "<actual><![CDATA[" + actual.trim() + "]]></actual>");
            }
            buffer.append("</status>\n");
        }
        if (!testCases.isEmpty()) {
            buffer.append("<testcases>\n");
            for (TestCase myTestCase : testCases.values()) {
                buffer.append(myTestCase.toXml());
            }
            buffer.append("</testcases>\n");
        }
        buffer.append("</test>\n");
        return buffer.toString();
    }

    public void merge(final Test test) {
        for (TestCase testCase : test.getTestCases().values()) {
            addTestCase(testCase);
        }
    }
}
