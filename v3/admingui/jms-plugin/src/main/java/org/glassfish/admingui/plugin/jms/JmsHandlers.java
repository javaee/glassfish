/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.plugin.jms;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author jasonlee
 */
public class JmsHandlers {
    protected static final String JMS_OBJECT_NAME = "com.sun.messaging.jms.server:type=DestinationManager,subtype=Config";
    protected static final String OP_LIST_DESTINATIONS = "getDestinations";
    protected static final String OP_CREATE = "create";
    protected static final String OP_DESTROY = "destroy";
    protected static final String PROP_NAME = "name";
    protected static final String PROP_DEST_TYPE = "desttype";

    @Handler(id="getMqServerConnection",
        output={@HandlerOutput(name="connection", type=MBeanServerConnection.class)}
    )
    public static void getIMQMBeanServerConnection(HandlerContext handlerCtx) {
        handlerCtx.setOutputValue("connection", ManagementFactory.getPlatformMBeanServer());
    }

    public static void getDestinations(HandlerContext handlerCtx) {
        //String result = (String)JMXUtil.invoke(JMS_OBJECT_NAME, OP_LIST_DESTINATIONS, null, null);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName[] results = null;
        try {
            results = (ObjectName[]) mbs.invoke(new ObjectName(JMS_OBJECT_NAME), OP_LIST_DESTINATIONS, new Object[]{}, new String[]{});
        } catch (Exception ex) {
            Logger.getLogger(JmsHandlers.class.getName()).log(Level.SEVERE, null, ex);
        }
        GuiUtil.getLogger().info("***** result = " + results[0].toString());
    }

    @Handler(id="getPhysicalDestinations",
        input={
            @HandlerInput(name="targetName", type=String.class, required=true),
            @HandlerInput(name="selectedRows", type=List.class)},
        output={
            @HandlerOutput(name="result", type=java.util.List.class)}
     )
    public static void getPhysicalDestinations(HandlerContext handlerCtx){

        String configName = ((String)handlerCtx.getInputValue("targetName"));
        ObjectName[] objectNames = null;
        List result = new ArrayList();
        try{
            //com.sun.messaging.jms.server:type=Destination,subtype=Config,desttype=q,name="mq.sys.dmq"
            //
            objectNames = (ObjectName[])JMXUtil.invoke(
                    //"com.sun.appserv:type=resources,category=config", "listPhysicalDestinations", params, types);
                    JMS_OBJECT_NAME, OP_LIST_DESTINATIONS);

            if (objectNames == null) {
                handlerCtx.setOutputValue("result", result);
                return; //nothing to load..
            }
            List<Map> selectedList = (List)handlerCtx.getInputValue("selectedRows");
            boolean hasOrig = (selectedList == null || selectedList.size()==0) ? false: true;

            for (int i = 0; i < objectNames.length; i++) {
                // getAttributes for the given objectName...
                HashMap oneRow = new HashMap();
                oneRow.put("name", objectNames[i].getKeyProperty(PROP_NAME).replaceAll("\"", ""));
                oneRow.put("type", "t".equals(objectNames[i].getKeyProperty(PROP_DEST_TYPE)) ? "topic" : "queue");
                oneRow.put("selected", (hasOrig)? isSelected(objectNames[i].getKeyProperty(PROP_NAME), selectedList): false);
                result.add(oneRow);
            }

           }catch(Exception ex){
               System.out.println("invoke:   " + JMS_OBJECT_NAME + ", method  =  " + OP_LIST_DESTINATIONS);
               GuiUtil.handleException(handlerCtx, ex);
           }
           handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p> This handler saves the values for all the attributes in
     *      Edit Realms Page </p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "createPhysicalDestination",
    input = {
        //@HandlerInput(name = "targetName", type = String.class, required = true),
        @HandlerInput(name = "name", type = String.class, required = true),
        //@HandlerInput(name = "Edit", type = Boolean.class, required = true),
        @HandlerInput(name = "addProps", type = Map.class),
        @HandlerInput(name = "type", type = String.class)})
    public static void savePhysicalDestinations(HandlerContext handlerCtx) {
        try {
            final String type = (String) handlerCtx.getInputValue("type");
            final String name = (String) handlerCtx.getInputValue("name");
            AttributeList list = new AttributeList();

//            Properties props = new Properties();
            Map addProps = (Map) handlerCtx.getInputValue("addProps");
            if (addProps != null) {
                Iterator additer = addProps.keySet().iterator();
                while (additer.hasNext()) {
                    Object key = additer.next();
//                    String addvalue = (String) addProps.get(key);
//                    props.put(key, addvalue);
                    list.add(new Attribute((String)key, (String) addProps.get(key)));
                }
            }

            String[] types = 
                    new String[]{"java.lang.String", "java.lang.String", "javax.management.AttributeList"};
                    //new String[]{"javax.management.AttributeList", "java.util.Properties", "java.lang.String"};
            Object[] params = 
                    new Object[]{type, name, list};
                    //new Object[]{list, props, configName};

            Object obj = JMXUtil.invoke(JMS_OBJECT_NAME, OP_CREATE, params, types);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }

    }

    @Handler(id="deleteJMSDest",
    input={
        @HandlerInput(name="selectedRows", type=List.class, required=true)}
//        @HandlerInput(name="targetName", type=String.class, required=true)}
    )
    public static void deleteJMSDest(HandlerContext handlerCtx) {
//        String configName = ((String) handlerCtx.getInputValue("targetName"));
        List obj = (List) handlerCtx.getInputValue("selectedRows");
        List<Map> selectedRows = (List) obj;
        try{
            for(Map oneRow : selectedRows){
                String name = (String)oneRow.get("name");
                String type = ((String)oneRow.get("type")).substring(0,1).toLowerCase();
                Object[] params = new Object[]{type, name};
                String[] types = new String[]{"java.lang.String","java.lang.String"};
                JMXUtil.invoke(JMS_OBJECT_NAME, OP_DESTROY, params, types);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    public static boolean isSelected(String name, List<Map> selectedList){
        if(selectedList == null || name == null) return false;
        for(Map oneRow : selectedList){
            if(name.equals(oneRow.get("name"))){
                return true;
            }
        }
        return false;
    }
}