package org.glassfish.javaee.core.deployment;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.deployment.ApplicationMetaDataProvider;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.DeploymentProperties;
import org.xml.sax.SAXParseException;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.util.ApplicationVisitor;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.deploy.shared.DeploymentPlanArchive;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.DescriptorArchivist;
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
    protected DescriptorArchivist descriptorArchivist;

    @Inject
    protected ApplicationArchivist applicationArchivist;

    @Inject
    Habitat habitat;

    private static String WRITEOUT_XML = System.getProperty(
        "writeout.xml");

    public MetaData getMetaData() {
        return new MetaData(false, new Class[] { Application.class, WebBundleDescriptor.class }, null);
    }

    public Application load(DeploymentContext dc) throws IOException {

        ReadableArchive sourceArchive = dc.getSource();
        ClassLoader cl = dc.getClassLoader();
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);

        String name = params.name();

        Archivist archivist = archivistFactory.getArchivist(
                sourceArchive, cl);
        archivist.setAnnotationProcessingRequested(true);
        archivist.setXMLValidation(false);
        archivist.setRuntimeXMLValidation(false);

        File deploymentPlan = params.deploymentplan;
        handleDeploymentPlan(deploymentPlan, archivist, sourceArchive);

        long start = System.currentTimeMillis();
        EarHandler.ApplicationHolder holder = dc.getModuleMetaData(EarHandler.ApplicationHolder.class);
        Application application=null;
        if (holder!=null) {
            // this is the ear case

            application = holder.app;

            // finish the job
            ApplicationArchivist appArchivist = 
                ApplicationArchivist.class.cast(archivist);
            appArchivist.setClassLoader(cl);

            appArchivist.setManifest(sourceArchive.getManifest());
            try {
                appArchivist.openWith(application, sourceArchive);
            } catch(SAXParseException e) {
                throw new IOException(e);
            }
            application.setRegistrationName(name);
        }
        if (application==null) {
            try {
                application = applicationFactory.openArchive(
                        name, archivist, sourceArchive, true);
            } catch(SAXParseException e) {
                throw new IOException(e);
            }
        }

        validateApplication(application, dc);

        // this may not be the best location for this but it will suffice.
        if (deploymentVisitor!=null) {
            deploymentVisitor.accept(application);
        }

        // write out xml files if needed
        if (Boolean.valueOf(WRITEOUT_XML)) {
            saveAppDescriptor(application, dc);
        }

        System.out.println("DOL Loading time" + (System.currentTimeMillis() - start));

        if (application.isVirtual()) {
            dc.addModuleMetaData(application.getStandaloneBundleDescriptor());
        }

        return application;

    }

    protected void handleDeploymentPlan(File deploymentPlan,
        Archivist archivist, ReadableArchive sourceArchive) throws IOException {
        //Note in copying of deployment plan to the portable archive,
        //we should make sure the manifest in the deployment plan jar
        //file does not overwrite the one in the original archive
        if (deploymentPlan != null) {
            DeploymentPlanArchive dpa = new DeploymentPlanArchive();
            dpa.open(deploymentPlan.toURI());
            // need to revisit for ear case
            WritableArchive targetArchive = archiveFactory.createArchive(
                sourceArchive.getURI());
            archivist.copyInto(dpa, targetArchive, false);
        }
    }    

    protected void saveAppDescriptor(Application application, 
        DeploymentContext context) throws IOException {
        if (application != null) {
            ReadableArchive archive = archiveFactory.openArchive(
                context.getSourceDir());
            context.getScratchDir("xml").mkdirs();
            WritableArchive archive2 = archiveFactory.createArchive(
                context.getScratchDir("xml"));
            descriptorArchivist.write(application, archive, archive2);

            // copy the additional webservice elements etc
            applicationArchivist.copyExtraElements(archive, archive2);
        }
    }

    protected void validateApplication(Application app, DeploymentContext dc) {
        if (app != null) {
            app.setClassLoader(dc.getClassLoader());
            app.visit((ApplicationVisitor) new ApplicationValidator());
        }
    }

}
