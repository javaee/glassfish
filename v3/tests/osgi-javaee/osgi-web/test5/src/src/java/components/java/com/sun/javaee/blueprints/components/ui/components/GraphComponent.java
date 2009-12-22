/* Copyright 2005 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at:
 http://developer.sun.com/berkeley_license.html
 $Id: GraphComponent.java,v 1.4 2005/11/24 00:10:57 inder Exp $ */

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
