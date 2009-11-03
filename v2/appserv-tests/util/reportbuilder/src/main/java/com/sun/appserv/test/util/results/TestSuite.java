package com.sun.appserv.test.util.results;
/**
 @Class: TestSuite
 @Description: Class holding One TestSuite info.
 @Author : Ramesh Mandava
 @Last Modified : By Ramesh on 10/24/2001
 @Last Modified : By Ramesh on 1/20/2002 , For preserving order of entry of tests 		used a separate testIdVector
 @Last Modified : By Justin Lee on 10/05/2009
*/

import java.util.Map;
import java.util.TreeMap;

public class TestSuite {
    private String id;
    private String name = ReporterConstants.NA;
    private String description = ReporterConstants.NA;
    private Map<String, Test> tests = new TreeMap<String, Test>();
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

    public Map<String, Test> getTests() {
        return tests;
    }

    public void addTest(Test test) {
        if(tests.get(test.getName()) == null) {
            tests.put(test.getName(), test);
        } else {
            tests.get(test.getName()).merge(test);
        }
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
        buffer.append("<id>" + id.trim() + "</id>\n");
        if (!name.equals(ReporterConstants.NA)) {
            buffer.append("<name>" + name.trim() + "</name>\n");
        }
        if (!description.equals(ReporterConstants.NA)) {
            buffer.append("<description><![CDATA[" + description.trim() + "]]></description>\n");
        }
        buffer.append("<tests>\n");
        for (Test myTest : tests.values()) {
            buffer.append(myTest.toXml());
        }
        buffer.append("</tests>\n");
        buffer.append("</testsuite>\n");

        return buffer.toString();
    }

    public String toHtml() {
        StringBuilder table = new StringBuilder(
            "<div id=\"table" + number + "\" class=\"suiteDetail\"><table width=\"40%\">"
                + ReportHandler.row(null, "td", "Testsuite Name", getName())
                + ReportHandler.row(null, "td", "Testsuite Description", getDescription())
                + ReportHandler.row(null, "th", "Name", "Status"));
        for (Test test : getTests().values()) {
            for (TestCase testCase : test.getTestCases().values()) {
                final String status = testCase.getStatus();
                table.append(String.format("<tr><td>%s</td>%s", testCase.getName(),
                    ReportHandler.cell(status.replaceAll("_", ""), 1, status)));
            }
        }
        return table
            + "<tr class=\"nav\"><td colspan=\"2\">"
            + "[<a href=#DetailedResults>Detailed Results</a>"
            + "|<a href=#Summary>Summary</a>"
            + "|<a href=#TOP>Top</a>]"
            + "</td></tr>"
            + "</table></div><p>";

    }

    public boolean getWritten() {
        return written;
    }

    public void setWritten(final boolean written) {
        this.written = written;
    }

    public void merge(final TestSuite suite) {
        for (Test test : suite.getTests().values()) {
            addTest(test);
        }
    }
}
