package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.config.serverbeans.HttpService;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class HttpServicePropertiesUpgrade extends BaseLegacyConfigurationUpgrade {
    @Inject
    private HttpService service;

    public void execute(AdminCommandContext context) {
        boolean done = false;
        try {
            final List<Property> properties = service.getProperty();
            final Iterator<Property> iterator = properties.iterator();
            while (!done && iterator.hasNext()) {
                final Property property = iterator.next();
                String name = property.getName();
                if ("accessLoggingEnabled".equals(name)
                    || "accessLogBufferSize".equals(name)
                    || "accessLogWriteInterval".equals(name)
                    || "sso-enabled".equals(name)) {
                    done = true;
                    upgrade(context, property);
                }
            }
        } catch (TransactionFailure tf) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading http-service properties."
                + "  Please check logs for errors", tf);
            throw new RuntimeException(tf);
        }
    }

    private void upgrade(final AdminCommandContext context, final Property property) throws TransactionFailure {
        if ("accessLoggingEnabled".equals(property.getName())) {
            updatePropertyToAttribute(context, service, "accessLoggingEnabled", "accessLoggingEnabled");
        } else if ("accessLogBufferSize".equals(property.getName())) {
            ConfigSupport.apply(new SingleConfigCode<AccessLog>() {
                @Override
                public Object run(AccessLog param) {
                    param.setBufferSizeBytes(property.getValue());
                    return param;
                }
            }, service.getAccessLog());
            removeProperty(service, "accessLogBufferSize");
            report(context,
                "Moved http-service.property.accessLogBufferSize to http-service.access-log.buffer-size-bytes");
        } else if ("accessLogWriteInterval".equals(property.getName())) {
            ConfigSupport.apply(new SingleConfigCode<AccessLog>() {
                @Override
                public Object run(AccessLog param) {
                    param.setWriteIntervalSeconds(property.getValue());
                    return param;
                }
            }, service.getAccessLog());
            removeProperty(service, "accessLogWriteInterval");
            report(context,
                "Moved http-service.property.accessLogWriteInterval to http-service.access-log.write-interval-seconds");
        } else if ("sso-enabled".equals(property.getName())) {
            updatePropertyToAttribute(context, service, "sso-enabled", "ssoEnabled");
        }
    }

}
