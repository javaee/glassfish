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
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.config.SystemPropertiesAccess;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.ObjectName;
import java.util.Map;
import java.util.Set;

/**
 */
public final class SystemPropertiesAccessTest
        extends AMXTestBase {
    public SystemPropertiesAccessTest() {
    }

    private Set<ObjectName>
    getAll()
            throws Exception {
        final Set<ObjectName> objectNames =
                getQueryMgr().queryInterfaceObjectNameSet(
                        SystemPropertiesAccess.class.getName(), null);

        return (objectNames);
    }


    private void
    checkPropertiesGet(final SystemPropertiesAccess props) {
        final Map<String, String> all = props.getSystemProperties();

        final String[] propNames = props.getSystemPropertyNames();

        for (final String name : propNames) {
            assert (props.existsSystemProperty(name));
            final String value = props.getSystemPropertyValue(name);
        }
    }

    private void
    testPropertiesSetToSameValue(final SystemPropertiesAccess props) {
        final String[] propNames = props.getSystemPropertyNames();

        // get each property, set it to the same value, the verify
        // it's the same.
        for (int i = 0; i < propNames.length; ++i) {
            final String propName = propNames[i];

            final String value = props.getSystemPropertyValue(propName);
            props.setSystemPropertyValue(propName, value);

            assert (props.getSystemPropertyValue(propName).equals(value));
        }
    }

    private void
    testCreateEmptySystemProperty(final SystemPropertiesAccess props) {
        final String NAME = "test.empty";

        props.createSystemProperty(NAME, "");
        assert (props.existsSystemProperty(NAME));
        props.removeSystemProperty(NAME);
        assert (!props.existsSystemProperty(NAME));
    }

    private void
    testSystemPropertiesCreateRemove(final SystemPropertiesAccess props) {
        final String[] propNames = props.getSystemPropertyNames();

        // add some properties, then delete them
        final int numToAdd = 1;
        final long now = System.currentTimeMillis();
        for (int i = 0; i < numToAdd; ++i) {
            final String testName = "__junittest_" + i + now;

            if (props.existsSystemProperty(testName)) {
                failure("test property already exists: " + testName);
            }

            props.createSystemProperty(testName, "value_" + i);
            assert (props.existsSystemProperty(testName));
        }
        final int numProps = props.getSystemPropertyNames().length;

        if (numProps != numToAdd + propNames.length) {
            failure("expecting " + numProps + " have " + numToAdd + propNames.length);
        }

        // remove the ones we added
        for (int i = 0; i < numToAdd; ++i) {
            final String testName = "__junittest_" + i + now;

            props.removeSystemProperty(testName);
            assert (!props.existsSystemProperty(testName));
        }

        assert (props.getSystemPropertyNames().length == propNames.length);

    }

    public synchronized void
    checkGetProperties(final ObjectName src)
            throws Exception {
        final AMX proxy = getProxy(src, AMX.class);

        if (!(proxy instanceof SystemPropertiesAccess)) {
            throw new IllegalArgumentException(
                    "MBean does not implement SystemPropertiesAccess: " + quote(src));
        }

        final SystemPropertiesAccess props = (SystemPropertiesAccess) proxy;
        checkPropertiesGet(props);
    }

    public void
    checkSetPropertiesSetToSameValue(final ObjectName src)
            throws Exception {
        final SystemPropertiesAccess props = getProxy(src, SystemPropertiesAccess.class);

        testPropertiesSetToSameValue(props);
    }


    public void
    checkCreateRemove(final ObjectName src)
            throws Exception {
        final SystemPropertiesAccess props =
                getProxy(src, SystemPropertiesAccess.class);

        testSystemPropertiesCreateRemove(props);
    }

    public synchronized void
    testPropertiesGet()
            throws Exception {
        final Set<ObjectName> all = getAll();

        testAll(all, "checkGetProperties");
    }

    public synchronized void
    testPropertiesSetToSameValue()
            throws Exception {
        final Set<ObjectName> all = getAll();

        testAll(all, "checkSetPropertiesSetToSameValue");
    }


    public synchronized void
    testCreateRemove()
            throws Exception {
        if (checkNotOffline("testCreateRemove")) {
            final Set<ObjectName> all = getAll();
            testAll(all, "checkCreateRemove");
        }
    }

}


