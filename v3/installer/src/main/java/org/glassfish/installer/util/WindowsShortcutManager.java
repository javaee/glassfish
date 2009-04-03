package org.glassfish.installer.util;

import java.io.*;
import java.util.*;
import java.lang.*;
import org.openinstaller.util.ExecuteCommand;
import org.glassfish.installer.util.WindowsScriptManager;

/* This class accepts method paramters to Manage Windows short cut program
 * group and items. Extends the Impl. ScriptManager that actuall does the
 * work.
 */
public class WindowsShortcutManager extends WindowsScriptManager {

	/*
	 * Create short cut of type .LNK, this is the standard shortcut that points
	 * to executables and batch files on windows.
	 */
	public boolean createShortCut(String tFolderName, String tLinkName,
			String tTargetPath, String tLinkDescription, String tArgumentList,
			String tIconLocation, String tWorkingDirectory, String tWindowStyle) {

		boolean retStatus = true;
		WindowsScriptManager wsMgr = new WindowsScriptManager();

		wsMgr.CREATE_LNK_SHORTCUT_SCRIPT_CODE = wsMgr.CREATE_LNK_SHORTCUT_SCRIPT_CODE
				.replaceAll("%%FOLDER_NAME%%", tFolderName).replaceAll(
						"%%NAME%%", tLinkName).replaceAll("%%TARGET_PATH%%",
						tTargetPath).replaceAll("%%DESCRIPTION%%",
						tLinkDescription).replaceAll("%%ARGUMENTS%%",
						tArgumentList).replaceAll("%%ICON_FILE_PATH%%",
						tIconLocation).replaceAll("%%WORKING_DIRECTORY%%",
						tWorkingDirectory).replaceAll("%%WINDOW_STYLE%%",
						tWindowStyle);
		try {

			wsMgr.execute(wsMgr.CREATE_LNK_SHORTCUT_SCRIPT_CODE);
		} catch (Exception ex) {
			// Should we log this?
			retStatus = false;
		}
		return retStatus;
	}

	/*
	 * Create short cut of type .URL, overridden method to handle htmls and web
	 * links.
	 */
	public boolean createShortCut(String tFolderName, String tLinkName,
			String tTargetPath) {

		boolean retStatus = true;
		WindowsScriptManager wsMgr = new WindowsScriptManager();

		wsMgr.CREATE_URL_SHORTCUT_SCRIPT_CODE = wsMgr.CREATE_URL_SHORTCUT_SCRIPT_CODE
				.replaceAll("%%FOLDER_NAME%%", tFolderName).replaceAll(
						"%%NAME%%", tLinkName).replaceAll("%%TARGET_PATH%%",
						tTargetPath);
		try {

			wsMgr.execute(wsMgr.CREATE_URL_SHORTCUT_SCRIPT_CODE);
		} catch (Exception ex) {
			// Should we log this?
			retStatus = false;
		}
		return retStatus;
	}

	/* Create the folder that holds the shortcut. */
	public boolean createFolder(String tFolderName) {

		boolean retStatus = true;
		WindowsScriptManager wsMgr = new WindowsScriptManager();

		wsMgr.CREATE_FOLDER_SCRIPT_CODE = wsMgr.CREATE_FOLDER_SCRIPT_CODE
				.replaceAll("%%FOLDER_NAME%%", tFolderName);

		try {
			wsMgr.execute(wsMgr.CREATE_FOLDER_SCRIPT_CODE);
		} catch (Exception ex) {
			// Should we log this?
			retStatus = false;
		}
		;
		return retStatus;
	}

	/* Delete the folder that holds the shortcut. */
	public boolean deleteFolder(String tFolderName) {

		boolean retStatus = true;
		WindowsScriptManager wsMgr = new WindowsScriptManager();

		wsMgr.DELETE_FOLDER_SCRIPT_CODE = wsMgr.DELETE_FOLDER_SCRIPT_CODE
				.replaceAll("%%FOLDER_NAME%%", tFolderName);
		try {

			wsMgr.execute(wsMgr.DELETE_FOLDER_SCRIPT_CODE);
		} catch (Exception ex) {
			// Should we log this?
			retStatus = false;
		}
		return retStatus;
	}

}
