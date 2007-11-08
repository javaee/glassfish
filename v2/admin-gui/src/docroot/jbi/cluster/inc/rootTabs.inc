<!--
#
# The contents of this file are subject to the terms 
# of the Common Development and Distribution License 
# (the License).  You may not use this file except in
# compliance with the License.
# 
# You can obtain a copy of the license at 
# https://glassfish.dev.java.net/public/CDDLv1.0.html or
# glassfish/bootstrap/legal/CDDLv1.0.txt.
# See the License for the specific language governing 
# permissions and limitations under the License.
# 
# When distributing Covered Code, include this CDDL 
# Header Notice in each file and include the License file 
# at glassfish/bootstrap/legal/CDDLv1.0.txt.  
# If applicable, add the following below the CDDL Header, 
# with the fields enclosed by brackets [] replaced by
# you own identifying information: 
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
#
-->
<!-- jbi/cluster/inc/rootTabs.inc -->

<sun:tabSet id="rootTabs" immediate="$boolean{false}" selected="#{sessionScope.jbiRootTabs}">

    <sun:tab id="rootGeneralTab" immediate="$boolean{false}" text="$resource{i18n.jbi.root.general.tab}" >
        <!command
setSessionAttribute(key="jbiRootTabs" value="rootGeneralTab");
redirect(page="root.jsf");
        />       
    </sun:tab>

    <sun:tab id="rootConfigurationTab" immediate="$boolean{false}" text="$resource{i18n.jbi.root.configuration.tab}">
        <!command
setSessionAttribute(key="jbiRootTabs" value="rootConfigurationTab");
redirect(page="rootConfiguration.jsf");
        />       
    </sun:tab>

    <sun:tab id="rootLoggingTab" 
        immediate="$boolean{false}" 
        rendered='false'
        text="$resource{i18n.jbi.root.logging.tab}"
        >
        <!command
setSessionAttribute(key="jbiRootTabs" value="rootLoggingTab");
redirect(page="rootLogging.jsf");
        />       
    </sun:tab>

    <sun:tab id="rootMonitoringTab" 
        immediate="$boolean{false}" 
        rendered='false'
        text="$resource{i18n.jbi.root.monitoring.tab}"
        >
        <!command
setSessionAttribute(key="jbiRootTabs" value="rootMonitoringTab");
redirect(page="rootMonitoring.jsf");
        />       
    </sun:tab>

</sun:tabSet>



