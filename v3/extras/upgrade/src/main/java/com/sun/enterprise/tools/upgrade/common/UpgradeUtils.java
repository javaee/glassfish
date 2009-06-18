/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Utils.java
 *
 * Created on April 19, 2004, 11:47 AM
 */

package com.sun.enterprise.tools.upgrade.common;

/**
 *
 * @author  prakash
 */

import java.io.*;
import java.util.logging.*;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.util.i18n.StringManager;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;




public class UpgradeUtils {
	
	private final StringManager stringManager =
        StringManager.getManager(UpgradeUtils.class);
	
	private static final Logger logger =
        LogService.getLogger(LogService.UPGRADE_LOGGER);

	private static UpgradeUtils upgradeUtils;
	private static CommonInfoModel common;
	
	/**
	 * UpgradeUtils private constructor
	 */
	private UpgradeUtils(CommonInfoModel common) {
		UpgradeUtils.common = common;
	}
	
	/**
	 * UpgradeUtils constructor
	 */
	public static UpgradeUtils getUpgradeUtils(CommonInfoModel cim){
		if(upgradeUtils == null) {
			upgradeUtils = new UpgradeUtils(cim);
		} else {
			common = cim;
		}
		return upgradeUtils;
	}
	
	/**
	 * Method to create backup directory if In-Place upgrade is done.
	 * Also to build the domain mapping of the source.
	 * @param cmi CommonInfoModel
	 * @param dirs Contents of the domains root of source
	 * @param domainRoot Domains root of the source
	 */
	public String backupDomain(String domainName, String domainPath,
		String targetDomainRoot ){
		String timestamp=""+System.currentTimeMillis();
		String newDomainName = domainName+"_"+timestamp;
		String backup = targetDomainRoot + "/" + UpgradeConstants.BACKUP_DIR;
		File backupFile = new File(backup);
		
		//delete any existing backup directory and create a new one
		try {
			deleteDirectory(backupFile);
			if(!backupFile.mkdir()) {
				throw new SecurityException();
			}
		} catch(SecurityException se) {
			logger.log(Level.SEVERE,
				stringManager.getString("upgrade.common.make_directory_failed",
				backupFile.getAbsolutePath()));
			System.exit(1);
		}
		
		//Move the existing domain to backup directory
		File domainDir = new File(domainPath);
		String sourceDomainPath = backup + "/" + newDomainName;
		//Copy and delete instead of rename because of Windows issue.
		try{
			File backupDomainDir = new File(sourceDomainPath);
			new File(sourceDomainPath).mkdirs();
			copyDirectory(domainDir, new File(sourceDomainPath));
			if(!deleteDirectory(domainDir)) {
				throw new SecurityException();
			}
		}catch (Exception e) {
			if(e instanceof IOException) {
				logger.log(Level.SEVERE,
					stringManager.getString("upgrade.common.copy_directory_failed",
					domainDir.getAbsolutePath(), sourceDomainPath));
				deleteDirectory(backupFile);
				System.exit(1);
			}
			if(e instanceof SecurityException) {
				logger.log(Level.WARNING,
					stringManager.getString("upgrade.common.delete_backedup_domain_directory_failed",
					domainDir.getAbsolutePath()));
				System.exit(1);
			}
		}
		return sourceDomainPath;
	}

    public String cloneDomain(String domainPath, String targetDomainRoot ){
	
		File domainDir = new File(domainPath);
		try{
			File cloneDirLoc = new File(targetDomainRoot);
			cloneDirLoc.mkdirs();
			copyDirectory(domainDir, cloneDirLoc);
		}catch (Exception e) {
				logger.log(Level.SEVERE,
					stringManager.getString("upgrade.common.copy_directory_failed",
					domainDir.getAbsolutePath(), targetDomainRoot));
				System.exit(1);
		}
		return targetDomainRoot;
	}



