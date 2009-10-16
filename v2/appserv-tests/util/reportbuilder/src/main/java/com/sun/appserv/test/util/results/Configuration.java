package com.sun.appserv.test.util.results;

public class Configuration {
    private String os;
    private String jdkVersion;
    private String machineName;

    public Configuration() {
    }

    public Configuration(final String os, final String jdkVersion, final String machineName) {
        this.os = os;
        this.jdkVersion = jdkVersion;
        this.machineName = machineName;
    }

    @Override
    public String toString() {
        return "Configuration{" +
            "jdkVersion='" + jdkVersion + '\'' +
            ", os='" + os + '\'' +
            ", machineName='" + machineName + '\'' +
            '}';
    }

    public String toHtml() {
        return "<table>"
            + "<tr><th colspan=2>Configuration Information</th></tr>"
            + ReportHandler.row(null, "td", "Machine Name", machineName)
            + ReportHandler.row(null, "td", "OS", os)
            + ReportHandler.row(null, "td", "JDK Version", jdkVersion)
            + "</table>";
    }
}