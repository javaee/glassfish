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
package org.jvnet.hk2.testing.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream; // for javadoc only

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import java.lang.reflect.AnnotatedElement;

import java.net.URL;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Collection; // for javadoc only
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import java.util.zip.ZipEntry;;
import java.util.zip.ZipFile;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.api.ServiceLocatorState;

import org.glassfish.hk2.external.org.objectweb.asm.AnnotationVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.ClassReader;
import org.glassfish.hk2.external.org.objectweb.asm.ClassVisitor;
import org.glassfish.hk2.external.org.objectweb.asm.Opcodes;

import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import org.junit.ClassRule; // for javadoc only
import org.junit.Test;

import org.junit.rules.ExternalResource;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import org.jvnet.hk2.annotations.Service; // for javadoc only

import org.jvnet.hk2.testing.junit.annotations.Classes;
import org.jvnet.hk2.testing.junit.annotations.InhabitantFiles;
import org.jvnet.hk2.testing.junit.annotations.Packages;

import org.jvnet.hk2.testing.junit.internal.ClassVisitorImpl;
import org.jvnet.hk2.testing.junit.internal.ErrorServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * An {@link ExternalResource} (and a {@link Binder}) that sets up and
 * tears down an HK2 {@link ServiceLocator} on a per-test-class or
 * per-test-method basis.
 *
 * @param <T> the type of JUnit test this {@link
 * ServiceLocatorTestRule} is related to; consider making it an
 * instance of {@link Binder}
 *
 * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
 *
 * @since 2.4.0-b33
 *
 * @see ExternalResource
 *
 * @see Binder
 */
public class ServiceLocatorTestRule<T> extends ExternalResource implements Binder {


  /*
   * Instance fields.
   */


  /**
   * The test instance instantiating this {@link
   * ServiceLocatorTestRule}.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final T test;

  /**
   * The {@link ServiceLocatorIsolation} designating whether the
   * {@link ServiceLocator} associated with this {@link
   * ServiceLocatorTestRule} is set up for each test method or shared
   * among them.
   *
   * <p>This field is never {@code null}.</p>
   */
  private final ServiceLocatorIsolation isolation;

  /**
   * A {@code boolean} indicating whether this {@link
   * ServiceLocatorTestRule} is configured to output additional
   * information to {@link System#out System.out}.
   *
   * @see #isVerbose()
   */
  private final boolean verbose;
  
  /**
   * The {@link Description} describing the JUnit test method
   * currently executing.
   *
   * <p>This field may be {@code null}.</p>
   *
   * @see #apply(Statement, Description)
   */
  private Description description;

