/*
 * Copyright 2005-2010 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.javaee.blueprints.components.ui.components;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.render.ResponseStateManager;

/**
 * Component wrapping a {@link Graph} object that is pointed at by the
 * a value binding reference expression.  This component supports
 * the processing of a {@link ActionEvent} that will toggle the expanded
 * state of the specified {@link Node} in the {@link Graph}.
 */

public class GraphComponent extends UICommand {
    
    /**
     * <p>The standard component family for this component.</p>
     */
    public static final String COMPONENT_FAMILY = "Graph";
    
    public GraphComponent() {
        
        // set a default actionListener to expand or collapse a node
        // when a node is clicked.
        // add listener only if its an initial request. If its a postback
        // the listener will be persisted by the state saving mechanism, so
        // we don't want to accumulate the listeners.
        FacesContext context = FacesContext.getCurrentInstance();
        // Is it better to use FacesContext context = getFacesContext();
        Application application = context.getApplication();
        String renderkitId = application.getViewHandler().
                calculateRenderKitId(context);
        ResponseStateManager rsm = this.getResponseStateManager(context,
                renderkitId);
        if (!rsm.isPostback(context)) {
            
            ExpressionFactory exprFactory = application.getExpressionFactory();
            MethodExpression actionListener =
                    exprFactory.createMethodExpression(
                    context.getELContext(),
                    "#{GraphBean.processGraphEvent}",
                    Void.class,
                    new Class[]{ActionEvent.class});
                    addActionListener(
                            new MethodExpressionActionListener(actionListener));
                    
        } 
        
    }
    
    
    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {
        return (COMPONENT_FAMILY);
    }
    
    protected ResponseStateManager getResponseStateManager(FacesContext context,
            String renderKitId){
        
        RenderKitFactory renderKitFactory = (RenderKitFactory)
        FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        
        RenderKit renderKit = renderKitFactory.getRenderKit(context, renderKitId);
        
        if ( renderKit != null) {
            return renderKit.getResponseStateManager();
        }
        return null;
    }
    
}
