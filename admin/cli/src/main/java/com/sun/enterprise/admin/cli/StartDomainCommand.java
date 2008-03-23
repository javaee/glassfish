package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.launcher.GFLauncher;
import com.sun.enterprise.admin.launcher.GFLauncherFactory;
import com.sun.enterprise.admin.launcher.GFLauncherInfo;
import com.sun.enterprise.cli.framework.*;
import java.util.logging.*;

public class StartDomainCommand extends Command {
    public boolean validateOptions() throws CommandValidationException {
        return true;
    }

    public void runCommand() throws CommandException, CommandValidationException {
        try {
            GFLauncher launcher = GFLauncherFactory.getInstance(
                    GFLauncherFactory.ServerType.domain);
            GFLauncherInfo info = launcher.getInfo();

            if (!operands.isEmpty()) {
                info.setDomainName((String) operands.firstElement());
            }

            String parent = getOption("domaindir");

            if (parent != null) {
                info.setDomainParentDir(parent);
            }

            info.setVerbose(getBooleanOption("verbose"));
            info.setDebug(getBooleanOption("debug"));
            info.setEmbedded(getBooleanOption("embedded"));
            launcher.launch();
        }
        catch (Throwable t) {
            throw new CommandException(getLocalizedString("CommandUnSuccessful",
                    new Object[]{name, t}), t);
        }
    }
}


