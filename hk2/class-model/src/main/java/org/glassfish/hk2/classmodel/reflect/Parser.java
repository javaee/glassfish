/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 *  The contents of this file are subject to the terms of either the GNU
 *  General Public License Version 2 only ("GPL") or the Common Development
 *  and Distribution License("CDDL") (collectively, the "License").  You
 *  may not use this file except in compliance with the License. You can obtain
 *  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 *  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 *  When distributing the software, include this License Header Notice in each
 *  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *  Sun designates this particular file as subject to the "Classpath" exception
 *  as provided by Sun in the GPL Version 2 section of the License file that
 *  accompanied this code.  If applicable, add the following below the License
 *  Header, with the fields enclosed by brackets [] replaced by your own
 *  identifying information: "Portions Copyrighted [year]
 *  [name of copyright owner]"
 *
 *  Contributor(s):
 *
 *  If you wish your version of this file to be governed by only the CDDL or
 *  only the GPL Version 2, indicate your decision by adding "[Contributor]
 *  elects to include this software in this distribution under the [CDDL or GPL
 *  Version 2] license."  If you don't indicate a single choice of license, a
 *  recipient has the option to distribute your version of this file under
 *  either the CDDL, the GPL Version 2 or to extend the choice of license to
 *  its licensees as provided above.  However, if you add GPL Version 2 code
 *  and therefore, elected the GPL Version 2 license, then the option applies
 *  only if the new code is made subject to such option by the copyright
 *  holder.
 */
package org.glassfish.hk2.classmodel.reflect;

import org.glassfish.hk2.classmodel.reflect.util.DirectoryArchive;
import org.glassfish.hk2.classmodel.reflect.util.JarArchive;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Parse jar files or directories and create the model for any classes found.
 *
 * @author Jerome Dochez
 */
public class Parser {

    private final ParsingContext context;

    private final List<Future<Result>> futures = Collections.synchronizedList(new ArrayList<Future<Result>>());
    public Parser(ParsingContext context) {
        this.context = context;
    }

    public Exception[] awaitTermination() throws InterruptedException {
        return awaitTermination(10, TimeUnit.SECONDS);
    }
    
    public synchronized Exception[] awaitTermination(int timeOut, TimeUnit unit) throws InterruptedException {


        List<Exception> exceptions = new ArrayList<Exception>();
        if (context.logger.isLoggable(Level.FINE)) {
            context.logger.log(Level.FINE, "awaiting termination of " + futures.size() + " tasks");
        }

        for (Future<Result> f : futures) {
            try {
                Result result = f.get(timeOut, unit);
                if (context.logger.isLoggable(Level.FINER)) {
                    context.logger.log(Level.FINER, "result " + result);
                    if (result!=null && result.fault!=null) {
                        context.logger.log(Level.FINER, "result fault" + result);
                    }
                }
                if (result!=null && result.fault!=null) {
                    exceptions.add(result.fault);
                }
            } catch (TimeoutException e) {
                exceptions.add(e);
            } catch (ExecutionException e) {
                exceptions.add(e);
            }
        }
        return exceptions.toArray(new Exception[exceptions.size()]);
    }

    public void parse(final File source, final Runnable doneHook) throws IOException {
        final ArchiveAdapter adapter = source.isFile()?new JarArchive(new JarFile(source)):new DirectoryArchive(source);
        parse(adapter, doneHook);
    }

    public synchronized void parse(final ArchiveAdapter source, final Runnable doneHook) throws IOException {

        if (context.logger.isLoggable(Level.FINE)) {
            context.logger.log(Level.FINE, "submitting file " + source.getName());
        }
        futures.add(context.executorService.submit(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                try {
                    if (context.logger.isLoggable(Level.FINE)) {
                        context.logger.log(Level.FINE, "elected file " + source.getName());
                    }
                    doJob(source, doneHook);
                    return new Result(source.getName(), null);
                } catch (Exception e) {
                    context.logger.log(Level.SEVERE, "Exception while parsing file " + source, e);
                    return new Result(source.getName(), e);
                }
            }
        }));
    }

    private void doJob(final ArchiveAdapter adapter, final Runnable doneHook) throws Exception {
        if (context.archiveSelector==null || context.archiveSelector.selects(adapter)) {
            if (context.logger.isLoggable(Level.FINE)) {
                context.logger.log(Level.FINE, "Parsing file " + adapter.getName());
            }
            for (final ArchiveAdapter.Entry entry : adapter) {
                if (entry.name.endsWith(".class")) {
                    if (context.logger.isLoggable(Level.FINER)) {
                        context.logger.log(Level.FINER, "Parsing class " + entry.name);
                    }
                    ClassReader cr = new ClassReader(adapter.getInputStream(entry.name));
                    cr.accept(context.getClassVisitor(), ClassReader.SKIP_DEBUG );
                }
            }
            if (context.logger.isLoggable(Level.FINE)) {
                context.logger.log(Level.FINE, "before running doneHook" + adapter.getName());
            }
            doneHook.run();
            if (context.logger.isLoggable(Level.FINE)) {
                context.logger.log(Level.FINE, "after running doneHook " + adapter.getName());
            }
        }
    }

    private class Result {
        final String name;
        final Exception fault;

        private Result(String name, Exception fault) {
            this.name = name;
            this.fault = fault;
        }

        @Override
        public String toString() {
            return super.toString() + " Result for " + name;
        }
    }
}
