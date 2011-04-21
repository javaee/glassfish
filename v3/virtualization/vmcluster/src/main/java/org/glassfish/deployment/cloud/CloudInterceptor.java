package org.glassfish.deployment.cloud;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.module.bootstrap.Populator;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.admin.DeployCommand;
import org.glassfish.vmcluster.util.RuntimeContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 3/2/11
 * Time: 3:18 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
@Scoped(PerLookup.class)
public class CloudInterceptor implements DeployCommand.Interceptor {

    @Inject
    Habitat habitat;

    @Inject
    RuntimeContext rtContext;

    @Inject(name="plain")
    ActionReport actionReport;


    @Override
    public void intercept(DeployCommand command, DeploymentContext context) {
        System.out.println("Interceptor called for ..." + command.name());
        boolean clusterCreated = false;
        try {
            if (context.getSource().exists("META-INF/cloud.xml")) {
                System.out.println("This is a virtual application !");
                CloudApplication cloudApplication = readConfig(context.getSource());
                for (CloudService cloudService : cloudApplication.getServices().getServices()) {
                    if (cloudService instanceof JavaEEService) {
                        JavaEEService javaEE = (JavaEEService) cloudService;
                        System.out.println("Applications wants " + javaEE.getMinInstances() + " Java EE instances");
                        rtContext.executeAdminCommand(actionReport, "create-virtual-cluster", command.name(), "min", javaEE.getMinInstances());
                        clusterCreated = !actionReport.hasFailures();
                        command.target = command.name();
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
            if (clusterCreated) {
                rtContext.executeAdminCommand(actionReport, "delete-virtual-cluster", command.name());
            }
        }
    }

    private CloudApplication readConfig(ReadableArchive archive) throws IOException {
        InputStream is=null;

        try {
            is = archive.getEntry("META-INF/cloud.xml");
            if (is==null) return null;

            final XMLStreamReader reader;
            try {
                reader = XMLInputFactory.newFactory().createXMLStreamReader(is);
            } catch (XMLStreamException e) {
                e.printStackTrace();
                return null;
            }

            final ConfigParser configParser = new ConfigParser(habitat);
            DomDocument document =  configParser.parse(reader);
            return document.getRoot().createProxy(CloudApplication.class);

       } catch(Exception e) {
            if (is!=null) {
                try { is.close(); } catch(Exception ex) { // ignore
                }
                throw new IOException(e);
            }
       }
       return null;
    }
}
