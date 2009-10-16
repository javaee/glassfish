package com.sun.appserv.test.util.results;
/**
 @Class: TestSuite
 @Description: Class holding One TestSuite info.
 @Author : Ramesh Mandava
 @Last Modified : By Ramesh on 10/24/2001
 @Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of tests 		used a separate testIdVector
 @Last Modified : By Justin Lee on 10/05/2009
*/

import java.util.ArrayList;
import java.util.List;

public class TestSuite {
    private String id;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private List<Test> tests = new ArrayList<Test>();
    int pass;
    int fail;
    int didNotRun;
    int total;
    public int number;
    private boolean written;

    public TestSuite() {
    }

    public TestSuite(String id) {
        this.id = id == null ? ReporterConstants.NA : id.trim();
    }

    public TestSuite(String id, String name) {
        this(id);
        this.name = name == null ? ReporterConstants.NA : name.trim();
    }

    public TestSuite(String id, String name, String description) {
        this(id, name);
        this.description = description == null ? ReporterConstants.NA : description.trim();
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

    public List<Test> getTests() {
        return tests;
    }

    public void addTest(Test test) {
        tests.add(test);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TestSuite");
        sb.append("{id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", tests=").append(tests);
        sb.append('}');
        return sb.toString();
    }

    public String toXml() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<testsuite>\n");
        buffer.append("  <id> " + id.trim() + " </id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        buffer.append("<tests>\n");
        for (Test myTest : tests) {
            buffer.append(myTest.toXml());
        }
        buffer.append("</tests>\n");
        buffer.append("</testsuite>\n");

        return buffer.toString();
    }

    public boolean getWritten() {
        return written;
    }

    public void setWritten(final boolean written) {
        this.written = written;
    }
}