	public String findLatestDomainBackup(String domainRoot,String domainName) {
		String latestDomainPath = null;
		if (domainName != null){
			String backupDirPath = domainRoot + "/" + UpgradeConstants.BACKUP_DIR;
			File backupDir = new File(backupDirPath);
			long latestTimestamp = 0;
			
			//If backup directory already exists return the directory name else null
			if(backupDir.isDirectory()) {
				String tmpN = domainName + "_";
				String [] dirs = backupDir.list();
				for(int i=0;i<dirs.length;i++) {
					if(dirs[i].startsWith(tmpN)){
						String time = dirs[i].substring(tmpN.length());
						long timestamp = Long.parseLong(time);
						if(timestamp > latestTimestamp) {
							latestTimestamp = timestamp;
							latestDomainPath = backupDirPath + "/"+dirs[i];
						}
					}else {
						continue;
					}
				}
			}
		}
		return  latestDomainPath;
	}

	
	public void recover() {
		String sourceDir = common.getSource().getDomainRoot();
		String targetDir = common.getTarget().getInstallDir();
		if(sourceDir != null && targetDir != null) {
			String dname = common.getSource().getDomainName();
			if(sourceDir.equals(targetDir)) {
				File backupdir = new File(sourceDir + "/" + UpgradeConstants.BACKUP_DIR);
				if(backupdir.isDirectory()) {
					String latestDomainPath = common.findLatestDomainDirBackup(sourceDir);
					if(latestDomainPath != null) {
						//Copy and delete instead of rename because of Windows issue.
						try{
							copyDirectory(new File(latestDomainPath),
								new File(sourceDir + "/" + dname));
							if(!deleteDirectory(backupdir)) {
								logger.log(Level.WARNING,
									stringManager.getString("upgrade.common.delete_directory_failed",
									backupdir.getAbsolutePath()));
							}
						}catch (IOException e) {
							logger.log(Level.SEVERE,
								stringManager.getString("upgrade.common.copy_directory_failed",
								latestDomainPath, sourceDir));
						}
					}
				}
			}
		}//not null check of domainList
	}
	

	public static void copyFile(String source, String target) throws IOException {
		FileUtils.copy(source, target);
	}
	
	/**
	 * Copies the entire tree to a new location except the symbolic links
	 * Invokes the FileUtils.java to do the same
	 *
	 * @param   sourceTree  File pointing at root of tree to copy
	 * @param   destTree    File pointing at root of new tree
	 *
	 * If target directory does not exist, it will be created.
	 *
	 * @exception  IOException  if an error while copying the content
	 */
	public static void copyDirectory(File sourceDir , File targetDir) throws IOException {
		File [] srcFiles = sourceDir.listFiles();
		if (srcFiles != null) {
			for(int i=0; i< srcFiles.length; i++) {
				File dest = new File(targetDir, srcFiles[i].getName());
				if( srcFiles[i].isDirectory() && FileUtils.safeIsRealDirectory(srcFiles[i])) {
					if (!dest.exists()) {
						dest.mkdirs();
					}
					copyDirectory(srcFiles[i], dest);
				} else {
					if (!dest.exists()) {
						dest.createNewFile();
					}
					copyFile(srcFiles[i].getAbsolutePath(),new File(targetDir, 
						srcFiles[i].getName()).getAbsolutePath());					
				}
			}
		}
	}
	

	public static boolean deleteDirectory(File dir) {
		if(dir.isDirectory()) {
			String[] subDirs = dir.list();
			for(int i=0; i<subDirs.length; i++) {
				boolean success = deleteDirectory(new File(dir, subDirs[i]));
				if(!success) {
					return false;
				}
			}
		}
		//Delete the empty directory
		return dir.delete();
	}
	
	
	public Document getDomainDocumentElement(String domainFileName){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document resultDoc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			///builder.setEntityResolver(
			///	(org.xml.sax.helpers.DefaultHandler)Class.forName
			///	("com.sun.enterprise.config.serverbeans.ServerValidationHandler").newInstance());
			resultDoc = builder.parse( new File(domainFileName));
		}catch (Exception ex){
			logger.log(Level.WARNING,
				stringManager.getString("upgrade.common.iiop_port_domain_doc"),ex);
		}
		return resultDoc;
	}
	
	public static boolean deleteFile(String targetMasterPasswordFile) {
		File masterPasswordFile = new File(targetMasterPasswordFile);
		return masterPasswordFile.delete();
	}

    /*
     * Code used by DirectoryMover implementations to actually
     * rename the directory if the user agrees. This code adds a '.original'
     * extension unless one exists already. If so, further append .0, .1,
     * etc.
     */
    public static void rename(File dir) {
        assert (dir.exists());
        File tempFile = new File(dir.getAbsolutePath() + ".original");
        if (tempFile.exists()) {
            String baseName = tempFile.getAbsolutePath();
            int count = 0;
            while (tempFile.exists()) {
                tempFile = new File(baseName + "." + count++);
            }
        }
        dir.renameTo(tempFile);
    }
    
}
