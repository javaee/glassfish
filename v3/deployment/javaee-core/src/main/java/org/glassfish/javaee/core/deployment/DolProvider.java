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
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.xml.sax.SAXParseException;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.deploy.shared.DeploymentPlanArchive;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.DescriptorArchivist;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.config.serverbeans.DasConfig;

import java.util.Properties;
import java.util.Collection;
import java.io.IOException;
import java.io.File;

/**
 * ApplicationMetada
 */
@Service
public class DolProvider implements ApplicationMetaDataProvider<Application> {

    @Inject
    ArchivistFactory archivistFactory;

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

    @Inject
    DasConfig dasConfig;

    @Inject
    public ApplicationRegistry appRegistry;

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
        String xmlValidationLevel = dasConfig.getDeployXmlValidation();
        archivist.setXMLValidationLevel(xmlValidationLevel);
        if (xmlValidationLevel.equals("none")) {
            archivist.setXMLValidation(false);
        }
        archivist.setRuntimeXMLValidation(false);

        File deploymentPlan = params.deploymentplan;
        handleDeploymentPlan(deploymentPlan, archivist, sourceArchive);

        long start = System.currentTimeMillis();
        ApplicationHolder holder = dc.getModuleMetaData(ApplicationHolder.class);
        Application application=null;
        if (holder!=null) {
            // this is the ear case

            application = holder.app;

            // finish the job
            ApplicationArchivist appArchivist = 
                ApplicationArchivist.class.cast(archivist);
            appArchivist.setClassLoader(cl);

            appArchivist.setManifest(sourceArchive.getManifest());

            setAppName(application, dc, name);
            
            try {
                appArchivist.openWith(application, sourceArchive);
            } catch(SAXParseException e) {
                throw new IOException(e);
            }
        }
        if (application==null) {
            try {
                application = applicationFactory.openArchive(
                        name, archivist, sourceArchive, true);
                setAppName(application, dc, name);
            } catch(SAXParseException e) {
                throw new IOException(e);
            }
        }

        resolveAppNameConflict(application);

        application.setRegistrationName(name);

        // write out xml files if needed
        if (Boolean.valueOf(WRITEOUT_XML)) {
            saveAppDescriptor(application, dc);
        }

        System.out.println("DOL Loading time" + (System.currentTimeMillis() - start));

        if (application.isVirtual()) {
            dc.addModuleMetaData(application.getStandaloneBundleDescriptor());
            for (RootDeploymentDescriptor extension : application.getStandaloneBundleDescriptor().getExtensionsDescriptors()) {
                dc.addModuleMetaData(extension);
            }
        }

        return application;

    }

    private void setAppName(Application application, DeploymentContext dc, String name) {
        // for standalone module, set the application name as the module name
        if (application.isVirtual()) {
            ModuleDescriptor md = application.getStandaloneBundleDescriptor(
                ).getModuleDescriptor();
            application.setAppName(md.getModuleName());
            return;
        }

        // if the app-name is not defined in the 
        // application.xml/sun-application.xml
        // use the default name
        if (application.getAppName() == null) {
            // This is needed as for the scenario where the user specifies
            // --name option explicitly, the EE6 app name might be different
            // from the application's registration name and we need a way
            // to retrieve the EE6 app name for server restart code path
            String defaultEE6AppName =
                dc.getAppProps().getProperty("default-EE6-app-name");
            if (defaultEE6AppName != null) {
                application.setAppName(defaultEE6AppName);
            }  else {
                application.setAppName(name);
            }
        }
    }

    // find if the application name is already in use, if yes
    // assign another name
    private void resolveAppNameConflict(Application application) {
        String appName = application.getAppName();
        Collection<ApplicationInfo> allApplications = 
            appRegistry.getAllApplicationInfos();    
        boolean needResolveConflict = true;
        int appendix = 1;
        while (needResolveConflict) {
            needResolveConflict = false;
            for (ApplicationInfo appInfo : allApplications) {
                Application app = appInfo.getMetaData(Application.class);
                if (appName.equals(app.getAppName())) {
                    // found a conflict 
                    needResolveConflict = true;
                    break;
                }
            }
            if (needResolveConflict) {
                appName = appName + "_" + String.valueOf(appendix); 
                appendix++;
                // once we assign a different name, we need re-check
                // to see if this new cause any conflict
                needResolveConflict = true;
            }
        }
        application.setAppName(appName);
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
}
