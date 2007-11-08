/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.config.ConfigChange;

/**
 * Configuration Change Event. This event is raised when one or more
 * configuration attributes are changed. The user interface supports a feature
 * because of which edits to configuration attributes are applied only at
 * user request. Therefore, configuration change events can be associated to
 * more than one configuration change.
 */
public class ConfigChangeEvent extends AdminEvent {

    /**
     * Event type
     */
    static final String eventType = ConfigChangeEvent.class.getName();

    /**
     * Is web core reconfig needed.
     */
    private boolean webCoreReconfigNeeded = false;

    /**
     * Has init or obj conf file changed.
     */
    private boolean initOrObjConfChanged = false;

    /**
     * A map to track config changes that match any ConfigChangeCategory
     * regular expression.
     */
    private HashMap matchMap;

    /**
     * Create a new ConfigChangeEvent. Every element in configChangeList should
     * be of type com.sun.enterprise.config.ConfigChange.
     * @param instanceName name of the instance to which this event applies
     * @param configChangeList list of configuration attribute changes.
     */
    public ConfigChangeEvent(String instanceName,
            ArrayList configChangeList) {
        super(eventType, instanceName);
        this.configChangeList = configChangeList;
    }

    /**
     * Get config changes list. The list contains objects of type ConfigAdd,
     * ConfigUpdate or ConfigDelete from package com.sun.enterprise.config (all
     * of them are sub-classes of ConfigChange). In some cases, this event may
     * be created by specifying null for Config change list and if no changes
     * are added post creation by invoking package method addConfigChange then
     * this method will return null.
     * @return list of config changes
     */
    public ArrayList getConfigChangeList() {
        return configChangeList;
    }

    /**
     * Set web core reconfig needed status. Some of the changes are handled
     * by reconfig signal in web core. Setting the status to true results in
     * invokation of web core reconfig.
     * @param reconfig whether web core reconfig is needed
     */
    void setWebCoreReconfigNeeded(boolean reconfig) {
        webCoreReconfigNeeded = reconfig;
    }

    /**
     * Is web core reconfig needed. Web core reconfig is needed if init.conf,
     * obj.conf or mime type files have been changed or if server.xml elements
     * http-service or web-container have been changed.
     */
    boolean isWebCoreReconfigNeeded() {
        return webCoreReconfigNeeded;
    }

    /**
     * Set whether init.conf or obj.conf files have changed.
     * @param changed true if init.conf or obj.conf has changed
     */
    void setInitOrObjConfChanged(boolean changed) {
        initOrObjConfChanged = changed;
    }

    /**
     * Is init.conf or obj.conf changed. If true, then a restart will be
     * required to handle this change.
     */
    boolean isInitOrObjConfChanged() {
        return initOrObjConfChanged;
    }

    /**
     * Match the specified regular expression pattern against all changed XPath
     * associated to the event.
     * @param pattern the pattern to match xpath with
     * @return true if any xpath matches, false otherwise
     */
    boolean matchXPathToPattern(Pattern pattern) {
        boolean match = false;
        if (configChangeList == null) {
            return match;
        }
        Iterator iter = configChangeList.iterator();
        while (iter.hasNext()) {
            ConfigChange change = (ConfigChange)iter.next();
            String xpath = change.getXPath();
            if (xpath != null) {
                Matcher matcher = pattern.matcher(xpath);
                match = matcher.matches();
                if (match) {
                    setConfigChangeMatched(change);
                }
            }
        }
        return match;
    }

    /**
     * Is this event no op. A ConfigChangeEvent is no op, if it does not have
     * any server.xml changes or if it has not been told that web core
     * reconfig is needed.
     */
    boolean isNoOp() {
        boolean isNoOp = false;
        if (configChangeList == null && !webCoreReconfigNeeded) {
            isNoOp = true;
        }
        return isNoOp;
    }

    /**
     * Set specified config change as matched. This method is called when the
     * change xpath matches a pattern from a ConfigChangeEvent listener.
     * @param change the config change that contains matched xpath.
     */
    private void setConfigChangeMatched(ConfigChange change) {
        synchronized (this) {
            if (matchMap == null) {
                matchMap = new HashMap();
            }
        }
        matchMap.put(change, change);
    }

    /**
     * Is all xpath in this event matched to at least one listener. The event
     * contains a list of config changes and if xpaths for all the changes are
     * mactehd to at least one listener then the method returns true. If change
     * list is empty then the method returns false.
     */
    boolean isAllXPathMatched() {
        boolean matched = true;
        if (configChangeList == null || matchMap == null) {
            matched = false;
            return matched;
        }
        Iterator iter = configChangeList.iterator();
        while (iter.hasNext()) {
            if (!matchMap.containsKey(iter.next())) {
                matched = false;
                break;
            }
        }
        return matched;
    }
}
