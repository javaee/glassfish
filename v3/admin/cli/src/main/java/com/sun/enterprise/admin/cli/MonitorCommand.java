/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.cli.framework.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * A local Monitor Command (This will call the Remote 'Monitor' Command).
 * The reason for having to implement this as local is to interpret the options
 * --interval and --filename(TBD) options
 * 
 * @author Prashanth
 */
public class MonitorCommand extends AbstractCommand {
    @Override
    public void runCommand() throws CommandException, CommandValidationException {
        validateOptions();
        //Based on interval, loop the subject to print the output
        Timer timer = new Timer();
        try
        {
            MonitorTask monitorTask = new MonitorTask(timer, type, filter,
                                            getRemoteArgs(), verbose, fileName);
            timer.scheduleAtFixedRate(monitorTask, 0, interval);

            boolean done = false;
                // detect if a q or Q key is entered
            while (!done)
            {
                //The native doesn't exist yet
                //final char c = new CliUtil().getKeyboardInput();
                //final char c = 'p';
                final String str = new BufferedReader(
                                    new InputStreamReader(System.in)).readLine(); 
                if (str.equals("q") || str.equals("Q"))
                {
                    timer.cancel();
                    done = true;
                    String exceptionMessage = monitorTask.getExceptionMessage();
                    if (exceptionMessage != null) {
                        throw new CommandException(exceptionMessage);
                    }
                }
                else if (str.equals("h") || str.equals("H"))
                {
                    monitorTask.displayDetails();
                }
                
            }
        }
        catch(Exception e) {
            timer.cancel();
            throw new CommandException(strings.get("monitorCommand.errorRemote", e.getMessage()));
        }
    }

    @Override
    public boolean validateOptions() throws CommandValidationException {
        super.validateOptions();
        
        port         = getOption(PORT);
        host         = getOption(HOST);
        user         = getOption(USER);
        passwordFile = getOption(PASSWORDFILE);
        interval = Integer.parseInt(getOption(INTERVAL)) * 1000;
        type = getOption(TYPE);
        filter = getOption(FILTER);
        return true;
    }

    private String[] getRemoteArgs() {
        List<String> list = new ArrayList<String>(5);
        list.add("monitor");
        
        if(ok(port)) {
            list.add("--port");
            list.add(port);
        }
        if(ok(host)) {
            list.add("--host");
            list.add(host);
        }
        if(ok(user)) {
            list.add("--user");
            list.add(user);
        }
        if(ok(passwordFile)) {
            list.add("--passwordfile");
            list.add(passwordFile);
        }
        if(ok(type)) {
            list.add("--type");
            list.add(type);
        }
        if(ok(filter)) {
            list.add("--filter");
            list.add(filter);
        }
        return list.toArray(new String[list.size()]);
    }
    
    private static boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    String port;
    String host;
    String user;
    String passwordFile;
    int interval;
    String type;
    String filter;
    boolean verbose;
    File fileName;
    public final static String INTERVAL = "interval";
    public final static String TYPE = "type";
    public final static String FILTER = "filter";
    private final static LocalStringsImpl strings = new LocalStringsImpl(MonitorCommand.class);
    private final static CLILogger logger = CLILogger.getInstance();
}
