/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.admin.cli;

import java.util.*;

/**
 * A map of the known local commands.
 * XXX - This is temporary until we have a new client side extensibility
 * framework for commands, e.g., use of HK2 on the client.
 *
 * @author Bill Shannon
 */
public class CommandTable extends HashMap<String, Class> {

    /**
     * Construct a CommandTable object with the builtin list of commands.
     */
    public CommandTable() {
        super();
        put("change-admin-password",    ChangeAdminPasswordCommand.class);
        put("change-master-password",   null);
        put("list-commands",            ListCommandsCommand.class);
        put("monitor",                  MonitorCommand.class);
        put("multimode",                MultimodeCommand.class);
        put("restart-domain",           RestartDomainCommand.class);
        put("start-domain",             StartDomainCommand.class);
        put("stop-domain",              StopDomainCommand.class);
        put("version",                  VersionCommand.class);
        put("help",                     HelpCommand.class);
        put("export",                   ExportCommand.class);
        put("unset",                    UnsetCommand.class);
        put("login",                    null);
        put("run-script",               RunScriptLocalCommand.class);
        put("create-domain",            null);
        put("create-service",           null);
        put("delete-domain",            null);
        put("list-domains",             null);
        put("start-database",           null);
        put("stop-database",            null);
        put("verify-domain-xml",        null);
    }
}
