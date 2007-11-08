/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.deployment.impl.reflection;

import com.sun.forte4j.modules.dbmodel.SchemaElement;
import com.sun.org.apache.jdo.impl.enhancer.ClassFileEnhancer;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerFatalError;
import com.sun.org.apache.jdo.impl.enhancer.EnhancerUserException;
import com.sun.org.apache.jdo.impl.enhancer.jdo.impl.EnhancerFilter;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;
import com.sun.org.apache.jdo.impl.enhancer.meta.model.EnhancerMetaDataModelImpl;
import com.sun.org.apache.jdo.impl.model.jdo.JDOModelFactoryImplDynamic;
import com.sun.org.apache.jdo.impl.model.jdo.caching.JDOModelFactoryImplCaching;
import com.sun.org.apache.jdo.impl.model.jdo.util.PrintSupport;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaModelFactory;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.JDOModelFactory;
import com.sun.persistence.api.deployment.DeploymentException;
import com.sun.persistence.api.deployment.DeploymentUnit;
import com.sun.persistence.api.deployment.XMLWriter;
import com.sun.persistence.api.model.mapping.MappingModel;
import com.sun.persistence.api.model.mapping.MappingModelFactory;
import com.sun.persistence.deployment.impl.JDOModelMapper;
import com.sun.persistence.deployment.impl.LogHelperDeployment;
import com.sun.persistence.deployment.impl.MappingModelMapper;
import com.sun.persistence.spi.deployment.Archive;
import com.sun.persistence.utility.logging.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This is standalone deployment tool that is used both inside and outside
 * container. Outside the container, it is also used at runtime as a
 * bootstrapping mechanism to create various model instances.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class StandaloneDeployer {

    // TODO:
    // 1) Make i18n compliant.

    // outside container, the default schema name is fixed.
    private static final String DEFAULT_SCHEMA_NAME = "default"; // NOI18N

    private final static Logger logger = LogHelperDeployment.getLogger();

    /**
     * This method is used before enhancement to capture full XML DD. This
     * method is responsible for reading annotations, reading XML DD, merging
     * the information, followed by population of default values. The full DD is
     * then saved to destination directory.
     *
     * @param input       is either a Jar file or a directory where classes are
     *                    available. If it is a directory, it muct be the root
     *                    of the package structure as typically passed to a URL
     *                    class loader.
     * @param destination is the destination dir where full DD is written as
     *                    META-INF/persistence-orm.xml. destination must not
     *                    exist prior to this call.
     * @return an in memory representation of what is known as "full" deployment
     *         descriptor in addition to saving the full DD.
     * @throws DeploymentException
     * @throws IOException
     */
    public static DeploymentUnit buildDescriptor(File input, File destination)
            throws DeploymentException, IOException {
        logger.info("Processing " + input);
        Archive archive = ArchiveFactory.getArchive(input);
        DescriptorBuilderImpl builder = new DescriptorBuilderImpl();
        DeploymentUnit du = builder.readXMLAndAnnotations(archive, true);
        File realDestination = new File(destination, "META-INF");
        if (!new File(destination, "META-INF").mkdirs()) {
            throw new IOException(
                    "Failed to create. " + realDestination +
                    " Please make sure " + destination +
                    " folder does not already exist.");
        }
        File outf = new File(realDestination, "persistence-orm.xml");
        logger.info("Writing the full deployment descriptor to " + outf);
        new XMLWriter().write(du.getPersistenceJar(),
                new FileOutputStream(outf));
        return du;
    }

    /**
     * This method combines the effect of calling buildDescriptor and enhance.
     *
     * @param unenhanced is either a Jar file or a directory where unenhanced
     *                   classes are available. If it is a directory, it muct be
     *                   the root of the package structure as typically passed
     *                   to a URL class loader.
     * @param enhanced   is the destination dir where full DD is written as
     *                   META-INF/persistence-orm.xml and the enhanced classes
     *                   will be written. enhanced dir must not exist prior to
     *                   this call.
     * @throws Exception
     */
    public static void buildDescriptorAndEnhance(
            File unenhanced, File enhanced)
            throws Exception {
        // capture the full DD and write it out to enhanced dir.
        DeploymentUnit du = buildDescriptor(unenhanced, enhanced);
        new EnhancerDriver(du, unenhanced, enhanced).enhance();
    }

    /**
     * This method is used at runtime to create the mapping model. It expects
     * the enhanced classes, the META-INF/persistence-orm.xml as well as the
     * dbschema to be loadable by supplied class loader.
     *
     * @param cl a class loader. if null is passed, we use {@link
     *           ClassLoader#getSystemClassLoader()} .
     * @return a fully populated JDOModel and a MappingModel. It also registers
     *         mappingmodel with the supplied MappingModelFactory.
     * @throws DeploymentException
     * @throws IOException
     */
    public static MappingModel mapMappingModel(
            ClassLoader cl,
            JavaModelFactory javaModelFactory,
            JDOModelFactory jdoModelFactory,
            MappingModelFactory mappingModelFactory) throws IOException,
            DeploymentException {
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        Archive archive = ArchiveFactory.getArchive(cl);
        DescriptorBuilderImpl builder = new DescriptorBuilderImpl();
        DeploymentUnit du = builder.readXML(archive);

        // Note we call getJavaModel as opposed to createJavaModel
        JavaModel javaModel = javaModelFactory.getJavaModel(cl);
        JDOModel jdoModel = jdoModelFactory.getJDOModel(javaModel, false);
        logger.info("JDOModel conversion");
        JDOModelMapper.map(du, jdoModel);
        String schemaName = DEFAULT_SCHEMA_NAME;
        logger.info("loading dbschema " + schemaName);
        SchemaElement se = SchemaElement.forName(schemaName, cl);
        if (se == null) {
            throw new DeploymentException("Please make " + schemaName +
                    ".dbschema available in your classpath");
        }
        // Note we call getMappingModel as opposed to createMappingModel
        MappingModel mappingModel = mappingModelFactory.getMappingModel(
                jdoModel, null);
        logger.info("mapping model conversion");
        MappingModelMapper.map(du, se, mappingModel);
        logger.info("Succesfully bootstrapped.");
        return mappingModel;
    }

    // helper method used to avoid build dependency on runtime project
    private static MappingModelFactory getRuntimeMappingModelFactory()
            throws Exception {
        String className =
                "com.sun.persistence.runtime.model.mapping.impl.RuntimeMappingModelFactoryImpl";
        Class clazz = Class.forName(className);
        Method method = clazz.getMethod("getInstance", new Class[]{});
        Object o = method.invoke(clazz);
        return MappingModelFactory.class.cast(o);
    }

    private static JavaModelFactory getRuntimeJavaModelFactory()
            throws Exception {
        String className =
                "com.sun.org.apache.jdo.impl.model.java.runtime.RuntimeJavaModelFactory";
        Class clazz = Class.forName(className);
        Method method = clazz.getMethod("getInstance", new Class[]{});
        Object o = method.invoke(clazz);
        return JavaModelFactory.class.cast(o);
    }

    private static class ArchiveFactory {
        static Archive getArchive(ClassLoader cl) {
            if (cl == null) {
                throw new NullPointerException("Error: class loader is null");
            }
            return new ClassLoaderArchive(cl);
        }

        static Archive getArchive(File f) throws IOException {
            if (!f.exists()) {
                throw new IOException("Error: " + f + " does not exist");
            }
            if (f.isDirectory()) return new DirectoryArchive(f);
            if (f.isFile()) return new JarFileArchive(new JarFile(f));
            throw new RuntimeException("Unrecognized input " + f);
        }
    }

    // Implementation of Archive interface for jar file
    private static class JarFileArchive implements Archive {
        JarFile jarFile;

        JarFileArchive(JarFile jarFile) {
            this.jarFile = jarFile;
        }

        public Enumeration<String> getEntries() {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            ArrayList<String> result = new ArrayList<String>();
            while (jarEntries.hasMoreElements()) {
                result.add(jarEntries.nextElement().getName());
            }
            return Collections.enumeration(result);
        }

        public InputStream getEntry(String entryPath) throws IOException {
            InputStream is = null;
            ZipEntry je = jarFile.getEntry(entryPath);
            if (je != null) {
                is = jarFile.getInputStream(jarFile.getEntry(entryPath));
            }
            return is;
        }

        public ClassLoader getClassLoader() {
            try {
                URL url = new File(jarFile.getName()).toURL();
                return new URLClassLoader(new URL[]{url});
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Implementation of Archive interface for directory
    private static class DirectoryArchive implements Archive {
        File directory;

        List<String> entries = new ArrayList<String>();

        DirectoryArchive(File directory) {
            assert(directory.isDirectory());
            this.directory = directory;
            init(directory, directory);
        }

        // The following piece of logic is copied from DirectoryScanner
        private void init(File top, File directory) {
            File[] dirFiles = directory.listFiles();
            for (File file : dirFiles) {
                entries.add(file.getPath().replace(File.separator, "/")
                        .substring(top.getPath().length() + 1));
                // entries.add(file.getPath().substring(top.getPath().length() + 1));
            }
            File[] subDirs = directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            for (File subDir : subDirs) {
                init(top, subDir);
            }
        }

        public Enumeration<String> getEntries() {
            return Collections.enumeration(entries);
        }

        public InputStream getEntry(String entryPath) throws IOException {
            File f = new File(directory, entryPath);
            if (f.exists()) {
                return new FileInputStream(f);
            } else {
                return null;
            }
        }

        public ClassLoader getClassLoader() {
            try {
                URL url = directory.toURL();
                return new URLClassLoader(new URL[]{url});
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Implementation of Archive interface using a ClassLoader
    private static class ClassLoaderArchive implements Archive {
        ClassLoader cl;

        ClassLoaderArchive(ClassLoader cl) {
            this.cl = cl;
        }

        public Enumeration<String> getEntries() throws IOException {
            // TODO: use Collections.emptyList().
            return Collections.enumeration(new ArrayList<String>());
        }

        public InputStream getEntry(String entryPath) throws IOException {
            return cl.getResourceAsStream(entryPath);
        }

        public ClassLoader getClassLoader() {
            return cl;
        }

    }

    /**
     * This class is the driver for the actual enhancer. It sets up JDOModel,
     * JavaModel etc. and then delegates to an actual ClassFileEnhancer to do
     * the work.
     */
    public static class EnhancerDriver {
        /* The actual enhancer. */
        private ClassFileEnhancer enhancer;

        /* directory where enhanced classes will be written out */
        private File enhanced;

        /* The JDOModel which provides metadata to enhnacer to work on.
         * associated with this JDOModel is a JavaModel which is used
         * to load unenhanced class files as resources. */
        private JDOModel jdoModel;

        public EnhancerDriver(
                DeploymentUnit du, File unenhanced, File enhanced)
                throws Exception, EnhancerUserException, IOException,
                DeploymentException {
            this.enhanced = enhanced;
            logger.info("Unenhanced location : " + unenhanced);
            logger.info("Enhanced location : " + enhanced);
            if (!enhanced.isDirectory() || !enhanced.exists()) {
                throw new IOException(enhanced + " does not exist.");
            }

            // create a JavaModel pointing to unenhanced location
            ClassLoader cl =
                    ArchiveFactory.getArchive(unenhanced).getClassLoader();
            final JavaModel javaModel
                    = getRuntimeJavaModelFactory().getJavaModel(cl);
            // create an empty JDOModel passing this JavaModel
            jdoModel = JDOModelFactoryImplDynamic.getInstance().
                    getJDOModel(javaModel, false);
            logger.info("JDOModel conversion");
            JDOModelMapper.map(du, jdoModel);
            logger.info("Succesfully populated a JDOModel");
            if (logger.isLoggable(Logger.FINE)) {
                PrintSupport.printJDOModel(jdoModel);
            }
            // Not needed since JDOModel takes care of this.
//            setOIdClasses();
            createEnhancer();
        }

        public void enhance() throws Exception {
            JavaModel javaModel = jdoModel.getJavaModel();
            for (JDOClass jdoClass : jdoModel.getDeclaredClasses()) {
                final String cn = jdoClass.getName();
                logger.info("Enhancing class: " + cn);
                final String fn = (cn.replace('.', '/') + ".class");
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new BufferedInputStream(
                            javaModel.getInputStreamForResource(fn));
                    final File of = new File(enhanced, fn);
                    final File dir = of.getAbsoluteFile().getParentFile();
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IOException("Error creating directory: "
                                + dir.getAbsolutePath());
                    }
                    os = new BufferedOutputStream(new FileOutputStream(of));
                    final boolean changed = enhancer.enhanceClassFile(is, os);
                    logger.info("Succesfully "
                            + (changed ? "enhanced" : "copied")
                            + " enhanced classfile: " + fn);
                } catch (Exception ex) {
                    // catch (IOException ex) {
                    // catch (EnhancerUserException ex) {
                    // catch (EnhancerFatalError ex) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }
                    logger.severe("Error enhancing file " + fn + " : " + ex);
                    throw ex;
                }
            }
            logger.info("Succesfully enhanced classfiles");
        }

        private void createEnhancer()
                throws EnhancerUserException, EnhancerFatalError {
            try {
                final PrintWriter out = new PrintWriter(System.out, true);
                final boolean verbose = logger.isLoggable(Logger.FINE);
                final EnhancerMetaData jdoMeta
                        = new EnhancerMetaDataModelImpl(out, verbose,
                                jdoModel, jdoModel.getJavaModel());
                final Properties props = new Properties();
                if(verbose) {
                    props.put(EnhancerFilter.VERBOSE_LEVEL,
                        EnhancerFilter.VERBOSE_LEVEL_VERBOSE);
                }
                enhancer = new EnhancerFilter(jdoMeta, props, out, out);
                logger.info("Succesfully created byte-code enhancer");
            } catch (EnhancerMetaDataUserException ex) {
                logger.severe("Error creating enhancer metadata: " + ex);
                throw ex;
            } catch (EnhancerMetaDataFatalError ex) {
                logger.severe("Error creating enhancer metadata: " + ex);
                throw ex;
            } catch (EnhancerUserException ex) {
                logger.severe("Error creating enhancer: " + ex);
                throw ex;
            } catch (EnhancerFatalError ex) {
                logger.severe("Error creating enhancer" + ex);
                throw ex;
            }
        }

    } // end of class EnhancerDriver

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            logger.severe(
                    "Usage: java " + StandaloneDeployer.class.getName() +
                    " <deploy|fullDD|enhance|bootstrap>");
            System.exit(1);
        }
        if (args[0].equalsIgnoreCase("fullDD")) {
            logger.info("Command = fullDD");
            if (args.length != 3) {
                logger.severe(
                        "Usage : java " + StandaloneDeployer.class.getName() +
                        " <deploy>" +
                        " <jar file or a directory containing unenhanced classes>" +
                        " <destination directory where full DD will be written>");
                System.exit(1);
            }
            buildDescriptor(new File(args[1]), new File(args[2]));
        }
        if (args[0].equalsIgnoreCase("deploy")) {
            logger.info("Command = deploy");
            if (args.length != 3) {
                logger.severe(
                        "Usage : java " + StandaloneDeployer.class.getName() +
                        " <enhance>" +
                        " <jar file or a directory containing unenhanced classes>" +
                        " <directory where enhanced classes will be copied to. \n" +
                        "  \t\tThe fullDD will also be written to this dir.>");
                System.exit(1);
            }
            buildDescriptorAndEnhance(new File(args[1]), new File(args[2]));
        }
        if (args[0].equalsIgnoreCase("enhance")) {
            // mostly used for testing, because we want people to use deploy.
            logger.info("Command = enhance");
            if (args.length != 3) {
                logger.severe(
                        "Usage : java " + StandaloneDeployer.class.getName() +
                        " <enhance>" +
                        " <jar file or a directory containing unenhanced classes>" +
                        " <directory where enhanced classes will be copied to. \n" +
                        "  \t\tThis directory should also have META-INF/persistence-orm.xml file>");
                System.exit(1);
            }
            // See we read the full DD from args[2]
            DeploymentUnit du = new DescriptorBuilderImpl().
                    readXML(ArchiveFactory.getArchive(new File(args[2])));
            logger.info("Read the full DD from " + args[1]);
            new EnhancerDriver(du, new File(args[1]), new File(args[2])).enhance();
        }
        if (args[0].equalsIgnoreCase("bootstrap")) {
            // only used for testing.
            // in reality this takes place in runtime code or in app loader
            logger.info("Command = bootstrap");
            if (args.length != 1) {
                logger.severe("Usage : java " +
                        StandaloneDeployer.class.getName() +
                        " <botstrap> ");
                System.exit(1);
            }
            logger.info("System classpath = " +
                    System.getProperty("java.class.path"));
            JavaModelFactory javaModelFactory = getRuntimeJavaModelFactory();
            JDOModelFactory jdoModelFactory =
                    JDOModelFactoryImplCaching.getInstance();
            MappingModelFactory mappingModelFactory =
                    getRuntimeMappingModelFactory();
            MappingModel mappingModel = StandaloneDeployer.mapMappingModel(
                    null, javaModelFactory, jdoModelFactory,
                    mappingModelFactory);
            assert(mappingModel.getJDOModel() != null);
            try {
                PrintSupport.printJDOModel(mappingModel.getJDOModel());
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                logger.info("\n *********************************\n" +
                        "PrintSupport tries to load some classes while printing a JDOModel." +
                        "So you will get this exception, when there are no " +
                        "entity classes in the class path.\n" +
                        "*****************************");
            }
        }
    } // main
}
