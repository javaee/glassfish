/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.javaee7.batch.test.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: makannan
 * Date: 4/5/13
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandUtil {

    private int exitCode;

    private Throwable cause;

    private List<String> result = new ArrayList<String>();

    private CommandUtil() {}

    public static CommandUtil getInstance() {
        return new CommandUtil();
    }

    public CommandUtil executeCommandAndGetAsList(String... command) {
        return executeCommandAndGetAsList(true, command);
    }

    public CommandUtil executeCommandAndGetAsList(boolean  withOutput, String... command) {
        try {
            if (withOutput) {
                System.out.println();
                for (String s : command) System.out.print(s + " ");
                System.out.println();
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    result.add(line);
                    if (withOutput)
                        System.out.println(line);
                }
            } finally {
                br.close();
            }

            exitCode = process.waitFor();

        } catch (Throwable ex) {
            cause = ex;
        }

        return this;
    }
    public CommandUtil executeCommandAndGetErrorOutput(String... command) {
        return executeCommandAndGetErrorOutput(true, command);
    }

    public CommandUtil executeCommandAndGetErrorOutput(boolean  withOutput, String... command) {
        try {
            if (withOutput) {
                System.out.println();
                for (String s : command) System.out.print(s + " ");
                System.out.println();
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    result.add(line);
                    if (withOutput)
                        System.out.println(line);
                }
            } finally {
                br.close();
            }

            exitCode = process.waitFor();

        } catch (Throwable ex) {
            cause = ex;
        }

        return this;
    }

    public boolean ranOK() {
        return cause == null && exitCode == 0;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Throwable getCause() {
        return cause;
    }

    public List<String> result() {
        return result;
    }
}
