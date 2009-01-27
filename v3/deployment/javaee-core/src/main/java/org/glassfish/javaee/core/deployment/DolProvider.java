package org.glassfish.javaee.core.deployment;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.deployment.ApplicationMetaDataProvider;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.DeploymentProperties;
import org.xml.sax.SAXParseException;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.util.ApplicationVisitor;
import com.sun.enterprise.deployment.deploy.shared.DeploymentPlanArchive;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deploy.shared.ArchiveFactory;

import java.util.Properties;
import java.io.IOException;
import java.io.File;

/**
 * ApplicationMetada
 */
@Service
public class DolProvider implements ApplicationMetaDataProvider<Application> {

    @Inject
    ArchivistFactory archivistFactory;

    @Inject(name="application_deploy", optional=true)
    protected ApplicationVisitor deploymentVisitor=null;

    @Inject
    protected ApplicationFactory applicationFactory;

    @Inject
    protected ArchiveFactory archiveFactory;

    @Inject
    Habitat habitat;

    public MetaData getMetaData() {
        return new MetaData(false, new Class[] { Application.class, WebBundleDescriptor.class }, null);
    }

    public Application load(DeploymentContext dc) throws IOException {

        ReadableArchive sourceArchive = dc.getSource();
        ClassLoader cl = dc.getClassLoader();
        Properties props = dc.getCommandParameters();
        String name = props.getProperty(DeploymentProperties.NAME);

        Archivist archivist = archivistFactory.getArchivist(
                sourceArchive, cl);
        archivist.setAnnotationProcessingRequested(true);
        archivist.setXMLValidation(false);
        archivist.setRuntimeXMLValidation(false);

        // we only expand deployment plan once in the first deployer
        if (dc.getModuleMetaData(Application.class) == null) {
            String deploymentPlan = props.getProperty(
                DeploymentProperties.DEPLOYMENT_PLAN);
            handleDeploymentPlan(deploymentPlan, archivist, sourceArchive);
        }
        long start = System.currentTimeMillis();
        EarHandler.ApplicationHolder holder = dc.getModuleMetaData(EarHandler.ApplicationHolder.class);
        Application application=null;
        if (holder!=null) {
            application = holder.app;

            // finish the job.
            application.setRegistrationName(name);
            archivist.setDescriptor(application);
            archivist.validate(cl);
/*            ApplicationArchivist appArchivist = habitat.getComponent(ApplicationArchivist.class);
            try {
                application = appArchivist.openWith(application, sourceArchive);
            } catch (SAXParseException e) {
                application=null;
            }
            */
        }
        if (application==null) {
            try {
                application = applicationFactory.openArchive(
                        name, archivist, sourceArchive, true);
            } catch(SAXParseException e) {
                throw new IOException(e);
            }
        }

        // this may not be the best location for this but it will suffice.
        if (deploymentVisitor!=null) {
            deploymentVisitor.accept(application);
        }
        System.out.println("DOL Loading time" + (System.currentTimeMillis() - start));

        if (application.isVirtual()) {
            dc.addModuleMetaData(application.getStandaloneBundleDescriptor());
        }

        return application;

    }
    protected void handleDeploymentPlan(String deploymentPlan,
        Archivist archivist, ReadableArchive sourceArchive) throws IOException {
        //Note in copying of deployment plan to the portable archive,
        //we should make sure the manifest in the deployment plan jar
        //file does not overwrite the one in the original archive
        if (deploymentPlan != null) {
            DeploymentPlanArchive dpa = new DeploymentPlanArchive();
            dpa.open(new File(deploymentPlan).toURI());
            // need to revisit for ear case
            WritableArchive targetArchive = archiveFactory.createArchive(
                sourceArchive.getURI());
            archivist.copyInto(dpa, targetArchive, false);
        }
    }    
}
