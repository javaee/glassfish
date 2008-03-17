package org.glassfish.persistence.jpa;

import com.sun.appserv.connectors.spi.ConnectorRuntime;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.module.ModuleDefinition;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.deployment.common.DummyApplication;
import org.glassfish.deployment.common.SimpleDeployer;
import org.glassfish.deployment.common.DeploymentException;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.ClassTransformer;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.Set;


@Service
public class JPADeployer extends SimpleDeployer<JPAContainer, DummyApplication> {

    @Inject
    ConnectorRuntime connectorRuntime;

    @Override public MetaData getMetaData() {
        //Inherit PublicAPIs from JavaEEDeployer 
        MetaData javaEEDeployerMetaData = super.getMetaData();
        ModuleDefinition[] publicAPIsForJavaEE = javaEEDeployerMetaData.getPublicAPIs();
        return new MetaData(true /*invalidateCL */ , publicAPIsForJavaEE, null /* provides */,
                new Class[] {Application.class} /* requires Application from dol */);
    }

    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {
        // Noting to generate yet!!
    }

    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {
        // Noting to cleanup yet!!
    }

    protected RootDeploymentDescriptor getDefaultBundleDescriptor() {
        return null;
    }

    protected String getModuleType() {
        //TODO check with Jerome who consumes this
        return "JPA-MODULE";
    }

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    @Override public boolean prepare(DeploymentContext dc) {
        boolean prepared = super.prepare(dc);
        if (prepared) {
            Application application = dc.getModuleMetaData(Application.class);
            Set<BundleDescriptor> bundles = application.getBundleDescriptors();
            //TODO Need to modify this to be more generic
            for (BundleDescriptor bundle : bundles) {
                PersistenceUnitLoader.ApplicationInfo applicationInfo = new ApplicationInfoImpl(bundle, dc, connectorRuntime);
                new PersistenceUnitLoaderImpl(applicationInfo).load();
            }
        }
        return true;
    }

    public DummyApplication load(JPAContainer container, DeploymentContext context) {
        return new DummyApplication(); 
    }

    public void unload(DummyApplication appContainer, DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private static class ApplicationInfoImpl
            implements PersistenceUnitLoader.ApplicationInfo {
        private BundleDescriptor bd;
        private DeploymentContext deploymentContext;
        private ConnectorRuntime connectorRuntime;
        
        public ApplicationInfoImpl(BundleDescriptor bd, DeploymentContext deploymentContext, ConnectorRuntime connectorRuntime) {
            this.bd = bd;
            this.deploymentContext = deploymentContext;
            this.connectorRuntime = connectorRuntime;
        }

        public ClassLoader getClassLoader() {
            return deploymentContext.getClassLoader();
        }

        public ClassLoader getTempClassloader() {
            // Note we return the same classloader as returned by getClassLoader.
            // This classloader will get discarded once the app is prepared and a new classlaoder will be used to
            // actually load the application
            // TODO : Discuss with Jerome this might not work as EclipseLink loads Listners, NamedQuery etc with this classloader
            // We also use this classloader to load the provider for example is PULImpl#load() to make sure that we see
            // providers bundled as lib with the app. If the classloader changes, things might get ugly in unpredictable way.
            return deploymentContext.getClassLoader();
        }

        public void addTransformer(ClassTransformer transformer) {
            // TODO: Resolve the differenct between java.lang.instrument.ClassFileTransformer that this method accepts
            // and javax.persistence.spi.ClassTransformer that JPA expects
            //deploymentContext.addClassFileTransformer(transformer);
        }

        public String getApplicationLocation() {
            //TODO : This needs to be cleaned up post TP2 to be more generic than dc.getSourceDir().
            return deploymentContext.getSourceDir().getAbsolutePath();
        }

        /**
         * @return the precise collection of PUs that are referenced by this war
         */
        public Collection<? extends PersistenceUnitDescriptor>
                getReferencedPUs() {
            return bd.findReferencedPUs();
        }

        /**
         * @return the list of EMFs that have been loaded for this war.
         */
        public Collection<? extends EntityManagerFactory> getEntityManagerFactories() {
//            // since we are only responsible for standalone web module,
//            // there is no need to search for EMFs in Application object.
//            assert(bd.getApplication().isVirtual());
            return bd.getEntityManagerFactories();
        }

        public DataSource lookupDataSource(String dataSourceName) throws NamingException {
            return DataSource.class.cast(connectorRuntime.lookupPMResource(dataSourceName, false) );
        }

        public DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException {
            return DataSource.class.cast(connectorRuntime.lookupNonTxResource(dataSourceName, false));
        }

    } // class ApplicationInfoImpl

}