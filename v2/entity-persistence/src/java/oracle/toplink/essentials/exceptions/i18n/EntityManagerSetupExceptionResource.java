/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.exceptions.i18n;

import java.util.ListResourceBundle;

/**
 * INTERNAL:
 * English ResourceBundle for EntityManagerSetupException messages.
 *
 * @author: Tom Ware
 */
public class EntityManagerSetupExceptionResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "28001", "A ValidationException was thrown while trying to create session: [{0}] " + ". The most likely causes of this issue are that your [{1}] file is not available on the classpath " + "or it does not contain a session called: [{0}]." },
                                           { "28002", "TopLink is attemting to load a ServerSession named [{0}] from [{1}], and not getting a ServerSession." },
                                           { "28003", "TopLink has loaded Session [{0}] from [{1}] and it either does not have a server platform specified or specifies " + "a server platform that does not use and external transaction controller.  Please specify an appropriate server platform if you plan to use JTA." },
                                           { "28004", "Error in setup of EntityManager factory: JavaSECMPInitializer.initializeFromMain returned false." },
                                           { "28005", "An Exception was thrown in setup of EntityManager factory." },
                                           { "28006", "ClassNotFound: [{0}] specified in [{1}] property." },
                                           { "28007", "Failed to instantiate ServerPlatform of type [{0}] specified in [{1}] property." },
                                           { "28008", "Class: {0} was not found while processing annotations." },
                                           { "28009", "Attempted to redeploy a session named {0} without closing it." },
                                           { "28010", "PersistenceUnitInfo {0} has transactionType JTA, but doesn't have jtaDataSource." },
                                           { "28011", "The session, [{0}], built for a persistence unit was not available at the time it was deployed.  This means that somehow the session was removed from the container in the middle of the deployment process." },
                                           { "28012", "Value [{0}] is of incorrect type for property [{2}], value type should be [{1}]." },
                                           { "28013", "Attempted to deploy PersistenceUnit [{0}] while being in the wrong state [{1}]. Close all factories for this PersistenceUnit." },
                                           { "28014", "Exception was thrown while processing property [{0}] with value [{1}]." },
                                           { "28015", "Failed to instantiate SessionLog of type [{0}] specified in [{1}] property." },
                                           { "28016", "The persistence unit with name [{0}] does not exist." },
                                           { "28017", "Attempted to predeploy PersistenceUnit [{0}] while being in the wrong state [{1}]." },
                                           { "28018", "predeploy for PersistenceUnit [{0}] failed." },
                                           { "28019", "deploy for PersistenceUnit [{0}] failed. Close all factories for this PersistenceUnit." },
                                           { "28020", "Value [true] for the property [toplink.weaving] is incorrect when global instrumentation is null, value should either be null or false." }
   };

    /**
      * Return the lookup table.
      */
    protected Object[][] getContents() {
        return contents;
    }
}
