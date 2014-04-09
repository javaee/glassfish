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
package org.glassfish.hk2.classmodel.reflect;

import org.glassfish.hk2.classmodel.reflect.impl.TypeProxy;
import org.glassfish.hk2.classmodel.reflect.impl.TypesCtr;
import org.glassfish.hk2.classmodel.reflect.util.DirectoryArchive;
import org.glassfish.hk2.classmodel.reflect.util.JarArchive;
import org.glassfish.hk2.classmodel.reflect.util.ResourceLocator;
import org.glassfish.hk2.external.org.objectweb.asm.ClassReader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parse jar files or directories and create the model for any classes found.
 *
 * @author Jerome Dochez
 */
public class Parser implements Closeable {

    public static final String DEFAULT_WAIT_SYSPROP = "hk2.parser.timeout";
      
    private final ParsingContext context;
    private final Map<String, Types> processedURI = Collections.synchronizedMap(new HashMap<String, Types>());

    private final Stack<Future<Result>> futures = new Stack<Future<Result>>();
    private final ExecutorService executorService;
    private final boolean ownES;

    // used to safeguard between await and parse
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final int DEFAULT_TIMEOUT = Integer.getInteger(DEFAULT_WAIT_SYSPROP, 100);
    
    
    public Parser(ParsingContext context) {
        this.context = context;
        executorService = (context.executorService==null?createExecutorService():context.executorService);
        ownES = context.executorService==null;
    }
    
