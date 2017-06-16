/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.common.util.admin.locking;

import junit.framework.Assert;
import org.glassfish.common.util.admin.ManagedFile;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;

/**
 * Tests for ManagedFile.accessRead() method
 * @author Jerome Dochez
 */
public class ReadLockTest {

    @Test
    public void readLock() throws IOException {

        File f = getFile();
        try {
            //System.out.println("trying the lock on " + f.getAbsolutePath());
            final ManagedFile managed = new ManagedFile(f, 1000, 1000);
            Lock fl = managed.accessRead();
            //System.out.println("Got the lock on " + f.getAbsolutePath());
            List<Future<Boolean>> results = new ArrayList<Future<Boolean>>(5);
            for (int i=0;i<5;i++) {
                results.add(Executors.newFixedThreadPool(2).submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            Lock second = managed.accessRead();
                            second.unlock();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return Boolean.TRUE;
                    }
                }));
            }
            Thread.sleep(100);
            fl.unlock();
            for (Future<Boolean> result : results) {
                Boolean exitCode = result.get();
                Assert.assertEquals(exitCode.booleanValue(), true);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    


    public File getFile() throws IOException {
        Enumeration<URL> urls = getClass().getClassLoader().getResources("adminport.xml");
        if (urls.hasMoreElements()) {
            try {
                File f = new File(urls.nextElement().toURI());
                if (f.exists()) {
                    return f;
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else {
            //System.out.println("Not found !");
        }
        return null;
    }    
}
