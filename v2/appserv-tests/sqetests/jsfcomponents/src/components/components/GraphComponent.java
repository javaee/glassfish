/*
 * $Id: GraphComponent.java,v 1.1 2005/11/03 02:59:49 SherryShen Exp $
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package components.components;


import components.model.Graph;
import components.model.Node;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * Component wrapping a {@link Graph} object that is pointed at by the
 * a value binding reference expression.  This component supports
 * the processing of a {@link ActionEvent} that will toggle the expanded
 * state of the specified {@link Node} in the {@link Graph}.
 */

public class GraphComponent extends UICommand {

    private static Log log = LogFactory.getLog(GraphComponent.class);


    public GraphComponent() {

        // set a default actionListener to expand or collapse a node
        // when a node is clicked.
        Class signature[] = {ActionEvent.class};
        setActionListener(FacesContext.getCurrentInstance().getApplication()
                          .createMethodBinding(
                              "#{GraphBean.processGraphEvent}",
                              signature));

    }


    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {

        return ("Graph");

    }

}
