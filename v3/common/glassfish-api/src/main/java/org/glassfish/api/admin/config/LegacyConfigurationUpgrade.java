package org.glassfish.api.admin.config;

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.api.admin.AdminCommandContext;

/**
 * Contract for upgrading legacy configuration values to their new location or simply removing them if they are no
 * longer supported.  Implmentations should notify the user any of any changes.
 *
 * @author Justin Lee
 */
@Contract
public interface LegacyConfigurationUpgrade {
    void execute(AdminCommandContext context);
}