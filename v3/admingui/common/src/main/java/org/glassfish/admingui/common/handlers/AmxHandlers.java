/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
