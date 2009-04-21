package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.server.logging.GFFileHandler;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommand;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
/**
 * Created by IntelliJ IDEA.
 * User: cmott
 * Date: Apr 20, 2009
 * Time: 5:32:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="rotate-log")
public class RotateLog implements AdminCommand {

    @Inject
    GFFileHandler gf;

    public void execute(AdminCommandContext context) {
        gf.rotate();

    }
}
