/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admingui.handlers;

import com.sun.appserv.management.config.TransactionServiceConfig;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.PropertyConfig;
import java.util.Map;
import org.glassfish.admingui.common.util.AMXRoot;
import org.glassfish.admingui.common.util.AMXUtil;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.HashMap;

/**
 *
 * @author anilam
 */
public class TransactionServiceHandlers {


/**
     *	<p> This handler returns the values for all the attributes in
     *      Transaction Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "OnRestart"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Retry"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogLocation"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Heuristic"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "KeyPoint"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"  -- Type: <code>java.util.Map</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getTransactionServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="OnRestart",      type=Boolean.class),
        @HandlerOutput(name="Timeout",  type=String.class),
        @HandlerOutput(name="Retry",    type=String.class),
        @HandlerOutput(name="LogLocation",       type=String.class),
        @HandlerOutput(name="Heuristic", type=String.class),
        @HandlerOutput(name="KeyPoint", type=String.class),
        @HandlerOutput(name="Properties", type=Map.class)})

        public static void getTransactionServiceSettings(HandlerContext handlerCtx) {



        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        TransactionServiceConfig tConfig = config.getTransactionServiceConfig();
        String onrestart = tConfig.getAutomaticRecovery();
        String timeout = tConfig.getTimeoutInSeconds();
        String retry = tConfig.getRetryTimeoutInSeconds();
        String loglocation = tConfig.getTxLogDir();
        String heuristic = tConfig.getHeuristicDecision();
        String keypoint = tConfig.getKeypointInterval();
        Map<String, PropertyConfig> props = new HashMap();
        props = tConfig.getPropertyConfigMap();
        handlerCtx.setOutputValue("OnRestart", onrestart);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Retry", retry);
        handlerCtx.setOutputValue("LogLocation", loglocation);
        handlerCtx.setOutputValue("Heuristic", heuristic);
        handlerCtx.setOutputValue("KeyPoint", keypoint);
        handlerCtx.setOutputValue("Properties", props);

    }

/**
     *	<p> This handler saves the values for all the attributes in
     *      Transaction Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Input value: "OnRestart"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Input value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Retry"     -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "LogLocation"        -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "Heuristic"  -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "KeyPoint"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="saveTransactionServiceSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="OnRestart",      type=String.class),
        @HandlerInput(name="Timeout",  type=String.class),
        @HandlerInput(name="Retry",    type=String.class),
        @HandlerInput(name="LogLocation",       type=String.class),
        @HandlerInput(name="Heuristic", type=String.class),
        @HandlerInput(name="KeyPoint", type=String.class),
        @HandlerInput(name="newProps", type=Map.class)})

        public static void saveTransactionServiceSettings(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        TransactionServiceConfig tConfig = config.getTransactionServiceConfig();
        AMXUtil.updateProperties( tConfig, (Map)handlerCtx.getInputValue("newProps"));
        tConfig.setAutomaticRecovery((String)handlerCtx.getInputValue("OnRestart"));
        tConfig.setTimeoutInSeconds(((String)handlerCtx.getInputValue("Timeout")));
        tConfig.setRetryTimeoutInSeconds(((String)handlerCtx.getInputValue("Retry")));
        tConfig.setTxLogDir(((String)handlerCtx.getInputValue("LogLocation")));
        tConfig.setHeuristicDecision(((String)handlerCtx.getInputValue("Heuristic")));
        tConfig.setKeypointInterval(((String)handlerCtx.getInputValue("KeyPoint")));
        AMXUtil.updateProperties( tConfig, (Map)handlerCtx.getInputValue("newProps"));
    }

/**
     *	<p> This handler returns the default values for all the attributes in
     *      Transaction Service Page </p>
     *	<p> Input value: "ConfigName"       -- Type: <code>java.lang.String</code></p>
     *	<p> Output value: "OnRestart"       -- Type: <code>java.lang.Boolean</code></p>
     *  <p> Output value: "Timeout"   -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Retry"     -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "LogLocation"        -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Heuristic"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "KeyPoint"  -- Type: <code>java.lang.String</code></p>
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getTransactionServiceDefaultSettings",
   input={
        @HandlerInput(name="ConfigName", type=String.class, required=true)   },
    output={
        @HandlerOutput(name="OnRestart",      type=Boolean.class),
        @HandlerOutput(name="Timeout",  type=String.class),
        @HandlerOutput(name="Retry",    type=String.class),
        @HandlerOutput(name="LogLocation",       type=String.class),
        @HandlerOutput(name="Heuristic", type=String.class),
        @HandlerOutput(name="KeyPoint", type=String.class)})

        public static void getTransactionServiceDefaultSettings(HandlerContext handlerCtx) {

        ConfigConfig config = AMXRoot.getInstance().getConfig(((String)handlerCtx.getInputValue("ConfigName")));
        TransactionServiceConfig tConfig = config.getTransactionServiceConfig();
        String onrestart = tConfig.getDefaultValue("AutomaticRecovery");
        String timeout = tConfig.getDefaultValue("TimeoutInSeconds");
        String retry = tConfig.getDefaultValue("RetryTimeoutInSeconds");
        String loglocation = tConfig.getDefaultValue("TxLogDir");
        String heuristic = tConfig.getDefaultValue("HeuristicDecision");
        String keypoint = tConfig.getDefaultValue("KeypointInterval");
        handlerCtx.setOutputValue("OnRestart", onrestart);
        handlerCtx.setOutputValue("Timeout", timeout);
        handlerCtx.setOutputValue("Retry", retry);
        handlerCtx.setOutputValue("LogLocation", loglocation);
        handlerCtx.setOutputValue("Heuristic", heuristic);
        handlerCtx.setOutputValue("KeyPoint", keypoint);

    }
}
