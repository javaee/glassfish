/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.console.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.*;

@ManagedBean(name="listServicesBean")
@SessionScoped
public class ListServicesBean {
    public List<Database> databases;
    public List<Template> templates;

    public ListServicesBean() {
        databases = getDatabases();
        templates = getTeamplates();
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    Database selectedDatabase = new Database();
    Template selectedTemplate = new Template();

    public Database getSelectedDatabase() {
        return selectedDatabase;
    }

    public void setSelectedDatabase(Database selectedDatabase) {
        this.selectedDatabase = selectedDatabase;
    }

    public Template getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(Template selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public List<Database> getDatabases() {
        List<Database> databases = new ArrayList<Database>();
        databases.add(new Database("MySQL", "root", "", "jdbc/_mysql_cloud_sample"));
        databases.add(new Database("Oracle", "system", "manager", "jdbc/_oracle_cloud_sample"));
        databases.add(new Database("Derby", "system", "manager", "jdbc/_derby_cloud_sample"));
        return databases;
    }

    private List<Template> getTeamplates() {
        List<Template> tempaltes = new ArrayList<Template>();
        tempaltes.add(new Template("GlassFish Small", "1-5"));
        tempaltes.add(new Template("GF Medium", "1-5"));
        return tempaltes;
    }

    public static class Database {
        private String name;
        private String user;
        private String password;
        private String jndiName;

        public Database() {
        }

        public Database(String name, String user, String password, String jndiName) {
            this.name = name;
            this.user = user;
            this.password = password;
            this.jndiName = jndiName;
        }

        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getJndiName() {
            return jndiName;
        }

        public void setJndiName(String jndiName) {
            this.jndiName = jndiName;
        }

    }

    public static class Template {
        private String name;
        private String minMaxInstances;

        public Template() {
        }

        public Template(String name, String minMaxInstances) {
            this.name = name;
            this.minMaxInstances = minMaxInstances;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMinMaxInstances() {
            return minMaxInstances;
        }

        public void setMinMaxInstances(String minMaxInstances) {
            this.minMaxInstances = minMaxInstances;
        }

    }
}
