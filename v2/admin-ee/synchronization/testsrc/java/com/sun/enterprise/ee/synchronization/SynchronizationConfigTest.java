/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.ee.synchronization;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.util.List;

public class SynchronizationConfigTest extends TestCase {

    public SynchronizationConfigTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testSynchronizationConfig() {         
        SynchronizationConfig synConfig = new SynchronizationConfig(
                     SynchronizationDriverFactory.INSTANCE_CONFIG_URL);

        int count = synConfig.getSyncCount();
        if ( count == INSTANCE_SYNC_COUNT) {
            System.out.println("Total number of sync elements " + count);
        } else {
            fail("Expected " + INSTANCE_SYNC_COUNT + " got " + count);
        }

        SynchronizationRequest[] reqs = synConfig.getSyncRequests();

        // config dir props verification

        boolean shallow = reqs[0].isShallowCopyEnabled();
        if ( shallow == CONFIG_DIR_SHALLOW_COPY_ENABLED ) {
            System.out.println("shallow copy enabled in config directory");
        }else {
            fail("shallow copy is not enabled in config directory. Expected  to be on");
        }

        // apps dir verification props

        boolean gcType = reqs[1].isGCEnabled();
        if ( gcType == APPLICATIONS_GC_ENABLED ) {
            System.out.println("GC is enabled in applications directory");
        }else {
            fail("GC is off in applications directory. Expected to be on");
        }

        // generated dir verification props

        int tsType = reqs[2].getTimestampType();

        if(tsType == GENERATED_TIMESTAMP_TYPE) {
            System.out.println(" config's timestamp-type is modified-since as expected");
        } else {
            fail(" domain.xml's timestamp-type expected to be modified-since");
        }

        // lib dir verification props

        List list = reqs[3].getExcludePatternList();

        if ( ( list != null) && ( list.size() == LIB_EXCLUDE_LIST_LENGTH) ){
            System.out.println(" There is(are) " + list.size() + " exclude patterns in lib directive");
        } else {
            fail("Expected " + LIB_EXCLUDE_LIST_LENGTH + " got " + list.size() +  "exclude patterns in lib directive");
        }

        // docroot dir verification props
        
        boolean exclude = reqs[4].isExclude();

        if(exclude == DOCROOT_EXCLUDE_PROPERTY) {
            System.out.println(" exclude is false in lib as expected");
        } else {
            fail(" Exclude must be off in lib directory");
        }
        
    }

    public void testNodeAgentConfig() {         
        SynchronizationConfig synConfig = new SynchronizationConfig(
                     SynchronizationDriverFactory.NODE_AGENT_CONFIG_URL);

        int count = synConfig.getSyncCount();
        if ( count == NA_SYNC_COUNT) {
            System.out.println("Total number of sync elements " + count);
        } else {
            fail("Expected " + NA_SYNC_COUNT + " got " + count);
        }

        SynchronizationRequest[] reqs = synConfig.getSyncRequests();

        // config dir props verification

        boolean shallow = reqs[0].isShallowCopyEnabled();
        if ( shallow == NA_SHALLOW_COPY_ENABLED ) {
            System.out.println("Node agent: shallow copy enabled ");
        }else {
            fail("Node agent: shallow copy is not enabled");
        }

        // apps dir verification props

        boolean gcType = reqs[0].isGCEnabled();
        if ( gcType == NA_GC_ENABLED ) {
            System.out.println("Node agent: GC is enabled ");
        }else {
            fail("Node agent: GC is . Expected to be on");
        }

        // generated dir verification props

        int tsType = reqs[0].getTimestampType();

        if(tsType == NA_TIMESTAMP_TYPE) {
            System.out.println("Node agent:  timestamp-type is modified-since as expected");
        } else {
            fail("Node agent: domain.xml's timestamp-type expected to be modified-since");
        }

        // lib dir verification props

        List list = reqs[0].getExcludePatternList();

        if ( ( list != null) && ( list.size() == NA_EXCLUDE_LIST_LENGTH) ){
            System.out.println("Node agent: There is(are) " + list.size() + " exclude patterns ");
        } else {
            fail("Node agent: Expected " + NA_EXCLUDE_LIST_LENGTH + " got " + list.size() +  "exclude patterns ");
        }

        // docroot dir verification props
        
        boolean exclude = reqs[0].isExclude();

        if(exclude == NA_EXCLUDE_PROPERTY) {
            System.out.println("Node agent: exclude is false  as expected");
        } else {
            fail("Node agent: Exclude must be off ");
        }
        
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(SynchronizationConfigTest.class);
    }

    /** The following need to change if synchronization-meta-data.xml changes**/

    boolean  DOCROOT_EXCLUDE_PROPERTY = false;

    boolean APPLICATIONS_GC_ENABLED = true;

    boolean  CONFIG_DIR_SHALLOW_COPY_ENABLED = true;

    int INSTANCE_SYNC_COUNT = 5;

    int LIB_EXCLUDE_LIST_LENGTH = 1;

    int GENERATED_TIMESTAMP_TYPE = SynchronizationRequest.TIMESTAMP_MODIFIED_SINCE;

    /** The following need to change if na-synchronization-meta-data.xml changes**/

    boolean  NA_EXCLUDE_PROPERTY = false;

    boolean NA_GC_ENABLED = false;

    boolean  NA_SHALLOW_COPY_ENABLED = false;

    int NA_SYNC_COUNT = 5;

    int NA_EXCLUDE_LIST_LENGTH = 0;

    int NA_TIMESTAMP_TYPE = SynchronizationRequest.TIMESTAMP_MODIFICATION_TIME;

}
