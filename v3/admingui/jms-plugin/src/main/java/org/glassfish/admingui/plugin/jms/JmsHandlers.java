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
import java.util.List;
import java.util.Map;
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
    protected static final String OBJECT_DEST_MGR = "com.sun.messaging.jms.server:type=DestinationManager,subtype=Config";
    protected static final String OBJECT_DEST_BASE = "com.sun.messaging.jms.server:type=Destination";
    protected static final String SUBTYPE_CONFIG = "Config";
    protected static final String SUBTYPE_MONITOR = "Monitor";

    protected static final String OP_LIST_DESTINATIONS = "getDestinations";
    protected static final String OP_CREATE = "create";
    protected static final String OP_DESTROY = "destroy";
    protected static final String OP_PURGE = "purge";

    protected static final String ATTR_CONSUMER_FLOW_LIMIT = "ConsumerFlowLimit";
    protected static final String ATTR_LIMIT_BEHAVIOR = "LimitBehavior";
    protected static final String ATTR_LOCAL_DELIVERY_PREFERRED = "LocalDeliveryPreferred";
    protected static final String ATTR_MAX_BYTES_PER_MSG = "MaxBytesPerMsg";
    protected static final String ATTR_MAX_NUM_ACTIVE_CONSUMERS = "MaxNumActiveConsumers";
    protected static final String ATTR_MAX_NUM_BACKUP_CONSUMERS = "MaxNumBackupConsumers";
    protected static final String ATTR_MAX_NUM_PRODUCERS = "MaxNumProducers";
    protected static final String ATTR_USE_DMQ = "UseDMQ";
    protected static final String ATTR_MAX_NUM_MSGS = "MaxNumMsgs";
    protected static final String ATTR_MAX_TOTAL_MSG_BYTES = "MaxTotalMsgBytes";
    protected static final String ATTR_VALIDATE_XML_SCHEMA_ENABLED = "ValidateXMLSchemaEnabled";
    protected static final String ATTR_XML_SCHEMA_URI_LIST = "XMLSchemaURIList";

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
            results = (ObjectName[]) mbs.invoke(new ObjectName(OBJECT_DEST_MGR), OP_LIST_DESTINATIONS, new Object[]{}, new String[]{});
        } catch (Exception ex) {
            Logger.getLogger(JmsHandlers.class.getName()).log(Level.SEVERE, null, ex);
        }
        GuiUtil.getLogger().info("***** result = " + results[0].toString());
    }

    @Handler(id="getPhysicalDestination",
        input={
            @HandlerInput(name="name", type=String.class, required=true),
            @HandlerInput(name="type", type=String.class, required=true)},
        output={
            @HandlerOutput(name="destData", type=java.util.Map.class)}
     )
    public static void getPhysicalDestination(HandlerContext handlerCtx){
        String name = (String)handlerCtx.getInputValue("name");
        String type = (String)handlerCtx.getInputValue("type");
        Map valueMap = new HashMap();
        try {
            String objectName = getJmsDestinationObjectName(SUBTYPE_CONFIG, name, type);
            AttributeList attributes = (AttributeList)JMXUtil.getMBeanServer().getAttributes(
                new ObjectName(objectName),
                new String[]{ATTR_MAX_NUM_MSGS, ATTR_MAX_BYTES_PER_MSG, ATTR_MAX_TOTAL_MSG_BYTES, ATTR_LIMIT_BEHAVIOR,
                    ATTR_MAX_NUM_PRODUCERS, ATTR_MAX_NUM_ACTIVE_CONSUMERS, ATTR_MAX_NUM_BACKUP_CONSUMERS, ATTR_CONSUMER_FLOW_LIMIT,
                    ATTR_LOCAL_DELIVERY_PREFERRED, ATTR_USE_DMQ, ATTR_VALIDATE_XML_SCHEMA_ENABLED, ATTR_XML_SCHEMA_URI_LIST});
            for (Attribute attribute: attributes.asList()) {
                valueMap.put(attribute.getName(), (attribute.getValue() != null) ? attribute.getValue().toString() : null);
            }

            handlerCtx.setOutputValue("destData", valueMap);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }


        handlerCtx.setOutputValue("destData", valueMap);
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
                    OBJECT_DEST_MGR, OP_LIST_DESTINATIONS);

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
               System.out.println("invoke:   " + OBJECT_DEST_MGR + ", method  =  " + OP_LIST_DESTINATIONS);
               GuiUtil.handleException(handlerCtx, ex);
           }
           handlerCtx.setOutputValue("result", result);
    }

    /**
     *	<p>This handler creates a physical destination.</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "createPhysicalDestination",
    input = {
        @HandlerInput(name = "name", type = String.class, required = true),
        @HandlerInput(name = "attributes", type = Map.class, required = true),
        @HandlerInput(name = "type", type = String.class)})
    public static void createPhysicalDestination(HandlerContext handlerCtx) {
        try {
            final String type = (String) handlerCtx.getInputValue("type");
            final String name = (String) handlerCtx.getInputValue("name");
            AttributeList list = new AttributeList();

            // Copy attributes to the AttributeList.
            // Make it work, then make it right. :|
            Map attrMap = (Map) handlerCtx.getInputValue("attributes");
            buildAttributeList(list, attrMap, type);

            String[] types = new String[]{"java.lang.String", "java.lang.String", "javax.management.AttributeList"};
            Object[] params = new Object[]{type, name, list};

            Object obj = JMXUtil.invoke(OBJECT_DEST_MGR, OP_CREATE, params, types);
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p>This handler updates a physical destination.</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "updatePhysicalDestination",
    input = {
        @HandlerInput(name = "name", type = String.class, required = true),
        @HandlerInput(name = "attributes", type = Map.class, required = true),
        @HandlerInput(name = "type", type = String.class)})
    public static void updatePhysicalDestination(HandlerContext handlerCtx) {
        try {
            final String type = (String) handlerCtx.getInputValue("type");
            final String name = (String) handlerCtx.getInputValue("name");
            AttributeList list = new AttributeList();

            // Copy attributes to the AttributeList.
            // Make it work, then make it right. :|
            Map attrMap = (Map) handlerCtx.getInputValue("attributes");
            buildAttributeList(list, attrMap, type);

            String objectName = getJmsDestinationObjectName(SUBTYPE_CONFIG, name, type);
            JMXUtil.getMBeanServer().setAttributes(new ObjectName(objectName), list);
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
                JMXUtil.invoke(OBJECT_DEST_MGR, OP_DESTROY, params, types);
            }
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler takes in selected rows, and removes selected config
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "flushJMSDestination",
    input = {
        @HandlerInput(name = "selectedRows", type = List.class, required = true)})
    public static void flushJMSDestination(HandlerContext handlerCtx) {
        List<Map> selectedRows = (List) handlerCtx.getInputValue("selectedRows");
        try {
            for (Map oneRow : selectedRows) {
                String name = (String) oneRow.get("name");
                String type = ((String)oneRow.get("type"));
                JMXUtil.invoke(getJmsDestinationObjectName(SUBTYPE_CONFIG, name, type), OP_PURGE);
            }
        } catch (Exception ex) {
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    /**
     *	<p> This handler returns a map populated with the default values for a
     * destination.  Currently, this is all hard-coded, based on data from the MQ
     * documentation.  When/if they expose an API for determining this programmatically,
     * the implementation will be updated.</p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id = "getDefaultPhysicalDestinationValues",
        output = {
            @HandlerOutput(name = "map", type = Map.class)
    })
    public static void getDefaultPhysicalDestinationValues(HandlerContext handlerCtx) {
        Map map = new HashMap();
        map.put(ATTR_MAX_NUM_MSGS, "-1");
        map.put(ATTR_MAX_BYTES_PER_MSG, "-1");
        map.put(ATTR_MAX_TOTAL_MSG_BYTES, "-1");
        map.put(ATTR_LIMIT_BEHAVIOR,"REJECT_NEWEST");
        map.put(ATTR_MAX_NUM_PRODUCERS, "100");
        map.put(ATTR_MAX_NUM_ACTIVE_CONSUMERS, "-1");
        map.put(ATTR_MAX_NUM_BACKUP_CONSUMERS, "0");
        map.put(ATTR_CONSUMER_FLOW_LIMIT, "1000");
        map.put(ATTR_LOCAL_DELIVERY_PREFERRED, "false");
        map.put(ATTR_USE_DMQ, "true");
        map.put(ATTR_VALIDATE_XML_SCHEMA_ENABLED, "false");
        map.put(ATTR_XML_SCHEMA_URI_LIST, "");

        handlerCtx.setOutputValue("map", map);
    }

    protected static String getJmsDestinationObjectName(String objectType, String name, String destType) {
        return OBJECT_DEST_BASE+",subtype="+objectType+",desttype="+destType.substring(0,1).toLowerCase()+",name=\""+name+"\"";
    }

    protected static void buildAttributeList(AttributeList list, Map attrMap, String type) {
        list.add(new Attribute(ATTR_MAX_NUM_MSGS, Long.parseLong((String) attrMap.get(ATTR_MAX_NUM_MSGS))));
        list.add(new Attribute(ATTR_MAX_BYTES_PER_MSG, Long.parseLong((String) attrMap.get(ATTR_MAX_BYTES_PER_MSG))));
        list.add(new Attribute(ATTR_MAX_TOTAL_MSG_BYTES, Long.parseLong((String) attrMap.get(ATTR_MAX_TOTAL_MSG_BYTES))));
        list.add(new Attribute(ATTR_LIMIT_BEHAVIOR, (String) attrMap.get(ATTR_LIMIT_BEHAVIOR)));
        list.add(new Attribute(ATTR_MAX_NUM_PRODUCERS, Integer.parseInt((String) attrMap.get(ATTR_MAX_NUM_PRODUCERS))));
        if ("queue".equals(type)) {
            list.add(new Attribute(ATTR_MAX_NUM_ACTIVE_CONSUMERS, Integer.parseInt((String) attrMap.get(ATTR_MAX_NUM_ACTIVE_CONSUMERS))));
            list.add(new Attribute(ATTR_MAX_NUM_BACKUP_CONSUMERS, Integer.parseInt((String) attrMap.get(ATTR_MAX_NUM_BACKUP_CONSUMERS))));
            list.add(new Attribute(ATTR_LOCAL_DELIVERY_PREFERRED, Boolean.valueOf((String) attrMap.get(ATTR_LOCAL_DELIVERY_PREFERRED))));
        }
        list.add(new Attribute(ATTR_CONSUMER_FLOW_LIMIT, Long.parseLong((String) attrMap.get(ATTR_CONSUMER_FLOW_LIMIT))));
        list.add(new Attribute(ATTR_USE_DMQ, Boolean.valueOf((String) attrMap.get(ATTR_USE_DMQ))));
        list.add(new Attribute(ATTR_VALIDATE_XML_SCHEMA_ENABLED, Boolean.valueOf((String) attrMap.get(ATTR_VALIDATE_XML_SCHEMA_ENABLED))));
        list.add(new Attribute(ATTR_XML_SCHEMA_URI_LIST, (String) attrMap.get(ATTR_XML_SCHEMA_URI_LIST)));
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