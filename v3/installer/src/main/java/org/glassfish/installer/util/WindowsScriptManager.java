package org.glassfish.installer.util;

import java.io.*;
import java.util.*;
import org.openinstaller.util.ExecuteCommand;

/* This class is the windows implementation to manage short cuts.
 * Current implementation relies on Windows Scripting Host.
 */
public class WindowsScriptManager {

	// Script code to create short cut of type ".lnk"
	protected String CREATE_LNK_SHORTCUT_SCRIPT_CODE = "Dim WSHShell, LinkFile, StrUserDesktop"
			+ "\n"
			+ "Set WSHShell = WScript.CreateObject(\"WScript.Shell\")"
			+ "\n"
			+ "StrUserDesktop = WshShell.SpecialFolders(\"StartMenu\")"
			+ "\n \n"
			+

			"LinkFile = StrUserDesktop & \"\\%%FOLDER_NAME%%\\%%NAME%%.lnk\""
			+ "\n"
			+ "Set Link = WSHShell.CreateShortcut(LinkFile)"
			+ "\n"
			+ "Link.TargetPath = \"%%TARGET_PATH%%\""
			+ "\n"
			+ "Link.Description = \"%%DESCRIPTION%%\""
			+ "\n"
			+ "Link.Arguments = \"%%ARGUMENTS%%\""
			+ "\n"
			+ "Link.IconLocation = \"%%ICON_FILE_PATH%%\""
			+ "\n"
			+ "Link.WorkingDirectory = \"%%WORKING_DIRECTORY%%\""
			+ "\n"
			+ "Link.WindowStyle = \"%%WINDOW_STYLE%%\""
			+ "\n"
			+ "Link.Save"
			+ "\n";

	// Script code to create short cut of type ".url"
	protected String CREATE_URL_SHORTCUT_SCRIPT_CODE = "Dim WSHShell, LinkFile, StrUserDesktop"
			+ "\n"
			+ "Set WSHShell = WScript.CreateObject(\"WScript.Shell\")"
			+ "\n"
			+ "StrUserDesktop = WshShell.SpecialFolders(\"StartMenu\")"
			+ "\n \n"
			+

			"LinkFile = StrUserDesktop & \"\\%%FOLDER_NAME%%\\%%NAME%%.url\""
			+ "\n"
			+ "Set Link = WSHShell.CreateShortcut(LinkFile)"
			+ "\n"
			+ "Link.TargetPath = \"%%TARGET_PATH%%\""
			+ "\n"
			+ "Link.Save"
			+ "\n";

	// Script code to delete the entire program group folder.
	protected String DELETE_FOLDER_SCRIPT_CODE =

	"Dim WSHShell, LinkFile, StrUserDesktop, FsObj"
			+ "\n"
			+ "Set WSHShell = WScript.CreateObject(\"WScript.Shell\")"
			+ "\n"
			+ "StrUserDesktop = WshShell.SpecialFolders(\"StartMenu\")"
			+ "\n \n"
			+ "Set FsObj = CreateObject(\"Scripting.FileSystemObject\")"
			+ "\n"
			+ "If Not FsObj.FolderExists(StrUserDesktop & \"\\%%FOLDER_NAME%%\") Then"
			+ "\n"
			+ "	FsObj.CreateFolder(StrUserDesktop & \"\\%%FOLDER_NAME%%\")"
			+ "\n" + "End If" + "\n";

	// Script code to create the entire program group folder.
	protected String CREATE_FOLDER_SCRIPT_CODE = "Dim WSHShell, LinkFile, StrUserDesktop, FsObj"
			+ "\n"
			+ "Set WSHShell = WScript.CreateObject(\"WScript.Shell\")"
			+ "\n"
			+ "StrUserDesktop = WshShell.SpecialFolders(\"StartMenu\")"
			+ "\n \n"
			+ "Set FsObj = CreateObject(\"Scripting.FileSystemObject\")"
			+ "\n"
			+ "If Not FsObj.FolderExists(StrUserDesktop & \"\\%%FOLDER_NAME%%\") Then"
			+ "\n"
			+ "	FsObj.CreateFolder(StrUserDesktop & \"\\%%FOLDER_NAME%%\")"
			+ "\n" + "End If" + "\n";

	// Script code to delete individual shortcuts, as we do not support
	// incremental uninstallation now, this is a noop.
	protected String DELETE_ITEM_SCRIPT_CODE = "NOT CURRENTLY USED";

	/*
	 * Generic execute method, that takes in the cscript commands, convert it to
	 * a temporary .vbs script, then execute it. The temporary file will be
	 * deleted.
	 */
	protected boolean execute(String scriptCode) {
		final File theTempFile;
		FileWriter theWriter = null;
		boolean retValue = true;
		String theOutput = "";
		try {
			theTempFile = File.createTempFile("wshscript", ".vbs");
			theWriter = new FileWriter(theTempFile);
			theWriter.write(scriptCode);
			theWriter.close();
			// Use the cscript command to execute the VBScript file.
			final String[] theCmd = new String[] { "cscript",
					"\"" + theTempFile.getAbsolutePath() + "\"", "//NOLOGO" };
			final ExecuteCommand theExec = new ExecuteCommand(theCmd);
			theExec.execute();
			theOutput = theExec.getAllOutput();
			// Delete the temporary file
			theTempFile.delete();
		} catch (Exception theIOE) {
			return retValue;
		}
		return retValue;
	}
}
