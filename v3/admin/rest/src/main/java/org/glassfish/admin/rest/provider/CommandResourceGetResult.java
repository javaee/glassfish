/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admin.rest.provider;

/**
 * Response information object. Returned on call to GET method on command
 * resource. Information used by provider to generate the appropriate output.
 *
 * @author Rajeshwar Patil
 */
public class CommandResourceGetResult extends Result {

    /**
     * Constructor
     */
    public CommandResourceGetResult(String commandResourceName, String command,
            String commandDisplayName, String commandMethod,
                String commandAction, OptionsResult metaData) {
        __commandResourceName = commandResourceName;
        __command = command;
        __commandDisplayName = commandDisplayName;
        __commandMethod = commandMethod;
        __metaData = metaData;
        __commandAction = commandAction;
    }

    /**
     * Returns command resource name.
     */
    public String getCommandResourceName() {
        return __commandResourceName;
    }

    /**
     * Returns command associated with the command resource.
     */
    public String getCommand() {
        return __command;
    }

    /**
     * Returns display name for command associated with the command resource.
     */
    public String getCommandDisplayName() {
        return __commandDisplayName;
    }

    /**
     * Returns type of command associated with the command resource.
     * Type can be POST, GET or DETELE.
     */
    public String getCommandMethod() {
        return __commandMethod;
    }

    /**
     * Returns the action of the command resoruce. Action can be used to
     * lable button in case of html representation.
     */
    public String getCommondAction() {
        return __commandAction;
    }

    /**
     * Returns OptionsResult - the meta-data of this resource.
     */
    public OptionsResult getMetaData() {
        return __metaData;
    }


    String __commandResourceName;
    String __command;
    String __commandDisplayName;
    String __commandMethod;

    //used in case of html representation as button lable
    String __commandAction;

    OptionsResult __metaData;
}
