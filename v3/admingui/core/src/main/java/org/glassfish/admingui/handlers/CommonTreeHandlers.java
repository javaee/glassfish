

package org.glassfish.admingui.handlers;
        
import com.sun.appserv.management.config.ApplicationConfig;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import org.glassfish.admingui.tree.FilterTreeEvent;

import java.util.ArrayList;

import java.util.List;
import javax.management.ObjectName;

/**
 *
 * @author anilam
 */
public class CommonTreeHandlers {
    
    /**
     *	<p> Default Constructor.</p>
     */
    public CommonTreeHandlers() {
    }
    
    
    /**
     *  <p> This handler filters out all the system apps from the list of objName available
     *      through the event object, based on the object-type attribute.
     */
     @Handler(id="filterSystemApps",
     input={
        @HandlerInput(name="appType", type=String.class, required=true)})
    public static Object filterSystemApps(HandlerContext handlerCtx) {
        FilterTreeEvent event = (FilterTreeEvent) handlerCtx.getEventObject();
        List orig = event.getChildObjects();
        ArrayList result = new ArrayList();
        
        if ( orig == null || orig.size() <=0 )
            return orig;
        String appType = (String)handlerCtx.getInputValue("appType");
        for(Object oneChild: orig ){
            if (oneChild instanceof ApplicationConfig){
                if (isAppType( (ApplicationConfig) oneChild, appType)){
                    result.add(oneChild);
                }
            }
        }
        return result;
    }
     
    private static boolean isAppType(ApplicationConfig testApp, String type){
        return true;
    }
}
