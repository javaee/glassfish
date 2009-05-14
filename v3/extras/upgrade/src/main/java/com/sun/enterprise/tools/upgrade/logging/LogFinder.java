/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.upgrade.logging;


import com.sun.enterprise.tools.upgrade.common.SourceAppSrvObj;
import com.sun.enterprise.tools.upgrade.common.TargetAppSrvObj;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Check for V3 logging.properties file which contains the server log file path.
 * Use the default server log location if the properties file or log file is not found.
 *
 * @author rsearls
 */
public class LogFinder {
   

    public static File getServerLogFile() throws FileNotFoundException, IOException{
        CommonInfoModel commonInfo = CommonInfoModel.getInstance();
        TargetAppSrvObj tAppSrvObj = commonInfo.getTarget();
        
        // Check for a V3 logging.properties file
        String tmpD = tAppSrvObj.getDomainDir();
        File f = new File(tmpD + File.separator + UpgradeConstants.AS_CONFIG_DIRECTORY,
                UpgradeConstants.LOGGING_PROPERTIES);

        File serverLogFile = new File(tmpD + File.separator  +
                UpgradeConstants.SERVER_LOG_DEFAULT);
        if (f.exists()) {
            Properties p = new Properties();
            FileInputStream fis = new FileInputStream(f);
            p.load(fis);
            fis.close();
            String tmpName = p.getProperty(UpgradeConstants.SERVER_LOG_PROPERTY,
                    tmpD + File.separator + UpgradeConstants.SERVER_LOG_DEFAULT);
            serverLogFile = new File(tmpName);
        }

        if (!serverLogFile.exists()){
            throw new FileNotFoundException(serverLogFile.getAbsolutePath());
        }
        return serverLogFile;
    }

}
