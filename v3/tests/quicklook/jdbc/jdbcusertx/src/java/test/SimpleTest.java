/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myapp.test;

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
