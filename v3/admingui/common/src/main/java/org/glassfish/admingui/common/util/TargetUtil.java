/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.management.Attribute;
import javax.management.AttributeList;

import javax.servlet.ServletContext;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.Realms;
import org.glassfish.admin.amx.base.RuntimeRoot;
import org.glassfish.admin.amx.base.ConnectorRuntimeAPIProvider;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.base.Query;
import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.intf.config.AMXConfigHelper;
import org.glassfish.admin.amx.intf.config.Application;
import org.glassfish.admin.amx.intf.config.ApplicationRef;
import org.glassfish.admin.amx.intf.config.Applications;
import org.glassfish.admin.amx.intf.config.Config;
import org.glassfish.admin.amx.intf.config.ConfigTools;
import org.glassfish.admin.amx.intf.config.Configs;
import org.glassfish.admin.amx.intf.config.Domain;
import org.glassfish.admin.amx.intf.config.PropertiesAccess;
import org.glassfish.admin.amx.intf.config.Property;

import org.glassfish.admin.amx.intf.config.Resources;
import org.glassfish.admin.amx.intf.config.Server;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkConfig;
import org.glassfish.admin.amx.intf.config.grizzly.NetworkListener;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author anilam
 */
public class TargetUtil {

    public  boolean isCluster(String name){
        return false;
    }

}
