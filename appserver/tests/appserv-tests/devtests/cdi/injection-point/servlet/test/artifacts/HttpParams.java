/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package test.artifacts;

import java.util.Enumeration;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.servlet.ServletRequest;

@RequestScoped
public class HttpParams {
    //A thread local variable to house the servlet request. This is set by the
    //servlet filter. This is not a recommended way to get hold of HttpServletRequest 
    //in a CDI bean but a workaround that is described in
    //http://www.seamframework.org/Community/HowToReachHttpServletRequestAndHttpServletResponseFromBean
    //The right approach is to use JSF's FacesContext as described in
    //https://docs.jboss.org/weld/reference/snapshot/en-US/html/injection.html#d0e1635
    public static ThreadLocal<ServletRequest> sr = new ThreadLocal<ServletRequest>();
    
    @Produces
    @HttpParam("")
    String getParamValue(InjectionPoint ip) {
        ServletRequest req = sr.get();
        String parameterName = ip.getAnnotated().getAnnotation(HttpParam.class).value();
        if (parameterName.trim().equals("")) parameterName = ip.getMember().getName();
        return req.getParameter(parameterName);
    }

}
