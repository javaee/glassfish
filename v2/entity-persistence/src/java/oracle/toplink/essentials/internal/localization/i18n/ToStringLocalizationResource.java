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
package oracle.toplink.essentials.internal.localization.i18n;

import java.util.ListResourceBundle;

/**
 * English ResourceBundle for ToStringLocalization messages.
 *
 * @author Shannon Chen
 * @since TOPLink/Java 5.0
 */
public class ToStringLocalizationResource extends ListResourceBundle {
    static final Object[][] contents = {
                                           { "datasource_name", "datasource name" },
                                           { "datasource", "datasource" },
                                           { "error_printing_expression", "Error printing expression" },
                                           { "not_instantiated", "not instantiated" },
                                           { "connected", "connected" },
                                           { "disconnected", "disconnected" },
                                           { "nest_level", "(nest level = {0})" },
                                           { "commit_depth", "(commit depth = {0})" },
                                           { "empty_commit_order_dependency_node", "Empty Commit Order Dependency Node" },
                                           { "node", "Node ({0})" },
                                           { "platform", "platform" },
                                           { "user_name", "user name" },
                                           { "server_name", "server name" },
                                           { "database_name", "database name" },
                                           { "datasource_URL", "datasource URL" },
                                           { "depth_reset_key", "(depth = {0}, reset key = {1})" },
                                           { "pooled", "pooled" },
                                           { "login", "login" },
                                           { "lazy", "lazy" },
                                           { "non_lazy", "non-lazy" },
                                           { "min_max", "(minimum connections = {0}, maximum connections = {1})" },
                                           { "begin_profile_of", "Begin profile of" },
                                           { "end_profile", "End profile" },
                                           { "profile", "Profile" },
                                           { "class", "class" },
                                           { "number_of_objects", "number of objects" },
                                           { "total_time", "total time" },
                                           { "local_time", "local time" },
                                           { "profiling_time", "profiling time" },
                                           { "time_object", "time/object" },
                                           { "objects_second", "objects/second" },
                                           { "shortestTime", "shortest time" },
                                           { "longestTime", "longest time" },
                                           { "context", "Context:	" },
                                           { "name", "Name: " },
                                           { "schema", "Schema: " },
                                           { "no_streams", "no stream(s)" },
                                           { "reader", "reader" },
                                           { "multiple_readers", "multiple readers" },
                                           { "writer", "writer" },
                                           { "no_files", "no file(s)" },
                                           { "mulitple_files", "mulitple files" },
                                           { "unknown", "unknown" },
                                           { "connector", "connector" },
                                           { "staticweave_processor_unknown_outcome", "Weaving classes stored in a directory and outputing to a JAR often leads to unexpected results." },
                                           { "staticweave_commandline_help_message",
                                               "  Usage: StaticWeave [options] source target\r\r"+
                                               "  Options:\r"+
                                               "    -classpath classpath\r" +
                                               "           Set the user class path. use \";\" as delimiter in windows and \":\" in unix.\r"+
                                               "    -persistenceinfo \r" +
                                               "           Explicitly identify where META-INF/persistence.xml is stored. It must be the root of META-INF/persistence.xml.\r"+
                                               "    -log \r" +
                                               "           Specify logging file.\r"+
                                               "    -loglevel \r" +
                                               "           Specify a literal value of the toplink logging level(OFF,SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST).\r"+
                                               "    The classpath must contain all the classes necessary to load the classes in the source.\r"+ 
                                               "    The weaving will be performed in place if source and target point to the same location. Weaving in place is ONLY applicable for directory-based sources.\r"+ 
                                               "  Example:\r" +
                                               "    To weave all entites contained in c:\\foo-source.jar with its persistence.xml contained within the c:\\foo-containing-persistence-xml.jar,\r"+
                                               "    and output to c:\\foo-target.jar,\r"+
                                               "    StaticWeave -persistenceinfo c:\\foo-containing-persistence-xml.jar -classpath c:\\classpath1;c:\\classpath2 c:\\foo-source.jar c:\\foo-target.jar\r\r"}
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}
