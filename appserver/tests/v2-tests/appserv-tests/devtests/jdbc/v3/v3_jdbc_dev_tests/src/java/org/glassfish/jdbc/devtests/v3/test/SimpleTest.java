package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author jagadish
 */
public interface SimpleTest {
    Map<String,Boolean> runTest(DataSource ds, PrintWriter out);
}
