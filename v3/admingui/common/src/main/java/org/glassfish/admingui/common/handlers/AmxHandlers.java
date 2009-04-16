/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.common.handlers;

import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import org.glassfish.admingui.common.util.MiscUtil;

/**
 *
 * @author jasonlee
 */
public class AmxHandlers {

    /**
     * <p> This handler returns the ConfigConfig object for the specified configuration name.</p>
     * <p> Input value:  "ConfigName"       -- Type: <code>java.lang.String</code></p>
     * <p> Output value: "configConfig"     -- Type: <code>com.sun.appserv.management.config.ConfigConfig</code></p>
     */
    @Handler(id = "getConfigConfig",
    input = {
        @HandlerInput(name = "configName", type = String.class, required = true)
    },
    output = {
        @HandlerOutput(name = "configConfig", type = ConfigConfig.class)
    })
    public static void getConfigConfig(HandlerContext handlerCtx) {
        String configName = ((String) handlerCtx.getInputValue("configName"));
        handlerCtx.setOutputValue("configConfig", AMXRoot.getInstance().getConfig(configName));
    }

    /**
     * This Handler will save the contents of a property sheet to the specified configuration
     * bean.  A typical usage will look something like this:
     * 
     * <code>insert example here</code>
     */
    @Handler(id = "savePropertiesMap",
    input = {
        @HandlerInput(name = "destination", type = PropertiesAccess.class, required = true),
        @HandlerInput(name = "values", type = Map.class, required = true)
    })
    public static void savePropertiesMap(HandlerContext handlerCtx) {
        final PropertiesAccess propertiesAccess = (PropertiesAccess) handlerCtx.getInputValue("destination");
        if (propertiesAccess == null) {
            throw new IllegalArgumentException("savePropertiesMap:  destination can not be null");
        }
        AMXUtil.updateProperties(propertiesAccess, (Map) handlerCtx.getInputValue("values"));
    }

    /**
     * This handler gets the default value for a configuration property for the given
     * module config
     */
    @Handler(id = "getDefaultConfigurationValue",
    input = {
        @HandlerInput(name = "moduleConfig", type = AMXConfig.class, required = true),
        @HandlerInput(name = "key", type = String.class, required = true)
    },
    output = {
        @HandlerOutput(name = "defaultValue", type = String.class)
    })
    public static void getDefaultConfgurationValue(HandlerContext handlerCtx) {
        AMXConfig amxConfig = (AMXConfig) handlerCtx.getInputValue("moduleConfig");
        if (amxConfig == null) {
            throw new IllegalArgumentException("getDefaultConfigurationValue:  moduleConfig can not be null");
        }
        handlerCtx.setOutputValue("defaultValue", amxConfig.getDefaultValue((String) handlerCtx.getInputValue("key")));
    }

    /**
     * This handler will take an AMXConfig object, and create a Map of values based
     * on the fields specified in the array
     * @param handlerCtx
     */
    @Handler(id = "createAmxConfigMap",
    input = {
        @HandlerInput(name = "moduleConfig", type = AMXConfig.class, required = true),
        @HandlerInput(name = "properties", type = List.class, required = true)
    },
    output = {
        @HandlerOutput(name = "configMap", type = Map.class)
    })
    public static void createAmxConfigMap(HandlerContext handlerCtx) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<String> properties = (List<String>) handlerCtx.getInputValue("properties");
        AMXConfig amxConfig = (AMXConfig) handlerCtx.getInputValue("moduleConfig");
        if (amxConfig == null) {
            throw new IllegalArgumentException("createAmxConfigMap:  moduleConfig can not be null");
        }
        if ((properties == null) || (properties.size() == 0)) {
            throw new IllegalArgumentException("createAmxConfigMap: properties can not be null or empty");
        }
        ValueExpression ve = MiscUtil.setValueExpression("#{amxConfigMap}", amxConfig);

        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final ELContext elContext = facesContext.getELContext();
        for (String prop : properties) {
            ValueExpression propVE = facesContext.getApplication().getExpressionFactory().
                    createValueExpression(elContext, "#{amxConfigMap." + prop + "}", Object.class);
            Object value = propVE.getValue(elContext);
            map.put(prop, value);
        }

        handlerCtx.setOutputValue("configMap", map);
    }

    /**
     * This handler will take an AMXConfig object, and create a Map of values based
     * on the fields specified in the array
     * @param handlerCtx
     */
    @Handler(id = "updateAmxConfig",
    input = {
        @HandlerInput(name = "moduleConfig", type = AMXConfig.class, required = true),
        @HandlerInput(name = "properties", type = List.class), // So things don't break, for now
        @HandlerInput(name = "configMap", type = Map.class)
    })
    public static void updateAmxConfig(HandlerContext handlerCtx) {
        AMXConfig amxConfig = (AMXConfig) handlerCtx.getInputValue("moduleConfig");
        Map<String, Object> map = (Map<String, Object>) handlerCtx.getInputValue("configMap");

        if (map != null) {
            MiscUtil.setValueExpression("#{amxConfig}", amxConfig);

            final FacesContext facesContext = FacesContext.getCurrentInstance();
            final ELContext elContext = facesContext.getELContext();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                ValueExpression propVE = facesContext.getApplication().getExpressionFactory().
                        createValueExpression(elContext, "#{amxConfig." + entry.getKey() + "}", Object.class);
                propVE.setValue(elContext, entry.getValue());
            }
        }
    }

    @Handler(id = "loadDefaultAmxConfigAttributes",
    input = {
        @HandlerInput(name = "amxConfig", type = AMXConfig.class, required = true),
        @HandlerInput(name = "configMap", type = Map.class, required = true)
    })
    public static void loadDefaultAmxConfigAttributes(HandlerContext handlerCtx) {
        AMXConfig amxConfig = (AMXConfig) handlerCtx.getInputValue("amxConfig");
        Map<String, Object> entries = (Map<String, Object>) handlerCtx.getInputValue("configMap");
        if (amxConfig == null) {
            throw new IllegalArgumentException("getDefaultConfigurationValue:  amxConfig can not be null");
        }
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            entries.put(entry.getKey(), amxConfig.getDefaultValue(entry.getKey()));
        }
    }

    /**
     * 
     */
    @Handler(id = "getAmxRoot",
    output = {
        @HandlerOutput(name = "amxRoot", type = AMXRoot.class)
    })
    public static void getAmxRootInstance(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("amxRoot", AMXRoot.getInstance());
    }
}
