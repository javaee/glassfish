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

public class WeldApplication extends Application {
   
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

    @Override
    public ActionListener getActionListener() {
        return delegate().getActionListener();
    }

    @Override
    public void setActionListener(ActionListener listener) {
        delegate().setActionListener(listener);
    }
 
    @Override
    public void setDefaultLocale(Locale locale) {
        delegate().setDefaultLocale(locale);
    }

    @Override
    public Locale getDefaultLocale() {
        return delegate().getDefaultLocale();
    }

    @Override
    public void setDefaultRenderKitId(String renderKitId) {
        delegate().setDefaultRenderKitId(renderKitId);
    }

    @Override
    public String getDefaultRenderKitId() {
        return delegate().getDefaultRenderKitId();
    }

    @Override
    public void setMessageBundle(String bundle) {
        delegate().setMessageBundle(bundle);
    }

    @Override
    public String getMessageBundle() {
        return delegate().getMessageBundle();
    }

    @Override
    public void setNavigationHandler(NavigationHandler handler) {
        delegate().setNavigationHandler(handler);
    }

    @Override
    public NavigationHandler getNavigationHandler() {
        return delegate().getNavigationHandler();
    }

    @Override
    public void setResourceHandler(ResourceHandler handler) {
        delegate().setResourceHandler(handler);
    }

    @Override
    public ResourceHandler getResourceHandler() {
        return delegate().getResourceHandler();
    }

    @Override
    public void setPropertyResolver(PropertyResolver resolver) {
        delegate().setPropertyResolver(resolver);
    }

    @Override
    public PropertyResolver getPropertyResolver() {
        return delegate().getPropertyResolver();
    }

    @Override
    public ResourceBundle getResourceBundle(FacesContext ctx, String name) {
        return delegate().getResourceBundle(ctx, name);
    }

    @Override
    public ValueBinding createValueBinding(String ref) {
        return delegate().createValueBinding(ref);
    }

    @Override
    public Iterator<String> getValidatorIds() {
        return delegate().getValidatorIds();
    }

    @Override
    public Validator createValidator(String validatorId) throws FacesException {
        return delegate().createValidator(validatorId);
    }

    @Override
    public void addValidator(String validatorId, String validatorClass) {
        delegate().addValidator(validatorId, validatorClass);
    }

    @Override
    public void setSupportedLocales(Collection<Locale> locales) {
        delegate().setSupportedLocales(locales);
    }

    @Override
    public Iterator<Locale> getSupportedLocales() {
        return delegate().getSupportedLocales();
    }

    @Override
    public MethodBinding createMethodBinding(String ref, Class<?> params[])
        throws ReferenceSyntaxException {
        return delegate().createMethodBinding(ref, params);
    } 

    @Override
    public Iterator<Class<?>> getConverterTypes() {
        return delegate().getConverterTypes();
    }

    @Override
    public Iterator<String> getConverterIds() {
        return delegate().getConverterIds();
    }

    @Override
    public Converter createConverter(Class<?> targetClass) {
        return delegate().createConverter(targetClass);
    }

    @Override
    public Converter createConverter(String converterId) {
        return delegate().createConverter(converterId);
    }

    @Override
    public void addConverter(Class<?> targetClass, String converterClass) {
        delegate().addConverter(targetClass, converterClass);
    }

    @Override
    public void addConverter(String converterId, String converterClass) {
        delegate().addConverter(converterId, converterClass);
    }

    @Override
    public Iterator<String> getComponentTypes() {
        return delegate().getComponentTypes();
    }

    @Override
    public UIComponent createComponent(ValueBinding binding,
        FacesContext ctx, String componentType) throws FacesException {
        return delegate().createComponent(binding, ctx, componentType);
    }

    @Override
    public UIComponent createComponent(String componentType) throws FacesException {
        return delegate().createComponent(componentType);
    }

    @Override
    public void addComponent(String componentType, String componentClass) {
        delegate().addComponent(componentType, componentClass);
    }

    @Override
    public void setStateManager(StateManager manager) {
        delegate().setStateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return delegate().getStateManager();
    }

    @Override
    public void setViewHandler(ViewHandler vHandler) {
        delegate().setViewHandler(vHandler);
    }

    @Override
    public ViewHandler getViewHandler() {
        return delegate().getViewHandler();
    }

    @Override
    public void setVariableResolver(VariableResolver resolver) {
        delegate().setVariableResolver(resolver);
    }

    @Override
    public VariableResolver getVariableResolver() {
        return delegate().getVariableResolver();
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        if (this.expressionFactory == null) {
            BeanManager beanManager = getBeanManager();
            if (beanManager != null) {
                this.expressionFactory = beanManager.wrapExpressionFactory(delegate().getExpressionFactory());
          } else {
              this.expressionFactory = delegate().getExpressionFactory(); 
          }
        }
        return expressionFactory;
    }

    private Application delegate() {
        return application;
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
