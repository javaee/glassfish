
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
<!-- configuration/httpServiceTabs.jsf -->

<sun:tabSet id="httpServiceTabs" immediate="$boolean{true}" selected="#{sessionScope.httpServiceTabs}">
    <sun:tab id="httpService" immediate="$boolean{true}" text="$resource{i18n.http.HttpService}" >
    	    <!command
                setSessionAttribute(key="httpServiceTabs" value="httpService");
                redirect(page="#{request.contextPath}/web/configuration/httpService.jsf?configName=#{configName}");
	    />       
    </sun:tab>
    <sun:tab id="accessLog" immediate="$boolean{true}" text="$resource{i18n.httpTabs.AccessLog}" >    
            <!command
                setSessionAttribute(key="httpServiceTabs" value="accessLog");
		redirect(page="#{request.contextPath}/web/configuration/accessLog.jsf?configName=#{configName}");
	    />       
    </sun:tab>    
    <sun:tab id="requestProcessing" immediate="$boolean{true}" text="$resource{i18n.httpTabs.RequestProcessing}" >
    	    <!command
                setSessionAttribute(key="httpServiceTabs" value="requestProcessing");
		redirect(page="#{request.contextPath}/web/configuration/requestProcessing.jsf?configName=#{configName}");
	    />       
    </sun:tab>
    <sun:tab id="keepAlive" immediate="$boolean{true}" text="$resource{i18n.httpTabs.KeepAlive}" >
    	    <!command
                setSessionAttribute(key="httpServiceTabs" value="keepAlive");
		redirect(page="#{request.contextPath}/web/configuration/keepAlive.jsf?configName=#{configName}");
	    />       
    </sun:tab> 
   <sun:tab id="connectionPool" immediate="$boolean{true}" text="$resource{i18n.httpTabs.ConnectionPool}" >
    	    <!command
                setSessionAttribute(key="httpServiceTabs" value="connectionPool");
		redirect(page="#{request.contextPath}/web/configuration/connectionPool.jsf?configName=#{configName}");
	    />       
    </sun:tab>    
   <sun:tab id="httpProtocol" immediate="$boolean{true}" text="$resource{i18n.httpTabs.HttpProtocol}" >
    	    <!command
                setSessionAttribute(key="httpServiceTabs" value="httpProtocol");
		redirect(page="#{request.contextPath}/web/configuration/httpProtocol.jsf?configName=#{configName}");
	    />       
    </sun:tab>
   <sun:tab id="fileCache" immediate="$boolean{true}" text="$resource{i18n.httpTabs.FileCache}" >
    	    <!command
                setSessionAttribute(key="httpServiceTabs" value="fileCache");
		redirect(page="#{request.contextPath}/web/configuration/httpFileCache.jsf?configName=#{configName}");
	    />       
    </sun:tab>        
</sun:tabSet>
