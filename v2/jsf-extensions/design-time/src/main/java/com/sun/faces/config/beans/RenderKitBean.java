/*
 * $Id: RenderKitBean.java,v 1.1 2005/09/20 21:11:29 edburns Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.faces.config.beans;


import com.sun.faces.util.ToolsUtil;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * <p>Configuration bean for <code>&lt;render-kit&gt; element.</p>
 */

public class RenderKitBean extends FeatureBean {


    private static final Logger logger = ToolsUtil.getLogger(ToolsUtil.FACES_LOGGER +
            ToolsUtil.BEANS_LOGGER);


    // -------------------------------------------------------------- Properties


    private String renderKitClass;
    public String getRenderKitClass() { return renderKitClass; }
    public void setRenderKitClass(String renderKitClass)
    { this.renderKitClass = renderKitClass; }


    private String renderKitId = "HTML_BASIC";
    public String getRenderKitId() { return renderKitId; }
    public void setRenderKitId(String renderKitId)
    { this.renderKitId = renderKitId; }


    // -------------------------------------------------- RendererHolder Methods


    // Key is family + rendererType
    private Map<String,RendererBean> renderers = new TreeMap<String, RendererBean>();


    public void addRenderer(RendererBean descriptor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "addRenderer(" +
                      descriptor.getComponentFamily() + "," +
                      descriptor.getRendererType() + ")");
        }
        renderers.put(descriptor.getComponentFamily() + "|" +
                      descriptor.getRendererType(), descriptor);
    }


    public RendererBean getRenderer(String componentFamily,
                                    String rendererType) {
        return (renderers.get
                (componentFamily + "|" + rendererType));
    }


    public RendererBean[] getRenderers() {
        RendererBean results[] = new RendererBean[renderers.size()];
        return (renderers.values().toArray(results));
    }


    public void removeRenderer(RendererBean descriptor) {
        renderers.remove(descriptor.getComponentFamily() + "|" +
                         descriptor.getRendererType());
    }


    // -------------------------------------------------------------- Extensions


    // ----------------------------------------------------------------- Methods


}