  /**
   * The {@link ServiceLocator} in effect for the currently executing
   * JUnit test method.
   *
   * <p>This field may be {@code null}.</p>
   */
  private ServiceLocator serviceLocator;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link ServiceLocatorTestRule} on behalf of the
   * supplied JUnit test in {@link ServiceLocatorIsolation#PER_TEST}
   * isolation in non-verbose mode.
   *
   * @param test the JUnit test; must not be {@code null}
   *
   * @exception AssertionError if {@code test} is {@code null}
   *
   * @see #ServiceLocatorTestRule(Object, ServiceLocatorIsolation,
   * boolean)
   */
  public ServiceLocatorTestRule(final T test) {
    this(test, ServiceLocatorIsolation.PER_TEST, false);
  }

  /**
   * Creates a new {@link ServiceLocatorTestRule} on behalf of the
   * supplied JUnit test in {@link ServiceLocatorIsolation#PER_TEST}
   * isolation with the supplied verbosity.
   *
   * @param test the JUnit test; must not be {@code null}
   *
   * @param verbose whether this {@link ServiceLocatorTestRule} should
   * output additional information to {@link System#out System.out}
   *
   * @exception AssertionError if {@code test} is {@code null}
   *
   * @see #ServiceLocatorTestRule(Object, ServiceLocatorIsolation,
   * boolean)
   */
  public ServiceLocatorTestRule(final T test, final boolean verbose) {
    this(test, ServiceLocatorIsolation.PER_TEST, verbose);
  }

  /**
   * Creates a new {@link ServiceLocatorTestRule} on behalf of the
   * supplied JUnit test in the given {@link ServiceLocatorIsolation}
   * in non-verbose mode.
   *
   * @param test the JUnit test; must not be {@code null}
   *
   * @param isolation the {@link ServiceLocatorIsolation}; if {@code
   * null} then {@link ServiceLocatorIsolation#PER_TEST} will be used
   * instead
   * 
   * @exception AssertionError if {@code test} is {@code null}
   *
   * @see #ServiceLocatorTestRule(Object, ServiceLocatorIsolation,
   * boolean)
   */
  public ServiceLocatorTestRule(final T test, final ServiceLocatorIsolation isolation) {
    this(test, isolation, false);
  }

  /**
   * Creates a new {@link ServiceLocatorTestRule} on behalf of the
   * supplied JUnit test in the given {@link ServiceLocatorIsolation}.
   *
   * @param test the JUnit test; must not be {@code null}
   *
   * @param isolation the {@link ServiceLocatorIsolation}; if {@code
   * null} then {@link ServiceLocatorIsolation#PER_TEST} will be used
   * instead
   *
   * @param verbose whether this {@link ServiceLocatorTestRule} should
   * output additional information to {@link System#out System.out}
   * 
   * @exception AssertionError if {@code test} is {@code null}
   */
  public ServiceLocatorTestRule(final T test, final ServiceLocatorIsolation isolation, final boolean verbose) {
    super();
    assertNotNull(test);
    this.test = test;
    this.isolation = isolation == null ? ServiceLocatorIsolation.PER_TEST : isolation;
    this.verbose = verbose;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link ServiceLocatorIsolation} for this {@link
   * ServiceLocatorTestRule}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link ServiceLocatorIsolation} for this {@link
   * ServiceLocatorTestRule}; never {@code null}
   */
  public final ServiceLocatorIsolation getServiceLocatorIsolation() {
    return this.isolation;
  }

  /**
   * Returns the {@link Description} describing the currently
   * executing JUnit test method.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link Description} describing the currently
   * executing JUnit test method, or {@code null}
   */
  private final Description getDescription() {
    return this.description;
  }

  /**
   * Returns {@code true} if this {@link ServiceLocatorTestRule}
   * should output additional information to {@link System#out
   * System.out}.
   *
   * @return {@code true} if this {@link ServiceLocatorTestRule}
   * should be verbose; {@code false} otherwise
   *
   * @see ServiceLocatorUtilities#dumpAllDescriptors(ServiceLocator,
   * PrintStream)
   */
  public final boolean isVerbose() {
    return this.verbose;
  }
  
  /**
   * When necessary, calls the {@link
   * #createServiceLocator(Description)}, {@link
   * #configureServiceLocator(ServiceLocator, Description)} and {@link
   * #performDependencyInjection(ServiceLocator, Object)} methods in
   * order to set up a {@link ServiceLocator} appropriate for the
   * current JUnit test.
   *
   * @see #createServiceLocator(Description)
   *
   * @see #configureServiceLocator(ServiceLocator, Description)
   *
   * @exception IOException if the {@link
   * #configureServiceLocator(ServiceLocator, Description)} method
   * threw an {@link IOException}
   * 
   * @see #performDependencyInjection(ServiceLocator, Object)
   */
  @Override
  public void before() throws IOException {
    final Description description = this.getDescription();
    assertNotNull(description);
    if (this.serviceLocator == null) {
      this.serviceLocator = this.createServiceLocator(description);
      assertNotNull(this.serviceLocator);
      this.configureServiceLocator(this.serviceLocator, description);
      this.performDependencyInjection(this.serviceLocator, this.test);
      if (this.isVerbose()) {
        ServiceLocatorUtilities.dumpAllDescriptors(this.serviceLocator, System.out);
      }
    }
  }

  /**
   * Configures the supplied {@link ServiceLocator} for use by the
   * test described by the supplied {@link Description}.
   *
   * <p>This implementation:</p>
   *
   * <ol>
   *
   * <li>Adds the {@link ErrorServiceImpl} class to the supplied
   * {@link ServiceLocator} so that exceptions will be thrown from
   * tests</li>
   *
   * <li>Removes, via {@link
   * DynamicConfiguration#addUnbindFilter(Filter)}, all {@link
   * Descriptor}s that have {@link Description Description.class} as
   * one of their {@linkplain Descriptor#getAdvertisedContracts()
   * contracts}</li>
   *
   * <li>Adds a {@linkplain
   * BuilderHelper#createConstantDescriptor(Object) constant
   * descriptor} in {@link Singleton} scope with a {@linkplain
   * Descriptor#getName() name} equal to the return value of the
   * {@link Description#getDisplayName()} method for the supplied
   * {@link Description} so that the current test can inject the
   * {@link Description} for the current method if it wishes</li>
   *
   * <li>{@linkplain DynamicConfiguration#addActiveDescriptor(Class)
   * Adds any classes} found in an optional {@link Classes} annotation
   * decorating the test class&mdash;if they are assignable to {@link
   * Factory Factory.class} then they are {@linkplain
   * DynamicConfiguration#addActiveFactoryDescriptor(Class) added as
   * factories}</li>
   *
   * <li>Adds any classes found in any packages listed in an optional
   * {@link Packages} annotation decorating the test class, provided
   * they are annotated with {@link Service}</li>
   *
   * <li>Reads any locator files listed in an optional {@link
   * InhabitantFiles} annotation decorating the test class and adds
   * the services listed therein</li>
   *
   * <li>Calls the {@link #bind(DynamicConfiguration)} method</li>
   *
   * <li>Calls the {@link Binder#bind(DynamicConfiguration)} method on
   * the test if it is in fact an instance of {@link Binder}</li>
   *
   * </ol>
   *
   * @param serviceLocator the {@link ServiceLocator} to configure;
   * must not be {@code null}
   *
   * @param testDescription the {@link Description} describing the
   * particular test being run; must not be {@code null}
   *
   * @exception AssertionError if either {@code serviceLocator} or
   * {@code testDescription} is {@code null}
   *
   * @exception IOException if there was an error looking for classes
   * in packages or finding or reading locator files
   */
  protected void configureServiceLocator(final ServiceLocator serviceLocator, final Description testDescription) throws IOException {
    assertNotNull(serviceLocator);
    assertNotNull(testDescription);
    final Class<?> testClass = testDescription.getTestClass();
    if (testClass != null) {

      final DynamicConfigurationService dynamicConfigurationService = serviceLocator.getService(DynamicConfigurationService.class);
      assertNotNull(dynamicConfigurationService);
      final DynamicConfiguration dynamicConfiguration = dynamicConfigurationService.createDynamicConfiguration();
      assertNotNull(dynamicConfiguration);

      dynamicConfiguration.addActiveDescriptor(ErrorServiceImpl.class);
      
      final Filter unbindFilter = BuilderHelper.createContractFilter(Description.class.getName());
      assertNotNull(unbindFilter);
      dynamicConfiguration.addUnbindFilter(unbindFilter);
      
      final AbstractActiveDescriptor<?> descriptionDescriptor = BuilderHelper.createConstantDescriptor(testDescription);
      assertNotNull(descriptionDescriptor);
      descriptionDescriptor.setName(testDescription.getDisplayName());
      descriptionDescriptor.setScope(Singleton.class.getName());
      dynamicConfiguration.addActiveDescriptor(descriptionDescriptor);

      this.bind(dynamicConfiguration, testClass);

      this.bind(dynamicConfiguration);
      
      if (this.test instanceof Binder) {
        ((Binder)this.test).bind(dynamicConfiguration);
      }
      
      dynamicConfiguration.commit();
    }  
  }

  /**
   * Called at the appropriate time by the {@link
   * #configureServiceLocator(ServiceLocator, Description)} method in
   * case subclasses wish to add services to the {@link
   * ServiceLocator} being configured.
   *
   * <p>Overrides of this method must not invoke the {@link
   * DynamicConfiguration#commit()} method.</p>
   *
   * <p>The default implementation of this method does nothing.</p>
   *
   * @param dynamicConfiguration the {@link DynamicConfiguration} to
   * manipulate in order to add or remove services from the {@link
   * ServiceLocator} being configured; must not be {@code null}
   */
  @Override
  public void bind(final DynamicConfiguration dynamicConfiguration) {
    
  }

  /**
   * Given a {@link Set} of package names, returns a {@link Set} of
   * {@link Service}-annotated {@link Class}es found in those
   * packages, whether they are located in {@link ZipFile}s or
   * directories on the classpath.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param packageNames the names of packages to search; may be
   * {@code null} in which case an {@linkplain Collection#isEmpty()
   * empty} {@link Set} will be returned
   *
   * @return a non-{@code null} {@link Set} of {@link Class}es
   *
   * @exception IOException if there was any kind of error during
   * package searching or file reading
   *
   * @see #getClassesFromDirectory(Set, File, boolean)
   *
   * @see #getClassesFromZipFile(Set, ZipFile)
   */
  private final Set<Class<?>> getClassesFromPackages(final Set<String> packageNames) throws IOException {
    final Set<Class<?>> returnValue = new LinkedHashSet<Class<?>>();
    if (packageNames != null && !packageNames.isEmpty()) {
      final String classpath = this.getClasspath();
      if (classpath != null) {
        final StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);
        while (st.hasMoreTokens()) {
          final String classpathEntry = st.nextToken();
          if (classpathEntry != null) {
            final File classpathEntryFile = new File(classpathEntry);
            if (classpathEntryFile.isDirectory()) {
              returnValue.addAll(getClassesFromDirectory(packageNames, classpathEntryFile, this.isVerbose()));
            } else if (classpathEntryFile.exists()) {
              // Zip format
              final ZipFile zipFile = new ZipFile(classpathEntryFile);
              try {
                returnValue.addAll(this.getClassesFromZipFile(packageNames, zipFile));
              } finally {
                try {
                  if (zipFile != null) {
                    zipFile.close();
                  }
                } catch (final IOException ignore) {

                }
              }
            }
          }
        }
      }
    }
    return returnValue;
  }

  /**
   * Given a {@link ZipFile} (which is usually a {@code .jar} file)
   * containing class files and a {@link Set} of package names,
   * searches it to find {@link Service}-annotated {@link Class}es
   * inside it that belong to one of the supplied package names and
   * returns a {@link Set} of such {@link Class}es.
   *
   * @param packageNames a {@link Set} of package names; may be {@code
   * null} in which case an {@linkplain Collection#isEmpty() empty}
   * {@link Set} will be returned
   *
   * @param zipFile a {@link ZipFile} hopefully containing class
   * files; may be {@code null} in which case an {@linkplain
   * Collection#isEmpty() empty} {@link Set} will be returned
   *
   * @return a non-{@code null} {@link Set} of {@link
   * Service}-annotated {@link Class}es
   *
   * @exception IOException if an error occurred reading the {@link
   * ZipFile}
   */
  private final Set<Class<?>> getClassesFromZipFile(final Set<String> packageNames, final ZipFile zipFile) throws IOException {
    final Set<Class<?>> returnValue = new LinkedHashSet<Class<?>>();
    if (packageNames != null && !packageNames.isEmpty() && zipFile != null) {
      for (final String packageName : packageNames) {
        if (packageName != null) {
          final Enumeration<? extends ZipEntry> entries = zipFile.entries();
          if (entries != null && entries.hasMoreElements()) {
            final String packagePath = packageName.replace('.', '/');
            assert packagePath != null;
            while (entries.hasMoreElements()) {
              final ZipEntry entry = entries.nextElement();
              if (entry != null && !entry.isDirectory()) {
                final String entryName = entry.getName();
                if (entryName != null && entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                  final InputStream entryStream = zipFile.getInputStream(entry);
                  try {
                    final Visitor classVisitor = new Visitor(returnValue, this.isVerbose());
                    new ClassReader(entryStream).accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                  } finally {
                    if (entryStream != null) {
                      try {
                        entryStream.close();
                      } catch (final IOException ignore) {

                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return returnValue;
  }

  /**
   * Returns a {@link String} representing the classpath to use to
   * search for classes.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code null}.</p>
   *
   * <p>The default implementation of this method returns the value of
   * the {@code java.class.path} {@linkplain
   * System#getProperty(String, String) system property}, or in the
   * almost certainly catastrophic case where this is not set, the
   * empty string.</p>
   *
   * @return a {@link String} representing the classpath, or {@code
   * null}
   */
  protected String getClasspath() {
    return System.getProperty("java.class.path", "");
  }

  /**
   * {@linkplain AnnotatedElement#getAnnotations() Retrieves any
   * <code>Annotation</code>s found on the supplied
   * <code>AnnotatedElement</code>} and calls the {@link
   * #bind(DynamicConfiguration, Collection)} method supplying them as
   * the second parameter.
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param element the {@link AnnotatedElement} that {@linkplain
   * AnnotatedElement#getAnnotations() has annotations}; may be {@code
   * null} in which case no action is performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading class or
   * locator files
   *
   * @see #bind(DynamicConfiguration, Collection)
   */
  private final void bind(final DynamicConfiguration configuration, final AnnotatedElement element) throws IOException {
    assertNotNull(configuration);
    if (element != null) {
      final Annotation[] annotations = element.getAnnotations();
      if (annotations != null && annotations.length > 0) {
        bind(configuration, Arrays.asList(annotations));
      }
    }
  }

  /**
   * Recursively examines the {@link Annotation}s supplied looking for
   * instances of the {@link Classes}, {@link InhabitantFiles} and
   * {@link Packages} annotations, and, using the binding methods on
   * the supplied {@link DynamicConfiguration} binds {@link
   * Descriptor}s for those "reachable" classes into the
   * configuration.
   *
   * <p><em>Recursively</em> means that a user-defined {@link
   * Annotation} might itself be annotated with {@link Classes}; in
   * such a case the {@link Classes} annotation's contents will be
   * found and processed.  This effectively allows stereotyping the
   * {@link Classes}, {@link InhabitantFiles} and {@link Packages}
   * annotations.</p>
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param annotations a {@link Collection} of {@link Annotation}s;
   * may be {@code null} in which case no action will be performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading class or
   * locator files
   *
   * @see #bind(DynamicConfiguration, Classes)
   *
   * @see #bind(DynamicConfiguration, InhabitantFiles)
   *
   * @see #bind(DynamicConfiguration, Packages)
   */
  private final void bind(final DynamicConfiguration configuration, final Collection<? extends Annotation> annotations) throws IOException {
    assertNotNull(configuration);    
    if (annotations != null && !annotations.isEmpty()) {
      final Deque<Annotation> annotationsToProcess = new ArrayDeque<Annotation>();
      for (final Annotation annotation : annotations) {
        assert annotation != null;
        if (!isBlacklisted(annotation)) {
          annotationsToProcess.addLast(annotation);
        }
      }
      final Set<Annotation> processedAnnotations = new HashSet<Annotation>();
      while (!annotationsToProcess.isEmpty()) {
        final Annotation annotation = annotationsToProcess.removeFirst();
        assert annotation != null;
        if (!isBlacklisted(annotation)) {
          processedAnnotations.add(annotation);
          if (annotation instanceof Classes) {
            bind(configuration, (Classes)annotation);
          } else if (annotation instanceof InhabitantFiles) {
            bind(configuration, (InhabitantFiles)annotation);
          } else if (annotation instanceof Packages) {
            this.bind(configuration, ((Packages)annotation));
          } else {
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            assert annotationType != null;
            final Annotation[] metaAnnotations = annotationType.getAnnotations();
            if (metaAnnotations != null && metaAnnotations.length > 0) {
              for (final Annotation metaAnnotation : metaAnnotations) {
                assert metaAnnotation != null;
                if (!isBlacklisted(metaAnnotation) && processedAnnotations.contains(metaAnnotation)) {
                  annotationsToProcess.addLast(metaAnnotation);
                }
              }
            }
          }
        }
      }
      processedAnnotations.clear();
    }
  }

  /**
   * Binds all {@link Class}es "reachable" from the supplied {@link
   * Classes} annotation.
   *
   * <p>Each {@link Class} found as a member of the return value of
   * the {@link Classes#value()} method is {@linkplain
   * DynamicConfiguration#addActiveDescriptor(Class) bound}
   * ({@linkplain
   * DynamicConfiguration#addActiveFactoryDescriptor(Class) even if}
   * it is an implementation of {@link Factory}) and in turn
   * {@linkplain #bind(DynamicConfiguration, AnnotatedElement) is
   * examined for <code>Annotation</code>s on itself}.</p>
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param classes a {@link Classes}; may be {@code null} in which
   * case no action will be performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading class files
   * or locator files
   *
   * @see DynamicConfiguration#addActiveDescriptor(Class)
   *
   * @see DynamicConfiguration#addActiveFactoryDescriptor(Class)
   */
  private final void bind(final DynamicConfiguration configuration, final Classes classes) throws IOException {
    assertNotNull(configuration);
    if (classes != null) {
      final Class<?>[] classArray = classes.value();
      if (classArray != null && classArray.length > 0) {
        for (final Class<?> c : classArray) {
          if (c != null) {
            if (Factory.class.isAssignableFrom(c)) {
              @SuppressWarnings("unchecked")
              final Class<? extends Factory<Object>> factoryClass = (Class<? extends Factory<Object>>)c;
              configuration.addActiveFactoryDescriptor((Class<Factory<Object>>)factoryClass);
            } else {
              configuration.addActiveDescriptor(c);
            }
            bind(configuration, c);
          }
        }
      }
    }
  }

  /**
   * Binds all {@link Class}es "reachable" from packages described by
   * the supplied {@link Packages} annotation.
   *
   * <p>This method calls the {@link #bind(DynamicConfiguration, Set)}
   * method, passing it a {@link Set} formed from the supplied {@link
   * Packages} annotation's {@linkplain Packages#value() list of
   * package names} as the second parameter.</p>
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param packages a {@link Packages}; may be {@code null} in which
   * case no action will be performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading class files
   * or locator files
   *
   * @see #bind(DynamicConfiguration, Set)
   */
  private final void bind(final DynamicConfiguration configuration, final Packages packages) throws IOException {
    if (packages != null) {
      final String[] packagesArray = packages.value();
      if (packagesArray != null && packagesArray.length > 0) {
        final Set<String> packageNames = new LinkedHashSet<String>();
        for (final String packageName : packagesArray) {
          if (packageName != null) {
            packageNames.add(packageName);
          }
        }
        this.bind(configuration, packageNames);
      }
    }
  }

  /**
   * Binds all {@link Class}es "reachable" from the supplied packages.
   *
   * <p>This method calls the {@link #getClassesFromPackages(Set)}
   * method, and, for each {@link Class} returned, if it is an
   * implementation of {@link Factory} then two {@linkplain
   * DynamicConfiguration#addActiveFactoryDescriptor(Class)
   * <code>Descriptor</code>s are added to the
   * <code>DynamicConfiguration</code> in the form of a
   * <code>FactoryDescriptors</code> addition} by way of the {@link
   * DynamicConfiguration#addActiveFactoryDescriptor(Class)} method,
   * or, if not, then a {@linkplain
   * DynamicConfiguration#addActiveDescriptor(Class) single
   * <code>Descriptor</code> is added} to the {@link
   * DynamicConfiguration}.</p>
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param packageNames a {@link Set} of package names; may be {@code
   * null} in which case no action will be performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading class files
   * or locator files
   *
   * @see #getClassesFromPackages(Set)
   *
   * @see DynamicConfiguration#addActiveDescriptor(Class)
   *
   * @see DynamicConfiguration#addActiveFactoryDescriptor(Class)
   */
  private final void bind(final DynamicConfiguration configuration, final Set<String> packageNames) throws IOException {
    assertNotNull(configuration);
    if (packageNames != null && !packageNames.isEmpty()) {
      final Set<Class<?>> classesFromPackages = this.getClassesFromPackages(packageNames);
      if (classesFromPackages != null && !classesFromPackages.isEmpty()) {
        for (final Class<?> c : classesFromPackages) {
          if (c != null) {
            if (Factory.class.isAssignableFrom(c)) {
              @SuppressWarnings("unchecked")
              final Class<? extends Factory<Object>> factoryClass = (Class<? extends Factory<Object>>)c;
              configuration.addActiveFactoryDescriptor(factoryClass);
            } else {
              configuration.addActiveDescriptor(c);
            }
          }
        }
      }
    }
  }

  /**
   * Performs HK2 dependency injection on this {@link
   * ServiceLocatorTestRule} and the supplied test instance, using the
   * supplied {@link ServiceLocator} as needed.
   *
   * <p>The default implementation of this method does the
   * following:</p>
   *
   * <ol>
   *
   * <li>Calls {@link ServiceLocator#inject(Object)} on the supplied
   * {@link ServiceLocator}, if it is non-{@code null}, passing it
   * this {@link ServiceLocatorTestRule}</li>
   *
   * <li>Calls {@link ServiceLocator#postConstruct(Object)} on the
   * supplied {@link ServiceLocator}, if it is non-{@code null},
   * passing it this {@link ServiceLocatorTestRule}</li>
   *
   * <li>Calls {@link ServiceLocator#inject(Object)} on the supplied
   * {@link ServiceLocator}, if it is non-{@code null}, passing it the
   * supplied {@code test} instance, if it is non-{@code null}</li>
   *
   * <li>Calls {@link ServiceLocator#postConstruct(Object)} on the
   * supplied {@link ServiceLocator}, if it is non-{@code null},
   * passing it the supplied {@code test} instance, if it is
   * non-{@code null}</li>
   *
   * </ol>
   *
   * <p>This method is guaranteed to be called after the {@link
   * #configureServiceLocator(ServiceLocator, Description)} method
   * with the same {@link ServiceLocator}.</p>
   *
   * @param serviceLocator the {@link ServiceLocator} to use to
   * perform injection; may be {@code null} in which case no action
   * will be taken
   *
   * @param test the test instance to inject; may be {@code null}
   *
   * @see #bind(DynamicConfiguration)
   *
   * @see #configureServiceLocator(ServiceLocator, Description)
   */
  protected void performDependencyInjection(final ServiceLocator serviceLocator, final T test) {
    if (serviceLocator != null) {
      this.serviceLocator.inject(this);
      this.serviceLocator.postConstruct(this);
      if (test != null) {
        serviceLocator.inject(test);      
        serviceLocator.postConstruct(test);
      }
    }
  }

  /**
   * Creates and returns a {@link ServiceLocator} suitable for the
   * JUnit test described by the supplied {@link Description}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @param testDescription the {@link Description} describing the
   * currently executing JUnit test; must not be {@code null}
   *
   * @return a non-{@code null} {@link ServiceLocator}
   *
   * @exception AssertionError if {@code testDescription} is {@code
   * null}
   */
  protected ServiceLocator createServiceLocator(final Description testDescription) {
    assertNotNull(testDescription);
    final ServiceLocator returnValue = ServiceLocatorFactory.getInstance().create(this.getServiceLocatorName(testDescription));
    assertNotNull(returnValue);
    return returnValue;
  }

  /**
   * Returns a name for a {@link ServiceLocator} that is appropriate
   * for the supplied {@link Description}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @param testDescription the {@link Description} describing the
   * currently executing JUnit test; must not be {@code null}
   *
   * @return a non-{@code null} {@link String} that will be used as a
   * name for a {@link ServiceLocator}
   *
   * @exception AssertionError if {@code testDescription} is {@code
   * null}, or if its {@link Description#getClassName()
   * getClassName()} method returns {@code null} or if its {@link
   * Description#getMethodName() getMethodName()} method returns
   * {@code null}
   */
  protected String getServiceLocatorName(final Description testDescription) {
    assertNotNull(testDescription);
    final String testClassName = testDescription.getClassName();
    assertNotNull(testClassName);
    final StringBuilder name = new StringBuilder(testClassName);
    final ServiceLocatorIsolation isolation = this.getServiceLocatorIsolation();
    if (isolation == null || isolation == ServiceLocatorIsolation.PER_TEST) {
      final String testMethodName = testDescription.getMethodName();
      assertNotNull(testMethodName);
      name.append(".").append(testMethodName);
    }
    return name.toString();
  }

  /**
   * Overrides the {@link ExternalResource#apply(Statement,
   * Description)} method to save the supplied {@link Description} so
   * that other methods in this class can refer to it.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @param statement the {@link Statement} this {@link
   * ServiceLocatorTestRule} will modify; passed unchanged to the
   * {@link ExternalResource#apply(Statement, Description)} method
   *
   * @param description the {@link Description} describing the current
   * JUnit test; must not be {@code null}; passed unchanged to the
   * {@link ExternalResource#apply(Statement, Description)} method
   *
   * @return the return value of the {@link
   * ExternalResource#apply(Statement, Description)} method; never
   * {@code null}
   */
  @Override
  public Statement apply(final Statement statement, final Description description) {
    this.description = description;
    return super.apply(statement, description);
  }
  
  /**
   * Shuts down and cleans up the {@link ServiceLocator} created by
   * this {@link ServiceLocatorTestRule}.
   *
   * <p>This method performs the following actions in order:</p>
   *
   * <ol>
   *
   * <li>Calls the {@link ServiceLocator#preDestroy(Object)} method on
   * the existing {@link ServiceLocator}, passing it the {@linkplain
   * #ServiceLocatorTestRule(Object) test instance supplied at
   * construction time}</li>
   *
   * <li>Calls the {@link ServiceLocator#preDestroy(Object)} method on
   * the existing {@link ServiceLocator}, passing it this {@link
   * ServiceLocatorTestRule}</li>
   *
   * <li>If the {@linkplain #getServiceLocatorIsolation()
   * <code>ServiceLocator</code> isolation level} is {@link
   * ServiceLocatorIsolation#PER_TEST PER_TEST}, calls the {@link
   * #shutdownAndDestroyServiceLocator(Description)} method with a
   * {@link Description} instance that describes the JUnit test method
   * that just executed</li>
   *
   * </ol>
   *
   * @see #shutdownAndDestroyServiceLocator(Description)
   */
  @Override
  public void after() {
    if (this.serviceLocator != null) {
      if (this.test != null) {
        this.serviceLocator.preDestroy(this.test);
      }
      this.serviceLocator.preDestroy(this);
      final ServiceLocatorIsolation isolation = this.getServiceLocatorIsolation();
      if (isolation == null || this.isolation == ServiceLocatorIsolation.PER_TEST) {
        this.shutdownAndDestroyServiceLocator(this.getDescription());
      }
    }  
    this.description = null;
  }

  /**
   * Blindly calls the {@link ServiceLocator#shutdown()} and {@link
   * ServiceLocatorFactory#destroy(ServiceLocator)} methods on this
   * {@link ServiceLocatorTestRule}'s associated {@link
   * ServiceLocator} if it is discovered to be non-{@code null}.
   *
   * @param testDescription a {@link Description} describing the
   * currently executing JUnit test, if any; ignored by this method's
   * default implementation; may be {@code null}
   */
  public void shutdownAndDestroyServiceLocator(final Description testDescription) {
    if (this.serviceLocator != null) {
      this.serviceLocator.shutdown();
      assertEquals(ServiceLocatorState.SHUTDOWN, this.serviceLocator.getState());
      ServiceLocatorFactory.getInstance().destroy(this.serviceLocator);
    }
  }
  

  /*
   * Static methods.
   */
  

  /**
   * Given a {@link Set} of package names and a {@link File}
   * designating an {@linkplain File#isDirectory() existing directory}
   * in the classpath that {@linkplain File#canRead() can be read},
   * returns a {@link Set} of {@link Service}-annotated {@link
   * Class}es that can be found there that belong to one of the
   * supplied package names.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @param packageNames a {@link Set} of package names; may be {@code
   * null} in which case an {@linkplain Collection#isEmpty() empty}
   * {@link Set} will be returned
   *
   * @param directory a {@link File} designating a directory; an
   * {@linkplain Collection#isEmpty() empty} {@link Set} will be
   * returned unless the directory so designated exists and is
   * readable
   *
   * @param verbose whether additional information should be output
   *
   * @return a non-{@code null} {@link Set} of {@link
   * Service}-annotated {@link Class}es
   *
   * @exception IOException if there was a problem reading files
   */
  private static final Set<Class<?>> getClassesFromDirectory(final Set<String> packageNames, final File directory, final boolean verbose) throws IOException {
    final Set<Class<?>> returnValue = new LinkedHashSet<Class<?>>();
    if (packageNames != null && directory != null && !packageNames.isEmpty() && directory.isDirectory() && directory.canRead()) {
      for (final String packageName : packageNames) {
        if (packageName != null) {
          final File packagePath = new File(directory, packageName.replace('.', '/'));
          if (packagePath.isDirectory() && packagePath.canRead()) {
            final File[] candidates = packagePath.listFiles(new FilenameFilter() {
                @Override
                public final boolean accept(final File directory, final String name) {
                  return name != null && name.endsWith(".class");
                }
              });
            if (candidates != null && candidates.length > 0) {
              for (final File candidate : candidates) {
                if (candidate != null && candidate.isFile() && candidate.canRead()) {
                  final InputStream fileInputStream = new FileInputStream(candidate);
                  try {
                    final Visitor classVisitor = new Visitor(returnValue, verbose);
                    new ClassReader(fileInputStream).accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                  } finally {
                    if (fileInputStream != null) {
                      try {
                        fileInputStream.close();
                      } catch (final IOException ignore) {

                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return returnValue;
  }

  /**
   * Returns {@code true} if the supplied {@link Annotation} is {@code
   * null} or designates an annotation type that should be excluded
   * from recursive annotation scanning performed by the {@link
   * #bind(DynamicConfiguration, AnnotatedElement)} method.
   *
   * @param annotation the {@link Annotation} to consider; may be
   * {@code null} in which case {@code true} will be returned
   *
   * @return {@code true} if the supplied {@link Annotation} is {@code
   * null} or designates an annotation type that should be excluded
   * from recursive annotation scanning performed by the {@link
   * #bind(DynamicConfiguration, AnnotatedElement)} method
   *
   * @see #isBlacklisted(Class)
   */
  private static final boolean isBlacklisted(final Annotation annotation) {
    return annotation == null || isBlacklisted(annotation.annotationType());
  }
  
  /**
   * Returns {@code true} if the supplied {@link Class} is {@code
   * null} or designates an annotation type that should be excluded
   * from recursive annotation scanning performed by the {@link
   * #bind(DynamicConfiguration, AnnotatedElement)} method.
   *
   * @param c the {@link Class} to consider; may be {@code null} in
   * which case {@code true} will be returned
   *
   * @return {@code true} if the supplied {@link Class} is {@code
   * null} or designates an annotation type that should be excluded
   * from recursive annotation scanning performed by the {@link
   * #bind(DynamicConfiguration, AnnotatedElement)} method; {@code
   * false} otherwise
   */
  private static final boolean isBlacklisted(final Class<? extends Annotation> c) {
    final boolean returnValue;
    if (c == null) {
      returnValue = true;
    } else {
      final String className = c.getName();
      returnValue = className.startsWith("java.") || className.startsWith("javax.");
    }
    return returnValue;
  }

  /**
   * Gathers all classpath resource names {@linkplain
   * InhabitantFiles#value() found} in the supplied {@link
   * InhabitantFiles} annotation representing HK2 locator files and
   * calls the {@link #bind(DynamicConfiguration, String)} method for
   * each such entry found.
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param inhabitantFiles an {@link InhabitantFiles}; may be {@code
   * null} in which case no action will be performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading locator
   * files
   *
   * @see #bind(DynamicConfiguration, String)
   */
  private static final void bind(final DynamicConfiguration configuration, final InhabitantFiles inhabitantFiles) throws IOException {
    assertNotNull(configuration);
    if (inhabitantFiles != null) {
      final String[] inhabitantFilesArray = inhabitantFiles.value();
      if (inhabitantFilesArray != null && inhabitantFilesArray.length > 0) {
        for (final String inhabitantFile : inhabitantFilesArray) {
          if (inhabitantFile != null) {
            bind(configuration, inhabitantFile);
          }
        }
      }
    }
  }

  /**
   * Using the {@linkplain Thread#getContextClassLoader() context
   * <code>ClassLoader</code>}, {@linkplain
   * ClassLoader#getResources(String) gets all classpath resources
   * with the supplied <code>locatorResourceName</code>}, and iterates
   * through them, calling the {@link #bind(DynamicConfiguration,
   * URL)} method for each one.
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param locatorResourceName the name of a classpath resource
   * identifying one or more HK2 locator files; may be {@code null} in
   * which case no action will be performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading locator
   * files
   *
   * @see ClassLoader#getResources(String)
   *
   * @see Thread#getContextClassLoader()
   *
   * @see #bind(DynamicConfiguration, URL)
   */
  private static final void bind(final DynamicConfiguration configuration, final String locatorResourceName) throws IOException {
    assertNotNull(configuration);
    if (locatorResourceName != null) {
      final Enumeration<URL> locatorResources = Thread.currentThread().getContextClassLoader().getResources(locatorResourceName);
      if (locatorResources != null) {
        while (locatorResources.hasMoreElements()) {
          final URL locatorResource = locatorResources.nextElement();
          if (locatorResource != null) {
            bind(configuration, locatorResource);
          }
        }
      }
    }
  }

  /**
   * {@linkplain URL#openStream() Opens an <code>InputStream</code>}
   * to the supplied {@link URL}, wraps a {@link BufferedReader}
   * around it, and calls the {@link #bind(DynamicConfiguration,
   * BufferedReader)} method.
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param locatorResource a {@link URL} to an HK2 locator resource;
   * may be {@code null} in which case no action will be taken
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading locator
   * files
   *
   * @see #bind(DynamicConfiguration, BufferedReader)
   */
  private static final void bind(final DynamicConfiguration configuration, final URL locatorResource) throws IOException {
    assertNotNull(configuration);
    if (locatorResource != null) {
      final BufferedReader reader = new BufferedReader(new InputStreamReader(locatorResource.openStream()));
      try {
        bind(configuration, reader);
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (final IOException ignore) {
            
          }
        }
      }
    }
  }

  /**
   * {@linkplain DescriptorImpl#readObject(BufferedReader) Reads} the
   * HK2 locator file represented by the supplied {@link
   * BufferedReader} and {@linkplain
   * DynamicConfiguration#bind(Descriptor) binds the
   * <code>Descriptor</code>}s it represents.
   *
   * @param configuration a {@link DynamicConfiguration} that will
   * perform the binding; must not be {@code null}
   *
   * @param reader a {@link BufferedReader} reading a stream of
   * descriptor information as parseable by the {@link
   * DescriptorImpl#readObject(BufferedReader)} method; may be {@code
   * null} in which case no action will be performed
   *
   * @exception AssertionError if {@code configuration} is {@code
   * null}
   *
   * @exception IOException if there was a problem reading locator
   * files
   *
   * @see DescriptorImpl#readObject(BufferedReader)
   *
   * @see DynamicConfiguration#bind(Descriptor)
   */
  private static final void bind(final DynamicConfiguration configuration, final BufferedReader reader) throws IOException {
    assertNotNull(configuration);
    if (reader != null) {
      DescriptorImpl descriptor = new DescriptorImpl();
      while (descriptor.readObject(reader)) {
        configuration.bind(descriptor);
        descriptor = new DescriptorImpl();
      }
    }
  }

  
  /*
   * Inner and nested classes.
   */
  

  /**
   * An {@code enum} describing possible {@link ServiceLocator}
   * isolation levels for JUnit tests.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @since 2.4.0-b33
   *
   * @see ServiceLocatorTestRule#getServiceLocatorIsolation()
   */
  public enum ServiceLocatorIsolation {

    /**
     * A {@link ServiceLocatorIsolation} indicating that the desired
     * isolation level is per JUnit test method.
     *
     * @see #PER_TEST_CLASS
     */
    PER_TEST,

    /**
     * A {@link ServiceLocatorIsolation} indicating that the desired
     * isolation level is per JUnit test class.
     *
     * @see #PER_TEST
     *
     * @see ClassRule
     */
    PER_TEST_CLASS
  }


  /**
   * A very, very special-purpose {@link ClassVisitorImpl} suitable
   * only for use by the {@link ServiceLocatorTestRule} class to
   * determine efficiently whether a given {@link Class} is annotated
   * with {@link Service} or not.
   *
   * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
   *
   * @since 2.4.0-b33
   *
   * @see ServiceLocatorTestRule
   *
   * @see ClassVisitorImpl
   */
  private static final class Visitor extends ClassVisitorImpl {

    /**
     * The fully qualified class name of the {@link Class} being
     * visited.
     *
     * <p>This field may be {@code null}.</p>
     *
     * <p>This field is set by the {@link #visit(int, int, String, String, String, String[])} method.</p>
     *
     * @see #visit(int, int, String, String, String, String[])
     */
    private String className;

    /**
     * A {@link Set} of {@link Class}es that will be added to by the
     * {@link #visitAnnotation(String, boolean)} method.
     *
     * <p>This field is never {@code null}.</p>
     *
     * @see
     * ServiceLocatorTestRule.Visitor#ServiceLocatorTestRule.Visitor(Set, boolean)
     *
     * @see #visitAnnotation(String, boolean)
     */
    private final Set<Class<?>> classes;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link Visitor}.
     *
     * @param classes A {@link Set} of {@link Class}es that will be
     * added to by the {@link #visitAnnotation(String, boolean)}
     * method; must not be {@code null}; must be mutable
     *
     * @param verbose whether or not additional information should be
     * output
     *
     * @see #visitAnnotation(String, boolean)
     */
    private Visitor(final Set<Class<?>> classes, final boolean verbose) {
      super(null, verbose, Collections.<String>emptySet());
      assertNotNull(classes);
      this.classes = classes;
    }


    /*
     * Instance methods.
     */


    /**
     * Called when a class file is encountered and internally records
     * its fully qualified class name.
     *
     * <p>It is guaranteed that this method will be called before the
     * {@link #visitAnnotation(String, boolean)} method on the current
     * thread.</p>
     *
     * @param version ignored
     *
     * @param access ignored
     *
     * @param name the name of the class, whose package segments are
     * separated with slashes ("{@code /}"); may be {@code null}
     *
     * @param signature ignored
     *
     * @param superName ignored
     *
     * @param interfaces ignored
     *
     * @see #visitAnnotation(String, boolean)
     */
    @Override
    public final void visit(final int version,
                            final int access,
                            final String name,
                            final String signature,
                            final String superName,
                            final String[] interfaces) {
      
      // Guaranteed to be called before visitAnnotation().
      
      if (name == null) {
        this.className = null;
      } else {
        this.className = name.replace('/', '.');
      }
    }

    /**
     * If the supplied {@code annotationClassDescriptor} is equal to
     * "{@code Lorg/jvnet/hk2/annotations/Service;}", and if the
     * supplied {@code visible} parameter is {@code true}, attempts to
     * {@linkplain Class#forName(String, boolean, ClassLoader) load}
     * the {@link Class} encountered in the prior (guaranteed) call to
     * the {@link #visit(int, int, String, String, String, String[])}
     * method, and, if that is successful, adds the resulting {@link
     * Class} to the {@linkplain
     * ServiceLocatorTestRule.Visitor#ServiceLocatorTestRule.Visitor(Set,
     * boolean) <code>Set</code> of <code>Class</code>es that was
     * supplied at construction time}.
     *
     * @param annotationClassDescriptor the descriptor for the
     * annotation being visited; may be {@code null}
     *
     * @param visible whether the annotation is visible or not
     *
     * @return {@code null} when invoked
     *
     * @see
     * ServiceLocatorTestRule.Visitor#ServiceLocatorTestRule.Visitor(Set,
     * boolean)
     */
    @Override
    public final AnnotationVisitor visitAnnotation(final String annotationClassDescriptor, final boolean visible) {
      if (visible && "Lorg/jvnet/hk2/annotations/Service;".equals(annotationClassDescriptor)) {
        try {
          this.classes.add(Class.forName(this.className, true, Thread.currentThread().getContextClassLoader()));
        } catch (final ClassNotFoundException classNotFoundException) {
          classNotFoundException.printStackTrace();
        }
      }
      return null;
    }

    /**
     * Does nothing when invoked.
     */
    @Override
    public final void visitEnd() {

    }
    
  }
  
}
