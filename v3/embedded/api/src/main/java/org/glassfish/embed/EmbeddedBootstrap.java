
package org.glassfish.embed;

import com.sun.enterprise.security.SecuritySniffer;
import com.sun.enterprise.v3.admin.CommandRunnerImpl;
import com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter;
import com.sun.enterprise.v3.server.APIClassLoaderServiceImpl;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import org.glassfish.config.support.DomainXml;
import com.sun.enterprise.v3.server.DomainXmlPersistence;
import com.sun.enterprise.v3.services.impl.LogManagerService;
import com.sun.enterprise.web.WebDeployer;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import com.sun.web.security.RealmAdapter;
import com.sun.web.server.DecoratorForJ2EEInstanceListener;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.deployment.autodeploy.AutoDeployService;
import org.glassfish.embed.impl.*;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.hk2.component.InhabitantsParser;
import org.glassfish.embed.Server;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.web.WebEntityResolver;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.Inhabitants;

/**
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 */

class EmbeddedBootstrap extends Main{
    EmbeddedBootstrap(Server server) {
        this.server = server;
    }

    @Override
    protected InhabitantsParser createInhabitantsParser(Habitat parentHabitat) {
        return decorateInhabitantsParser(parentHabitat);
    }

    /**
     * Tweaks the 'recipe' --- for embedded use, we'd like GFv3 to behave a little bit
     * differently from normal stand-alone use.
     * @param parser
     * @return
     */
    private InhabitantsParser decorateInhabitantsParser(Habitat parentHabitat) {
        InhabitantsParser parser = super.createInhabitantsParser(parentHabitat);

        // registering the server using the base class and not the current instance class
        // (GlassFish server may be extended by the user)
        parser.habitat.add(new ExistingSingletonInhabitant<Server>(Server.class, server));

        // register scattered web handler before normal WarHandler kicks in.
        Inhabitant<ScatteredWarHandler> swh = Inhabitants.create(new ScatteredWarHandler());
        parser.habitat.add(swh);
        parser.habitat.addIndex(swh, ArchiveHandler.class.getName(), null);

        // we don't want GFv3 to reconfigure all the loggers
        parser.drop(LogManagerService.class);

        // we don't need admin CLI support.
        // TODO: admin CLI should be really moved to a separate class
        parser.drop(AdminConsoleAdapter.class);

        if (server.getInfo().autoDeploy == false) {
            try {
                Class.forName("org.glassfish.deployment.autodeploy.AutoDeployService");
                parser.drop(AutoDeployService.class);
            }
            catch (Exception e) {
                // ignore.  It may not be available
            }
        }

        //TODO: workaround for a bug
        parser.replace(ApplicationLifecycle.class, EmbeddedApplicationLifecycle.class);

        parser.replace(APIClassLoaderServiceImpl.class, EmbeddedAPIClassLoaderServiceImpl.class);
        // we don't really parse domain.xml from disk
        parser.replace(DomainXml.class, EmbeddedDomainXml.class);

        // ... and we don't persist it either.
        parser.replace(DomainXmlPersistence.class, EmbeddedDomainXml.class);
        try {
            // we provide our own ServerEnvironment
            EmbeddedServerEnvironment.setInstanceRoot(server.getFileSystem().getInstanceRoot());
        }
        catch (EmbeddedException ex) {
            //TODO ????
        }

        parser.replace(ServerEnvironmentImpl.class, EmbeddedServerEnvironment.class);

        {// adjustment for webtier only bundle
            // https://embedded-glassfish.dev.java.net/issues/show_bug.cgi?id=92
            // Dropping the InstanceListener leads to big problems with JNDI etc...
            // parser.drop(DecoratorForJ2EEInstanceListener.class);

            // in the webtier-only bundle, these components don't exist to begin with.

            try {
                // security code needs a whole lot more work to work in the modular environment.
                // disabling it for now.
                parser.drop(SecuritySniffer.class);

                // WebContainer has a bug in how it looks up Realm, but this should work around that.
                parser.drop(RealmAdapter.class);
            }
            catch (LinkageError e) {
                // maybe we are running in the webtier only bundle
            }
             
        }

        // override the location of default-web.xml
        parser.replace(WebDeployer.class, EmbeddedWebDeployer.class);

        // override the location of cached DTDs and schemas
        parser.replace(WebEntityResolver.class, EntityResolverImpl.class);

        parser.replace(CommandRunnerImpl.class, EmbeddedCommandRunner.class);

        return parser;
    }

    private Server server;
}