    public Exception[] awaitTermination() throws InterruptedException {
        return awaitTermination(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    public Exception[] awaitTermination(int timeOut, TimeUnit unit) throws InterruptedException {
        List<Exception> exceptions = new ArrayList<Exception>();
        final Logger logger = context.logger;
        while(futures.size()>0) {
            if (context.logger.isLoggable(Level.FINE)) {
                 context.logger.log(Level.FINE, "Await iterating at " + System.currentTimeMillis() + " waiting for " + futures.size());
            }
            Future<Result> f;
            synchronized(futures) {
                try {
                    f = futures.pop();
                } catch(EmptyStackException e) {
                    // it's ok, another thread took the load from us.
                    f = null;
                }
            }
            if (f!=null) {
                try {
                    Result result = f.get(timeOut, unit);
                    if (context.logger.isLoggable(Level.FINE)) {
                        context.logger.log(Level.FINE, "future finished at " + System.currentTimeMillis() + " for " + result.name);
                    }
                    context.logger.log(Level.FINER, "result {0}", result);
                    if (result!=null && result.fault!=null) {
                       context.logger.log(Level.WARNING, "result fault {0}", result);
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
        }

        lock.writeLock().lock();
        try {

            // now we need to visit all the types that got referenced but not visited
            final ResourceLocator locator = context.getLocator();
            if (locator!=null) {
                context.logger.info("visiting unvisited references");
              
                context.types.onNotVisitedEntries(new TypesCtr.ProxyTask() {
                    @Override
                    public void on(TypeProxy<?> proxy) {

                        String name = proxy.getName();
                        // make this name a resource name...
                        String resourceName = name.replaceAll("\\.", "/") + ".class";

                        InputStream is = null;
                        try {
                            is = locator.openResourceStream(resourceName);
                            if (null == is) {
                              return;
                            }

                            // now visit...
                            logger.log(Level.FINE, "Going to visit {0}", resourceName);
                            ClassReader cr = new ClassReader(is);
                            try {
                                URL url = locator.getResource(resourceName);
                                File file = getFilePath(url.getPath(), resourceName);
                                URI definingURI = getDefiningURI(file);
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine("file=" + file + "; definingURI=" + definingURI);
                                }
                                cr.accept(context.getClassVisitor(definingURI, resourceName), ClassReader.SKIP_DEBUG);
                            } catch (Throwable e) {
                                logger.log(Level.SEVERE, "Exception while visiting {0}", name);
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (is != null)
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    logger.log(Level.SEVERE, "Exception while closing " + resourceName + " stream", e);
                                }
                        }
                    }
                });
            } else {
                context.types.clearNonVisitedEntries();
            }
            close();
            return exceptions.toArray(new Exception[exceptions.size()]);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static URI getDefiningURI(File file) {
      try {
        return file.getCanonicalFile().toURI();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
    private static File getFilePath(String path, String resourceName) {
      path = path.substring(0, path.length() - resourceName.length());
      if (path.endsWith("!/")) {
        path = path.substring(0, path.length()-2);
      }
      if (path.startsWith("file:/")) {
        path = path.substring(6, path.length());
      }
      File file = new File(path);
      return file;
    }
    
    @Override
    public void close() {
      // if we own the executor service, time to shut it down.
      if (executorService!=null && ownES) {
          executorService.shutdown();
      }
    }
    
    public void parse(final File source, final Runnable doneHook) throws IOException {
        // todo : use protocol to lookup implementation
        final ArchiveAdapter adapter = createArchiveAdapter(source, doneHook);
        
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
              if (doneHook!=null) {
                  doneHook.run();
              }
            }
          }
        };
        parse(adapter, cleanUpAndNotify);
    }

    private ArchiveAdapter createArchiveAdapter(File source, Runnable doneHook)
            throws IOException {
      try {
        ArchiveAdapter aa = source.isFile()?new JarArchive(this, source.toURI()):
                                            new DirectoryArchive(this, source);
        return aa;
      } catch (IOException e) {
        if (doneHook!=null) {
          doneHook.run();
        }
        throw e;
      }
    }

    /**
     * Parse the archive adapter entries and run the runnable hook on completion.
     *
     * @param source the archive adapter to parse
     * @param doneHook the runnable hook to run after completion
     * @return the future object to monitor the result of the parsing.
     *
     * @throws IOException thrown by the source archive adapter when accessing entries
     */
    public Future<Result> parse(final ArchiveAdapter source, final Runnable doneHook) throws IOException {

        lock.readLock().lock();
        try {

            ExecutorService es = executorService;
            boolean immediateShutdown = false;
            if (es.isShutdown()) {
                context.logger.info("Executor service is shutdown, since awaitTermination was called, " +
                        "provide an executor service instance from the context to avoid automatic shutdown");
                es = createExecutorService();
                immediateShutdown = true;
            }
            final Logger logger = context.logger;
            Types types = getResult(source.getURI());
            if (types!=null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Skipping reparsing..." + source.getURI());
                }
                doneHook.run();
                return null;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "at " + System.currentTimeMillis() + "in " + this + " submitting file " + source.getURI().getPath());
                logger.log(Level.FINE, "submitting file " + source.getURI().getPath());
            }
            Future<Result> future = es.submit(new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    try {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, "elected file " + source.getURI().getPath());
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            context.logger.log(Level.FINE, "started working at " + System.currentTimeMillis() + "in "
                                    + this + " on " + source.getURI().getPath());
                        }
                        doJob(source, doneHook);

                        return new Result(source.getURI().getPath(), null);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Exception while parsing file " + source.getURI(), e);
                        return new Result(source.getURI().getPath(), e);
                    }
                }
            });
            synchronized(futures) {
                futures.add(future);
            }
            if (immediateShutdown) {
                es.shutdown();
            }
            return future;
        } finally {
            lock.readLock().unlock();
        }
    }

    private synchronized Types getResult(URI uri) {
        return processedURI.get(uri.getSchemeSpecificPart());
    }
                               
    private synchronized void saveResult(URI uri, Types types) {
        this.processedURI.put(uri.getPath(), types);
    }

    private void doJob(final ArchiveAdapter adapter, final Runnable doneHook) throws Exception {
        final Logger logger = context.logger;
        long startTime = System.currentTimeMillis();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Parsing " + adapter.getURI() + " on thread " + Thread.currentThread().getName());
        }
        if (context.archiveSelector == null || context.archiveSelector.selects(adapter)) {
            final URI uri = adapter.getURI();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Parsing file {0}", uri.getPath());
            }

            adapter.onSelectedEntries(
                    new ArchiveAdapter.Selector() {
                        @Override
                        public boolean isSelected(ArchiveAdapter.Entry entry) {
                            return entry.name.endsWith(".class");
                        }
                    },
                    new ArchiveAdapter.EntryTask() {
                        @Override
                        public void on(ArchiveAdapter.Entry entry, InputStream is) throws IOException {
                            if (logger.isLoggable(Level.FINER)) {
                                logger.log(Level.FINER, "Parsing class " + entry.name);
                            }
                            try {
                                ClassReader cr = new ClassReader(is);
                                cr.accept(context.getClassVisitor(uri, entry.name, true), ClassReader.SKIP_DEBUG);
                            } catch (Throwable e) {
                                logger.log(Level.SEVERE, "Exception while visiting " + entry.name
                                        + " of size " + entry.size, e);
                            }
                        }
                    },
                    logger
            );
            saveResult(uri, context.getTypes());
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE,"Finished parsing " + adapter.getURI().getPath() + " at " + System.currentTimeMillis() + " in "
                + (System.currentTimeMillis() - startTime) + " ms");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "before running doneHook" + adapter.getURI().getPath());
        }
        if (doneHook != null) {
            doneHook.run();
        }
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

    private ExecutorService createExecutorService() {
        // TODO: may have to dumb this down since there appears to be issues in ZipFile processing when there is contention on the same file or directory
        // FIXME: anything other than 1 is not stable since the class-model framework is not thread safe (visitors are shared)
//        Runtime runtime = Runtime.getRuntime();
//        int nbOfProcessors = runtime.availableProcessors();
        int nbOfProcessors = 1;
        
        return Executors.newFixedThreadPool(nbOfProcessors, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("Hk2-jar-scanner-" + t.getId());
                t.setDaemon(true);
                return t;
            }
        });
    }
    

    public static class Result {
        public final String name;
        public  final Exception fault;

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
