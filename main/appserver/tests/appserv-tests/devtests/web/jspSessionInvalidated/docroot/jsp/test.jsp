<%--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
    or packager/legal/LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at packager/legal/LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

--%><%

    Object obj;
    
    // Invalidate the session to make sure this does not disturb the 
    // behavior of the following methods on PageContext:
    //     private int doGetAttributeScope(String name);
    //     private Object doFindAttribute(String name){
    //     private void doRemoveAttribute(String name){
    session.invalidate();
    
    try {
        // attr1 does not exist. findAttribute() invoked on all scopes and
        // must return a null value.
        obj = pageContext.findAttribute("attr1");
        if (obj != null) {
            out.println("ERROR: attr1 is not null on findAttribute()");            
        }  
        
        // attr1 does not exist. getAttributeScope() invoked on all scopes and
        // must return 0.
        int scope = pageContext.getAttributesScope("attr1");
       if (scope != 0) {
            out.println("ERROR: scope is not 0 on getAttributesScope()");            
        }                
        
        // set attr2 in application scope.
        // Make sure it gets removed properly.
        pageContext.setAttribute("attr2", "attr2", PageContext.APPLICATION_SCOPE);
        pageContext.removeAttribute("attr2");
        obj = pageContext.findAttribute("attr2");
        if (obj != null) {
            out.println("ERROR: attr2 is not null");            
        }        

        // Success!
        out.println("SUCCESS");
        
    } catch (IllegalStateException ex) {
        out.println("ERROR: invalidated session throws IllegalStateException when trying to find/remove attribute");
    }
%>
