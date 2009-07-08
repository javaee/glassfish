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

import com.sun.enterprise.cli.framework.*;

/**
 * A local command.  This is just a proxy for local commands implemented
 * by the old CLI framework.  This will be removed when we remove our
 * dependency on the old CLI framework and convert all the local commands
 * to use the new framework.
 *
 * @author Bill Shannon
 */
public class LocalCommand extends CLICommand {
    private CLIMain cli;

    public LocalCommand(String name, ProgramOptions po, Environment env) {
        super(name, po, env);
        cli = new CLIMain();
    }

    public int execute(String[] args) throws CommandException {
        String[] nargs = new String[args.length + 1];
        System.arraycopy(args, 0, nargs, 1, args.length);
        nargs[0] = name;
        try {
            cli.invokeCommand(nargs);
            return SUCCESS;
        } catch (CommandException ce) {
            logger.printError(ce.getMessage());
            return ERROR;
        } catch (CommandValidationException cve) {
            logger.printError(cve.getMessage());
            return ERROR;
        } catch (NoClassDefFoundError ncdfe) {
            logger.printError(ncdfe.toString());
            return ERROR;
        } catch (InvalidCommandException ice) {
            return INVALID_COMMAND_ERROR;
        } catch (Throwable ex) {
            logger.printExceptionStackTrace(ex);
            logger.printError(ex.toString());
            return ERROR;
        }
    }

    protected void prepare() {
        // never called because we override execute
    }

    protected int executeCommand() {
        // never called because we override execute
        return -1;
    }
}
