<!--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 language governing permissions and limitations under the License.

 When distributing the software, include this License Header Notice in each
 file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 Sun designates this particular file as subject to the "Classpath" exception
 as provided by Sun in the GPL Version 2 section of the License file that
 accompanied this code.  If applicable, add the following below the License
 Header, with the fields enclosed by brackets [] replaced by your own
 identifying information: "Portions Copyrighted [year]
 [name of copyright owner]"

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
-->
<!initPage
    if (#{i18nBundle}) {
        setResourceBundle(key="i18n" bundle="#{i18nBundle}")
    }
    if (#{helpBundle}) {
        setResourceBundle(key="help" bundle="#{helpBundle}")
    }
/>
<sun:page>
#include "/shared/restart.inc"

<sun:html>
<!insert name="head">
    <sun:script url="/resource/js/adminjsf.js" />
    <sun:head title="#{pageTitle}">
        <!insert name="headExtra" />
    </sun:head>
</insert>
    <sun:body onLoad="#{bodyOnLoad}">
        <sun:form id="form">
            <!insert name="formExtra" />
            <sun:title id="topTitle" title="#{pageTitle}" helpText="#{helpText}">
                <!insert name="titleExtra" />

                <!-- Buttons  -->
                <!facet pageButtonsTop>
                    <sun:panelGroup id="topButtons">
                        <!insert name="pageButtonsTop" />
                    </sun:panelGroup> 
                </facet>
            </sun:title>

            <!insert name="content">
                "Content Goes Here
            </insert>

            <sun:title id="bottomTitle" rendered="#{useBottomTitle == 'true'}">
                <!-- Buttons  -->
                <!facet pageButtonsBottom>
                    <sun:panelGroup id="bottomButtons">
                        <!insert name="pageButtonsBottom" />
                    </sun:panelGroup>
                </facet>
            </sun:title>

            //<!insert name="helpkey" />
            <sun:hidden id="helpKey" value="#{helpKey}" />
        </sun:form>
    </sun:body>
    <!insert name="htmlExtra" />
</sun:html>
</sun:page>