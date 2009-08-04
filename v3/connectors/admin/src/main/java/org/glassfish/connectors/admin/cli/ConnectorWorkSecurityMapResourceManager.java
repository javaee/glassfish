package org.glassfish.connectors.admin.cli;


import org.glassfish.api.I18n;
import org.glassfish.resource.common.ResourceConstants;
import org.glassfish.resource.common.ResourceStatus;
import org.glassfish.admin.cli.resources.ResourceManager;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.*;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.beans.PropertyVetoException;


@Service(name = ResourceConstants.WORK_SECURITY_MAP)
@I18n("add.resources")
public class ConnectorWorkSecurityMapResourceManager implements ResourceManager {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ConnectorWorkSecurityMapResourceManager.class);
    String raName;
    Properties principalsMap;
    Properties groupsMap;
    String description;
    String mapName;

    public String getResourceType() {
        return ResourceConstants.WORK_SECURITY_MAP;
    }

    public ResourceStatus create(Resources resources, HashMap attrList, Properties props, Server targetServer)
            throws Exception {
        setParams(attrList);

        if (mapName == null) {
            String msg = localStrings.getLocalString(
                    "create.connector.work.security.map.noMapName",
                    "No mapname defined for connector work security map.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (raName == null) {
            String msg = localStrings.getLocalString(
                    "create.connector.work.security.map.noRaName",
                    "No raname defined for connector work security map.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (principalsMap == null && groupsMap == null) {
            String msg = localStrings.getLocalString(
                    "create.connector.work.security.map.noMap",
                    "No principalsmap or groupsmap defined for connector work security map.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (principalsMap != null && groupsMap != null) {
            String msg = localStrings.getLocalString(
                    "create.connector.work.security.map.specifyPrincipalsOrGroupsMap",
                    "A work-security-map can have either (any number of) group mapping  " +
                            "or (any number of) principals mapping but not both. Specify" +
                            "--principalsmap or --groupsmap.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof WorkSecurityMap) {
                if (((WorkSecurityMap) resource).getName().equals(mapName) &&
                        ((WorkSecurityMap) resource).getResourceAdapterName().equals(raName)) {
                    String msg = localStrings.getLocalString(
                            "create.connector.work.security.map.duplicate",
                            "A connector work security map named {0} for resource adapter {1} already exists.",
                            mapName, raName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
                }
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {
                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {

                    WorkSecurityMap workSecurityMap =
                            param.createChild(WorkSecurityMap.class);
                    workSecurityMap.setName(mapName);
                    workSecurityMap.setResourceAdapterName(raName);
                    if (principalsMap != null) {
                        for (Map.Entry e : principalsMap.entrySet()) {
                            PrincipalMap principalMap = workSecurityMap.createChild(PrincipalMap.class);
                            principalMap.setEisPrincipal((String) e.getKey());
                            principalMap.setMappedPrincipal((String) e.getValue());
                            workSecurityMap.getPrincipalMap().add(principalMap);
                        }
                    } else if (groupsMap != null) {
                        for (Map.Entry e : groupsMap.entrySet()) {
                            GroupMap groupMap = workSecurityMap.createChild(GroupMap.class);
                            groupMap.setEisGroup((String) e.getKey());
                            groupMap.setMappedGroup((String) e.getValue());
                            workSecurityMap.getGroupMap().add(groupMap);
                        }
                    }
                    param.getResources().add(workSecurityMap);
                    return workSecurityMap;
                }
            }, resources);
            String msg = localStrings.getLocalString(
                    "create.work.security.map.success",
                    "Work security map {0} created.", mapName);
            return new ResourceStatus(ResourceStatus.SUCCESS, msg, true);
        } catch (TransactionFailure tfe) {
            String msg = localStrings.getLocalString(
                    "create.connector.work.security.map.fail",
                    "Unable to create connector work security map {0}.", mapName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }
    }

    private void setParams(HashMap attrList) {
        raName = (String) attrList.get(ResourceConstants.WORK_SECURITY_MAP_RA_NAME);
        mapName = (String) attrList.get(ResourceConstants.WORK_SECURITY_MAP_NAME);
        description = (String) attrList.get(ResourceConstants.CONNECTOR_CONN_DESCRIPTION);
        principalsMap = (Properties) attrList.get(ResourceConstants.WORK_SECURITY_MAP_PRINCIPAL_MAP);
        groupsMap = (Properties) attrList.get(ResourceConstants.WORK_SECURITY_MAP_GROUP_MAP);
    }


}
