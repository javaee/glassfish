/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.classmodel.reflect.test.ordering;

import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.util.InputStreamArchiveAdapter;
import org.junit.Ignore;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * main program to test a particular archive scanning
 * 
 */
@Ignore
public class JarTest {
    public static void main(String[] args) {
        if (args.length!=1) {
            System.out.println("usage : JarTest <path_to_jar_file>");
            return;
        }
        JarTest jt = new JarTest();
        try {
            jt.process(args[0]);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void process(String path) throws IOException {
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("File not found : " + path);
            return;
        }
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINE);
        ParsingContext pc = new ParsingContext.Builder().logger(logger).build();
        Parser parser = new Parser(pc);
        long start = System.currentTimeMillis();

        parser.parse(f, new Runnable() {
            @Override
            public void run() {
                System.out.println("Done parsing");
            }
        });
        try {
            parser.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Collection<Type> types = pc.getTypes().getAllTypes();
        System.out.println("Finished parsing " + types.size() + " classes in " + (System.currentTimeMillis() - start) + "ms");

    }

}
