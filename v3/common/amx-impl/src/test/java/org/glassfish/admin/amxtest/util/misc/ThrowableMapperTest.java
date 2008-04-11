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
package org.glassfish.admin.amxtest.util.misc;

import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.ThrowableMapper;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ThrowableMapperTest
        extends junit.framework.TestCase {
    public ThrowableMapperTest() {
    }

    private List<Throwable>
    getStandard() {
        final List<Throwable> items = new ArrayList<Throwable>();

        items.add(new Throwable("Throwable test"));
        items.add(new Exception("Exception test"));
        items.add(new RuntimeException("RuntimeException test"));
        items.add(new IOException("IOException test"));
        items.add(new Error("Error test"));

        items.add(new MBeanException(
                new Exception("within MBeanException"), "MBeanException test"));
        items.add(new AttributeNotFoundException("Test"));
        items.add(new InstanceNotFoundException("InstanceNotFoundException test "));

        items.add(new ClassNotFoundException("foo.bar"));

        return items;
    }

    private List<Throwable>
    _testStandardWrappedWithStandard(final Throwable cause) {
        final List<Throwable> items = new ArrayList<Throwable>();

        final Throwable t = new Throwable("hello", cause);
        assert (t == ThrowableMapper.map(t));
        items.add(t);

        final Exception e = new Exception("hello", cause);
        assert (e == ThrowableMapper.map(e));
        items.add(e);

        final RuntimeException r = new RuntimeException("hello", cause);
        assert (r == ThrowableMapper.map(r));
        items.add(r);

        final IOException i = new IOException("hello");
        i.initCause(cause);
        assert (i == ThrowableMapper.map(i));
        items.add(i);

        final Error err = new Error("hello", cause);
        assert (err == ThrowableMapper.map(err));
        items.add(err);

        return items;
    }

    public List<Throwable>
    _testStandardThrowables(final List<Throwable> items) {
        final List<Throwable> results = new ArrayList<Throwable>();

        for (final Throwable item : items) {
            results.addAll(_testStandardWrappedWithStandard(item));
        }

        return results;
    }

    public void
    testStandard() {
        final List<Throwable> items = _testStandardThrowables(getStandard());

        _testStandardThrowables(items);
    }


    private static final class ProprietaryThrowable
            extends Throwable {
        public static final long serialVersionUID = 987324; // eliminate compile warnings

        public ProprietaryThrowable(String msg) { super(msg); }

        public ProprietaryThrowable(
                String msg,
                Throwable cause) { super(msg, cause); }
    }

    private static final class ProprietaryException
            extends Exception {
        public static final long serialVersionUID = 987324; // eliminate compile warnings

        public ProprietaryException(String msg) { super(msg); }

        public ProprietaryException(
                String msg,
                Throwable cause) { super(msg, cause); }
    }

    private static final class ProprietaryRuntimeException
            extends Exception {
        public static final long serialVersionUID = 987324; // eliminate compile warnings

        public ProprietaryRuntimeException(String msg) { super(msg); }

        public ProprietaryRuntimeException(
                String msg,
                Throwable cause) { super(msg, cause); }
    }

    private static final class ProprietaryError
            extends Error {
        public static final long serialVersionUID = 987324; // eliminate compile warnings

        public ProprietaryError(String msg) { super(msg); }

        public ProprietaryError(
                String msg,
                Throwable cause) { super(msg, cause); }
    }

    private List<Throwable>
    getProprietary() {
        final List<Throwable> items = new ArrayList<Throwable>();

        items.add(new ProprietaryThrowable("ProprietaryThrowable test"));
        items.add(new ProprietaryException("ProprietaryException test"));
        items.add(new ProprietaryRuntimeException("ProprietaryRuntimeException test"));
        items.add(new ProprietaryError("ProprietaryError test"));

        return items;
    }


    private void
    verifyMapping(
            final Throwable original,
            final Throwable remapped) {
        assert (remapped != original);
        assert (remapped.getClass() != original.getClass());

        if (original instanceof RuntimeException) {
            assert (remapped instanceof RuntimeException);
        }
        if (original instanceof Error) {
            assert (remapped instanceof Error);
        }
        if (original instanceof Exception) {
            assert (remapped instanceof Exception);
        }

        assert (original.getMessage().equals(remapped.getMessage()));

        assert (
                ExceptionUtil.getCauses(original).length ==
                        ExceptionUtil.getCauses(remapped).length);

        if (original.getCause() != null) {
            verifyMapping(original.getCause(), remapped.getCause());
        }

        final StackTraceElement[] originalStackTrace = original.getStackTrace();
        final StackTraceElement[] remappedStackTrace = remapped.getStackTrace();
        assert (originalStackTrace.length == remappedStackTrace.length);
        for (int i = 0; i < originalStackTrace.length; ++i) {
            assert (originalStackTrace[i] == remappedStackTrace[i]);
        }

    }

    public void
    xxtestProprietary() {
        final Throwable t = new ProprietaryThrowable("hello");

        final Throwable rm = ThrowableMapper.map(t);

        final StackTraceElement[] originalStackTrace = t.getStackTrace();
        final StackTraceElement[] remappedStackTrace = rm.getStackTrace();
        assert (originalStackTrace.length == remappedStackTrace.length);
        for (int i = 0; i < originalStackTrace.length; ++i) {
            assert (originalStackTrace[i] == remappedStackTrace[i]);
        }
    }

    public void
    testProprietary() {
        final List<Throwable> items = getProprietary();

        for (final Throwable item : items) {
            final Throwable remapped = ThrowableMapper.map(item);

            verifyMapping(item, remapped);
        }
    }
}


 	






