package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.admin.servermgmt.RepositoryException;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;

/**
 * This is a place holder class ... waiting for appserver team
 * to return functionality.   This class implementation is not
 * sufficient for authenticating domain credentials.  It MUST
 * be replace with valid functionality.
 *
 * @author rebeccas
 */
public class RepositoryManager {

	public RepositoryManager(){}

    public void validateAdminUserAndPassword(RepositoryConfig repConf,
            String adminUserName, String adminPassword) throws RepositoryException{
        CommonInfoModel.getDefaultLogger().info(
                "validateAdminUserAndPassword: NEEDS TO BE REPLACED WITH REAL CODE BEFORE SHIPPING" +
                "\nDomain login credentials: \n\tuser: " + adminUserName
                + "\n\tadminPassword: " + adminPassword);

    }

    public void validateMasterPassword(RepositoryConfig repConf, String masterPassword)
        throws RepositoryException{
        CommonInfoModel.getDefaultLogger().info(
                "validateMasterPassword: NEEDS TO BE REPLACED WITH REAL CODE BEFORE SHIPPING" +
                "\nDomain login credentials: \n\tmasterPassword: " + masterPassword);
    }
}