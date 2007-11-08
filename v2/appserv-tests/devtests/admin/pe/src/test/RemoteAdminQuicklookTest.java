package test;
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

import javax.management.MBeanServerConnection;
public interface RemoteAdminQuicklookTest {
    /** Performs an arbitrary test and returns the result of the same as a String.
     * The returned result must be something that is available as status string in
     * com.sun.ejte.ccl.reporter.SimpleReporterAdapter.PASS and FAIL.
     */
    public String test();
    
    /** Sets the MBeanServerConnection for testing the stuff remotely. The parameter may
     * not be null.
     */
    public void setMBeanServerConnection(final MBeanServerConnection c);
    
    /** Returns the name of the test.
     */
    public String getName();
    
    /** Returns the time taken by the test to execute in milliseconds
     * @return long denoting the time taken for this test to execute
     */
    public long getExecutionTime();
}