package org.glassfish.vmcluster;

import org.glassfish.vmcluster.config.Action;
import org.glassfish.vmcluster.config.Virtualization;
import org.jvnet.hk2.annotations.Service;

import java.io.File;


/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Sep 14, 2010
 * Time: 2:19:18 PM
 * To change this template use File | Settings | File Templates.
 */

public class ShellCommand {

    final Action action;
    final String path;
    final ParameterResolver resolver;

    public ShellCommand(String path, Action action, ParameterResolver resolver) {
        this.path = path;
        this.action = action;
        this.resolver = resolver;
    }

    public String build() {

        StringBuilder builder = new StringBuilder();
        File f = new File(path, action.getCommand());
        if (action.getInvoker()!=null) {
            builder.append(action.getInvoker()).append(' ');
        }
        if (f.exists()) {
            builder.append(f.getAbsolutePath());
        } else {
            // rely on path
            builder.append(action.getCommand());
        }
        builder.append(' ');
        for (String p : action.getParameters()) {
            if (p!=null) {
                String resolved = resolver.resolve(p);
                if (resolved==null) {
                    builder.append(p);
                } else {
                    builder.append(resolved);
                }

            }
            builder.append(' ');
        }
        
        return builder.toString();
    }
}
