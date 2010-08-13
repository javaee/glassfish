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

import org.glassfish.hk2.classmodel.reflect.impl.TypeBuilder;
import org.glassfish.hk2.classmodel.reflect.impl.TypesImpl;
import org.glassfish.hk2.classmodel.reflect.util.DirectoryArchive;
import org.glassfish.hk2.classmodel.reflect.util.JarArchive;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parse jar files or directories and create the model for any classes found.
 *
 * @author Jerome Dochez
 */
public class Parser {

    private final ParsingContext context;
    private final Parser parent;
    private final Map<URI, Types> processedURI = Collections.synchronizedMap(new HashMap<URI, Types>());

    private final List<Future<Result>> futures = Collections.synchronizedList(new ArrayList<Future<Result>>());
    private ExecutorService executorService = null;
    
    public Parser(ParsingContext context) {
        this.context = context;
        executorService = context.executorService;
        parent = null;
    }

    private Parser(ParsingContext context, Parser parent) {
        this.context = context;
        executorService = context.executorService;
        this.parent = parent;
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
            if (executorService!=null) {
                executorService.shutdown();
                executorService=null;
            }
        }
        return exceptions.toArray(new Exception[exceptions.size()]);
    }

    public void parse(final File source, final Runnable doneHook) throws IOException {
        // todo : use protocol to lookup implementation
        final ArchiveAdapter adapter = source.isFile()?new JarArchive(source.toURI()):new DirectoryArchive(source);
        final Runnable cleanUpAndNotify = new Runnable() {
          @Override
          public void run() {
            try {
              try {
                adapter.close();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            } finally {
                if (doneHook!=null)
                    doneHook.run();
            }
          }
        };
        parse(adapter, cleanUpAndNotify);
    }

    public synchronized void parse(final ArchiveAdapter source, final Runnable doneHook) throws IOException {

        final Logger logger = context.logger;
        Types types = getResult(source.getURI());
        if (types!=null) {
            if (!processedURI.containsKey(source.getURI())) {
                processedURI.put(source.getURI(), types);    
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Skipping reparsing..." + source.getURI());
            }
            return;
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "submitting file " + source.getURI().getPath());
        }
        futures.add(getExecutorService().submit(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                try {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "elected file " + source.getURI().getPath());
                    }
                    doJob(source, doneHook);
                    
                    return new Result(source.getURI().getPath(), null);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Exception while parsing file " + source, e);
                    return new Result(source.getURI().getPath(), e);
                }
            }
        }));
    }

    private Types getResult(URI uri) {
        Types types = processedURI.get(uri);
        if (types==null && parent!=null) {
            types = parent.getResult(uri);
        }
        return types;
    }
                               
    private void saveResult(URI uri, Types types) {
        this.processedURI.put(uri, types);
        if (parent!=null) {
            parent.saveResult(uri, types);
        }
    }

    private void doJob(final ArchiveAdapter adapter, final Runnable doneHook) throws Exception {
        final Logger logger = context.logger;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Parsing " + adapter.getURI() + " on thread " + Thread.currentThread().getName());
        }
        if (context.archiveSelector == null || context.archiveSelector.selects(adapter)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Parsing file " + adapter.getURI().getPath());
            }
            final URI uri = adapter.getURI();

            adapter.onSelectedEntries(
                    new ArchiveAdapter.Selector() {
                        @Override
                        public boolean isSelected(ArchiveAdapter.Entry entry) {
                            return entry.name.endsWith(".class");
                        }
                    },
                    new ArchiveAdapter.EntryTask() {
                        @Override
                        public void on(ArchiveAdapter.Entry entry, byte[] bytes) throws IOException {
                            if (logger.isLoggable(Level.FINER)) {
                                logger.log(Level.FINER, "Parsing class " + entry.name);
                            }

                            ClassReader cr = new ClassReader(bytes);
                            cr.accept(context.getClassVisitor(uri, entry.name), ClassReader.SKIP_DEBUG);
                        }
                    },
                    logger
            );
            saveResult(uri, context.getTypes());
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "before running doneHook" + adapter.getURI().getPath());
        }
        if (doneHook != null)
            doneHook.run();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "after running doneHook " + adapter.getURI().getPath());
        }
    }

    /**
     * Returns the context this parser instance was initialized with during
     * the call to {@link Parser#Parser(ParsingContext)}
     *
     * @return the parsing context this parser uses to store the parsing
     * activities results.
     */
    public ParsingContext getContext() {
        return context;
    }

    private synchronized ExecutorService getExecutorService() {
        if (executorService==null) {
            Runtime runtime = Runtime.getRuntime();
            int nrOfProcessors = runtime.availableProcessors();
            executorService = Executors.newFixedThreadPool(nrOfProcessors, new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("Hk2-jar-scanner");
                        return t;
                    }
                });
        }
        return executorService;
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
