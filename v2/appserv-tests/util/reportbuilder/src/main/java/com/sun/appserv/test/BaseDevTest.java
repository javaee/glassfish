package com.sun.appserv.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public abstract class BaseDevTest {
    public final SimpleReporterAdapter stat;
    public PrintWriter writer;
    public static final boolean DEBUG = true;

    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
    public BaseDevTest() {
        if (DEBUG) {
            try {
                writer = new PrintWriter(new FileWriter("test.out"), true);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        stat = new SimpleReporterAdapter("appserv-tests", getTestName());
        stat.addDescription(getTestDescription());
    }

    protected abstract String getTestName();

    protected abstract String getTestDescription();

    public void report(String step, boolean success) {
        stat.addStatus(step, success ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
    }

    /**
     * Runs the command with the args given
     *
     * @param args
     *
     * @return true if successful
     */
    public boolean asadmin(final String... args) {
        List<String> command = new ArrayList<String>();
        command.add(System.getenv().get("S1AS_HOME") + "/bin/asadmin");
        command.addAll(Arrays.asList(antProp("as.props").split(" ")));
        command.addAll(Arrays.asList(args));
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = null;
        boolean success = false;
        try {
            process = builder.start();
            InputStream inStream = process.getInputStream();
            InputStream errStream = process.getErrorStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            try {
                final byte[] buf = new byte[1000];
                int read;
                while ((read = inStream.read(buf)) != -1) {
                    out.write(buf, 0, read);
                }
                while ((read = errStream.read(buf)) != -1) {
                    err.write(buf, 0, read);
                }
            } finally {
                errStream.close();
                inStream.close();
            }
            String outString = new String(out.toByteArray()).trim();
            String errString = new String(err.toByteArray()).trim();
            process.waitFor();
            success = process.exitValue() == 0 && !outString.contains(String.format("Command %s failed.", args[0]));
            write(outString);
            write(errString);
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (process != null) {
                process.destroy();
            }
            writer.close();
        }
        return success;
    }

    public String antProp(final String key) {
        String value = System.getProperty(key);
        if (value == null) {
            try {
                Properties props = new Properties();
                String apsHome = System.getenv("APS_HOME");
                FileReader reader = new FileReader(new File(apsHome, "config.properties"));
                try {
                    props.load(reader);
                } finally {
                    reader.close();
                }
                value = props.getProperty(key);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return value;
    }

    public void write(final String out) {
        if (DEBUG) {
            writer.println(out);
            writer.flush();
        }
    }
}
