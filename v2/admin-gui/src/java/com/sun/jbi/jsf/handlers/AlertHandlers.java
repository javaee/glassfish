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

package com.sun.jbi.jsf.handlers;

import com.sun.enterprise.tools.admingui.util.GuiUtil;
import com.sun.jbi.jsf.bean.AlertBean;
import com.sun.jbi.jsf.util.AlertUtilities;
import com.sun.jbi.jsf.util.BeanUtilities;
import com.sun.jbi.jsf.util.ClusterUtilities;
import com.sun.jbi.jsf.util.I18nUtilities;
import com.sun.jbi.jsf.util.JBILogger;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import java.util.logging.Logger;


/**
 * Provides jsftemplating handlers for showing/discarding Alerts
 */
public class AlertHandlers
{
    //Get Logger to log fine mesages for debugging
	private static Logger sLog = JBILogger.getInstance();


    /**
     * <p> Decreases the number of alerts to be displayed if an alert is to be displayed
     * <p> Input  value: "isAlertNeeded" -- Type: <code> Boolean</code></p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiDecrementAlertCountIfNeeded",
	     input={
		 @HandlerInput(name="isAlertNeeded", type=Boolean.class, required=true)} )
	
        public static void jbiDecrementAlertCountIfNeeded(HandlerContext handlerCtx)
    {
        Boolean isAlertNeeded = (Boolean)
            handlerCtx.getInputValue("isAlertNeeded");

	AlertBean alertBean = BeanUtilities.getAlertBean();
	int alertCount = alertBean.getAlertCount();

	if ((null != isAlertNeeded)
	    && (isAlertNeeded))
	    {
		--alertCount;
		alertBean.setAlertCount(alertCount);
	    }
	
	sLog.fine("AlertHandlers.jbiDecrementAlertCountIfNeeded(...), " +
					" isAlertNeeded=" + isAlertNeeded +
					", alertBean.getAlertCount()=" + alertCount); 
	
    }

    /**
     * <p> Sets a Defaults or Target Loaded alert
     * <p> Input  value: "defaultsOrTarget" -- Type: <code> java.lang.String</code>
     * <p> Input  value: "clusterOrPe" -- Type: <code> java.lang.String</code>
     * <p> Input  value: "target" -- Type: <code> java.lang.String</code>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiSetDefaultsOrTargetLoadedAlert",
             input={
		 @HandlerInput(name="defaultsOrTarget", type=String.class, required=true),
		 @HandlerInput(name="clusterOrPe", type=String.class, required=true),
		 @HandlerInput(name="target", type=String.class, required=true)})
        public static void jbiSetDefaultsOrTargetLoadedAlert(HandlerContext handlerCtx)
    {
        String defaultsOrTarget = (String)handlerCtx.getInputValue("defaultsOrTarget");
        String clusterOrPe = (String)handlerCtx.getInputValue("clusterOrPe");
        String target    = (String)handlerCtx.getInputValue("target");

	sLog.fine("AlertHandlers.jbiSetDefaultsOrTargetLoadedAlert(), defaultsOrTarget =" +
		  defaultsOrTarget + ", clusterOrPe=" + clusterOrPe + ", target=" + target);

	AlertBean alertBean = BeanUtilities.getAlertBean();

	Object[] args = {target};

	String alertType = "info";
	String alertSummary = 
	    GuiUtil.getMessage(I18nUtilities
			       .getResourceString("jbi.root.configuration." +
						  defaultsOrTarget + "." +
						  clusterOrPe + ".loaded.summary.message"), 
			       args);
	String alertDetail = I18nUtilities
	    .getResourceString("jbi.root.configuration." + 			       
			       defaultsOrTarget + "." +
			       clusterOrPe + ".loaded.detail.message");

	alertBean.setAlertType(alertType);
	alertBean.setAlertSummary(alertSummary);
	alertBean.setAlertDetail(alertDetail);

	sLog.fine("AlertHandlers.jbiSetDefaultsOrTargetLoadedAlert(), alertType=" + alertType +
		  ", alertSummary=" + alertSummary +
		  ", alertDetail=" + alertDetail);
    }



    /**
     * <p> Increases the number of alerts to be displayed if an alert is to be displayed
     * <p> Input  value: "isAlertNeeded" -- Type: <code> Boolean</code></p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiIncrementAlertCountIfNeeded",
	     input={
		 @HandlerInput(name="isAlertNeeded", type=Boolean.class, required=true)} )
	
        public static void jbiIncrementAlertCountIfNeeded(HandlerContext handlerCtx)
    {
        Boolean isAlertNeeded = (Boolean)
            handlerCtx.getInputValue("isAlertNeeded");
	
	AlertBean alertBean = BeanUtilities.getAlertBean();
	int alertCount = alertBean.getAlertCount();

	if ((null != isAlertNeeded)
	    && (isAlertNeeded))
	    {
		++alertCount;
		alertBean.setAlertCount(alertCount);
	    }
	
	sLog.fine("AlertHandlers.jbiIncrementAlertCountIfNeeded(...), " +
					" isAlertNeeded=" + isAlertNeeded +
					", alertBean.getAlertCount()=" + alertCount); 
	
    }

    /**
     * <p> Returns true if an alert still needs to be displayed
     * <p> Output  value: "isAlertNeeded" -- Type: <code> Boolean</code></p>
     * @param  handlerCtx <code>HandlerContext</code> provides inputs and outputs.
     */
    @Handler(id="jbiIsAlertNeeded",
	     output={
		 @HandlerOutput(name="isAlertNeeded", type=Boolean.class) } )
	
        public static void jbiIsAlertNeeded(HandlerContext handlerCtx)
    {
	AlertBean alertBean = BeanUtilities.getAlertBean();

	int alertCount = alertBean.getAlertCount();

	boolean isAlertNeeded = (0 < alertCount);
 
	handlerCtx.setOutputValue ("isAlertNeeded", isAlertNeeded);

	sLog.fine("AlertHandlers.jbiIsAlertNeeded(...), " +
					" isAlertNeeded=" + isAlertNeeded +
					", alertBean.getAlertCount()=" + alertCount); 
	
    }

}
