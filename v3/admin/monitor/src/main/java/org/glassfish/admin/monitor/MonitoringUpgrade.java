package org.glassfish.admin.monitor;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.api.monitoring.ContainerMonitoring;
import com.sun.enterprise.config.serverbeans.MonitoringService;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyVetoException;

/**
 * This services upgrades the monitoring configuration.
 * 
 * @author Jerome Dochez
 */
@Service
public class MonitoringUpgrade implements ConfigurationUpgrade, PostConstruct {

    @Inject(optional=true)
    MonitoringService ms;

    @Inject
    Logger logger;
    
    public void postConstruct() {
        try {
            // get the level attribute from module-monitoring-levels
            if (ms == null) {
                logger.log(Level.INFO,
                    "No upgrade necessary, monitoring-service does not exist in domain.xml");
                return;
            }
            final ModuleMonitoringLevels mmls = ms.getModuleMonitoringLevels();
            if (mmls == null) {
                // already upgraded
                return;
            }

            // if monitoring-item exists set level
            // otherwise create element and set level

                ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {
                    public Object run(MonitoringService param)
                    throws PropertyVetoException, TransactionFailure {
                        if (isNonDefault(mmls.getOrb())) {
                            addContainerMonitoring(param, ContainerMonitoring.ORB, mmls.getOrb());
                        }
                        if (isNonDefault(mmls.getEjbContainer())) {
                            addContainerMonitoring(param, ContainerMonitoring.EJB_CONTAINER, mmls.getEjbContainer());
                        }
                        if (isNonDefault(mmls.getJmsService())) {
                            addContainerMonitoring(param, ContainerMonitoring.JMS_SERVICE, mmls.getJmsService());
                        }
                        if (isNonDefault(mmls.getConnectorConnectionPool())) {
                            addContainerMonitoring(param, ContainerMonitoring.CONNECTOR_CONNECTION_POOL, mmls.getConnectorConnectionPool());
                        }
                        if (isNonDefault(mmls.getConnectorService())) {
                            addContainerMonitoring(param, ContainerMonitoring.CONNECTOR_SERVICE, mmls.getConnectorService());
                        }
                        if (isNonDefault(mmls.getHttpService())) {
                            addContainerMonitoring(param, ContainerMonitoring.HTTP_SERVICE, mmls.getHttpService());
                        }
                        if (isNonDefault(mmls.getJdbcConnectionPool())) {
                            addContainerMonitoring(param, ContainerMonitoring.JDBC_CONNECTION_POOL, mmls.getJdbcConnectionPool());
                        }
                        if (isNonDefault(mmls.getJvm())) {
                            addContainerMonitoring(param, ContainerMonitoring.JVM, mmls.getJvm());
                        }
                        if (isNonDefault(mmls.getThreadPool())) {
                            addContainerMonitoring(param, ContainerMonitoring.THREAD_POOL, mmls.getThreadPool());
                        }
                        if (isNonDefault(mmls.getTransactionService())) {
                            addContainerMonitoring(param, ContainerMonitoring.TRANSACTION_SERVICE, mmls.getTransactionService());
                        }
                        if (isNonDefault(mmls.getWebContainer())) {
                            addContainerMonitoring(param, ContainerMonitoring.WEB_CONTAINER, mmls.getWebContainer());
                        }

                        return null;
                    }
                }, ms);
        } catch (TransactionFailure tf) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                "Failure while upgrading domain.xml monitoring-service", tf);
            throw new RuntimeException(tf);
        }
    }

    private boolean isNonDefault(String name) {
        return (name!=null && name.length()>0 && !name.equals(ContainerMonitoring.LEVEL_OFF));
    }

    private void addContainerMonitoring(MonitoringService ms, String name, String level)
            throws TransactionFailure, PropertyVetoException {

        if (ms.getContainerMonitoring(name) == null) {
            ContainerMonitoring newItem = ms.createChild(
                ContainerMonitoring.class);
            newItem.setName(name);
            newItem.setLevel(level);
            ms.getContainerMonitoring().add(newItem);
        }
    }
}
