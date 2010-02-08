/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.weld.jsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.el.ELContextListener;
import javax.el.ExpressionFactory;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ResourceHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.weld.util.Util;

import org.jboss.weld.el.WeldELContextListener;
import org.jboss.weld.el.WeldExpressionFactory;

public class WeldApplication extends ApplicationWrapper {
   
    private static final ELContextListener[] EMPTY_LISTENERS = {};
   
    private final Application application;
    private ExpressionFactory expressionFactory;
   
    public WeldApplication(Application application) {
        this.application = application;
        BeanManager beanManager = getBeanManager();
        if (beanManager != null) {
            application.addELContextListener(Util.<ELContextListener>newInstance(
                "org.jboss.weld.el.WeldELContextListener"));
            application.addELResolver(beanManager.getELResolver());
        }
    }

    public Application getWrapped() {
        return this.application;
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        if (this.expressionFactory == null) {
            BeanManager beanManager = getBeanManager();
            if (beanManager != null) {
                this.expressionFactory = beanManager.wrapExpressionFactory(getWrapped().getExpressionFactory());
          } else {
              this.expressionFactory = getWrapped().getExpressionFactory(); 
          }
        }
        return expressionFactory;
    }

    private BeanManager getBeanManager() {
        try {
            InitialContext context = new InitialContext();
            return (BeanManager) context.lookup("java:comp/BeanManager");
        } catch (NamingException e) {
            return null;
        }

    }
}
