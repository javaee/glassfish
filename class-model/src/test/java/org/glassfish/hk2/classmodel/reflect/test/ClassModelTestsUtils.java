/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package org.glassfish.hk2.classmodel.reflect.test;

import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Aug 12, 2010
 * Time: 7:01:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassModelTestsUtils {

    static Types types = null;

    private final static ClassModelTestsUtils instance = new ClassModelTestsUtils();


    public static Types getTypes() throws IOException, InterruptedException {

        synchronized(instance) {

            if (types == null) {
                File userDir = new File(System.getProperty("user.dir"));
                File modelDir = new File(userDir, "target" + File.separator + "test-classes");

                if (modelDir.exists()) {
                    ParsingContext pc = (new ParsingContext.Builder().config(new ParsingConfig() {
                        @Override
                        public Set<String> getAnnotationsOfInterest() {
                            return Collections.emptySet();
                        }

                        @Override
                        public Set<String> getTypesOfInterest() {
                            return Collections.emptySet();
                        }

                        @Override
                        public boolean modelUnAnnotatedMembers() {
                            return true;
                        }
                    })).build();
                    Parser parser = new Parser(pc);

                    parser.parse(modelDir, null);
                    Exception[] exceptions = parser.awaitTermination(100, TimeUnit.SECONDS);
                    if (exceptions!=null) {
                        for (Exception e : exceptions) {
                            System.out.println("Found Exception ! : " +e);
                        }
                        Assert.assertTrue("Exceptions returned", exceptions.length==0);
                    }
                    types = pc.getTypes();
                }
            }
        }
        return types;
    }

}
