package org.glassfish.persistence.jpa;

import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.Deployer;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service(name = "org.glassfish.persistence.jpa.JPAContainer")
public class JPAContainer implements org.glassfish.api.container.Container {

    public Class<? extends Deployer> getDeployer() {
        return JPADeployer.class;
    }

    public String getName() {
        return "JPA";
    }

}