/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package org.glassfish.admin.rest.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.glassfish.admin.rest.logviewer.LogViewerResource;
import org.glassfish.admin.rest.RestService;
import org.glassfish.admin.rest.results.ActionReportResult;
import org.glassfish.admin.rest.results.OptionsResult;
import org.glassfish.admin.rest.results.StringListResult;
import org.glassfish.admin.rest.utils.xml.RestActionReporter;
import org.glassfish.api.admin.ParameterMap;
//import org.glassfish.external.amx.AMXGlassfish;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Dom;

/**
 * This is the root class for the generated DomainResource
 * that bootstrap the dom tree with the domain object
 * and add a few sub resources like log viewer
 * or log-level setup which are not described as configbeans
 * but more external config or files (server.log or JDK logger setup
 * 
 * @author ludo
 */
public class GlassFishDomainResource extends TemplateResource {
    @Override
    public Dom getEntity() {
        return RestService.getDomainBean();
    }

    @Path("view-log/")
    public LogViewerResource getViewLogResource() {
        LogViewerResource resource = resourceContext.getResource(LogViewerResource.class);
        return resource;
    }

    @GET
    @Path("jmx-urls/")
    @Produces({"text/html;qs=2",MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public ActionReportResult getJmxServiceUrl() {
        try {
            Habitat habitat = RestService.getHabitat();
            MBeanServer mBeanServer = habitat.getComponent(MBeanServer.class);
            JMXServiceURL[] urls = (JMXServiceURL[]) mBeanServer.getAttribute(getBootAMXMBeanObjectName(), "JMXServiceURLs");
            List<String> jmxUrls = new ArrayList();
            for (JMXServiceURL url : urls) {
                jmxUrls.add(url.getURLPath());
            }
            RestActionReporter ar = new RestActionReporter();
            ar.setActionDescription("Get JMX Service URLs");
            ar.setSuccess();
            ar.getExtraProperties().put("jmxServiceUrls", jmxUrls);
            return new ActionReportResult(ar);
        } catch (final JMException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectName getBootAMXMBeanObjectName() {
        try {
            return new ObjectName("amx-support:type=boot-amx");
        } catch (final Exception e) {
            throw new RuntimeException("bad ObjectName", e);
        }
    }



    @POST
    @Path("set/")
    @Produces({"text/html;qs=2",MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED})
    public ActionReportResult setDomainConfig(HashMap<String, String> data) {
        TemplateExecCommand resource = new TemplateExecCommand("DomainResource", "set", "POST", "commandAction", "set", false);
        resource.requestHeaders = requestHeaders;
        
        final Iterator<Entry<String, String>> iterator = data.entrySet().iterator();
        if (iterator.hasNext()) {
            ParameterMap fixed = new ParameterMap();
            Map.Entry entry = iterator.next();
            fixed.add("DEFAULT", entry.getKey()+"="+entry.getValue());

            return resource.executeCommand(fixed);
        }

        throw new RuntimeException("You must supply exactly one configuration option."); //i18n
    }
}
