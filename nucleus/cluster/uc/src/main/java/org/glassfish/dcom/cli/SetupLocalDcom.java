/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.dcom.cli;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import java.io.*;
import java.io.File;
import java.util.*;
import org.glassfish.api.Param;
import static com.sun.enterprise.universal.process.ProcessUtils.getExe;
import javax.inject.Inject*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.util.OS;

/*
 * @author Byron Nevins
 */
@Service(name = "setup-local-dcom")
@PerLookup
public final class SetupLocalDcom extends CLICommand {
    @Param(name = "verbose", shortName = "v", primary = false, optional = true)
    boolean verbose;
    @Param(name = "force", shortName = "f", primary = false, optional = true)
    boolean force;
    private final static String[] DEPENDENCIES = new String[]{
        "advapi32.dll",
        "kernel32.dll", //"test32.dll",
    };
    private static final String CPP_APP_FILENAME = "DcomConfigurator.exe";
    private static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"));
    private static final File CPP_APP = new File(TMPDIR, CPP_APP_FILENAME);
    private final Console console = System.console();

    @Override
    protected void validate() throws CommandException {
        super.validate();

        if (!OS.isWindowsForSure())
            throw new CommandException(Strings.get("vld.windows.only"));

        if (console == null)
            throw new CommandException(Strings.get("vld.noconsole"));

        if(!force)
            areYouSure();

        checkPath();
        prepareCppApp();
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        try {
            List<String> cmds = new ArrayList<String>();
            cmds.add(CPP_APP.getAbsolutePath());

            if (verbose)
                cmds.add("--verbose");

            ProcessManager pm = new ProcessManager(cmds);
            pm.execute();

            int ret = pm.getExitValue();

            if (verbose || ret != 0)
                logger.info(pm.getStdout() + pm.getStderr());

            return ret;
        }
        catch (ProcessManagerException ex) {
            throw new CommandException(ex);
        }
    }

    /**
     * make sure that the right DLLs for running the C++ program are in the Path
     * @throws CommandException if the right DLLs are not in the path.
     */
    private void checkPath() throws CommandException {
        List<String> notFound = new ArrayList<String>();

        for (String dll : DEPENDENCIES) {
            File f = getExe(dll);

            if (f == null)
                notFound.add(dll);
            else
                logger.finer("Required DLL Located: " + f);
        }

        if (!notFound.isEmpty()) {
            throw prepareMissingDllMessage(notFound);
        }
    }

    private static CommandException prepareMissingDllMessage(List<String> notFound) {
        StringBuilder sb = new StringBuilder();

        for (String dll : notFound) {
            sb.append('\t').append(dll).append('\n');
        }
        String msg = Strings.get("vld.missing.dlls", sb.toString());
        return new CommandException(msg);
    }

    private void prepareCppApp() throws CommandException {
        if (!TMPDIR.isDirectory())
            throw exceptionMaker("internal.error", Strings.get("vld.badtempdir", TMPDIR));

        if (CPP_APP.exists()) {
            CPP_APP.delete();

            if (CPP_APP.exists())
                throw exceptionMaker("vld.app.exists", CPP_APP);

            finer("vld.app.deleted", CPP_APP);
        }

        CPP_APP.deleteOnExit();

        // copy it from inside this jar to the file system
        InputStream in = getClass().getResourceAsStream("/lib/" + CPP_APP_FILENAME);
        FileOutputStream out;

        try {
            out = new FileOutputStream(CPP_APP);
            copyStream(in, out);
        }
        catch (IOException ex) {
            throw exceptionMaker("vld.error.extracting.ex", CPP_APP, ex);
        }

        if (!CPP_APP.canExecute())
            throw exceptionMaker("vld.error.extracting", CPP_APP);
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null)
            throw new NullPointerException("internal error: null arguments");

        byte[] buf = new byte[16384];
        int len;

        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }
    /*
     * note how this method will likely be inlined by the compiler since it is tiny
     * and private...
     */

    private CommandException exceptionMaker(String key, Object... args) {
        if (args == null || args.length == 0)
            return new CommandException(Strings.get(key));
        else
            return new CommandException(Strings.get(key, args));
    }

    private void finer(String key, Object... args) {
        logger.finer(Strings.get(key, args));
    }

    private void areYouSure() throws CommandException {
        if (!programOpts.isInteractive())
            throw new CommandException(Strings.get("vld.not.interactive"));

        String msg = Strings.get("vld.areyousure");
        String answer = console.readLine("%s:  ", msg);

        if (!"yes".equalsIgnoreCase(answer))
            throw new CommandException(Strings.get("vld.no"));
    }
}
