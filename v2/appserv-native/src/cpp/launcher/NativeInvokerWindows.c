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

#include <process.h> 
#include <string.h>
#include <stdio.h> 
#include <windows.h> 
#include <TCHAR.h>
#include "processLauncher.h" 


char *commandLine[COMMAND_LINE_SIZE];
char delimiter[2]="\0\0"; 

int getJavaStartCommand(char *scriptFile); 
BOOL createChildProcess(char *scriptFile, HANDLE *hChildStdoutWr); 
void readFromPipe(HANDLE *hChildStdoutRdDup); 
void errorExit(char*); 
void PrintEvent(const TCHAR* psz);
void LookupErrorMsg(TCHAR* pszMsg, int cch, DWORD dwError);
int ErrorPrinter(const TCHAR* psz, DWORD dwErr);
int digestData(char *chBuff, char *delimiter, int *foundSync, int *lineCnt, int *conFlag);
int executeJavaCommand(char *childExecutorFile, int iVerbose, int iLauncherReturn, char *displayName);
int getDebug(void);
void printDebug(char *message);


int getJavaStartCommand(char *scriptFile) { 
	HANDLE hChildStdoutRd, hChildStdoutWr, hChildStdoutRdDup;
    SECURITY_ATTRIBUTES saAttr; 
    BOOL fSuccess; 
	
	//DebugBreak();
    // Set the bInheritHandle flag so pipe handles are inherited. 
    saAttr.nLength = sizeof(SECURITY_ATTRIBUTES); 
    saAttr.bInheritHandle = TRUE; 
    saAttr.lpSecurityDescriptor = NULL; 
 
    // The steps for redirecting child process's STDOUT: 
    //     1. Create anonymous pipe to be STDOUT for child process. 
    //     2. Create a noninheritable duplicate of the read handle and
    //        close the inheritable read handle. 
    //     3. Set STDOUT for child process
 
    // Create a pipe to be used for the child process's STDOUT. 
     if (! CreatePipe(&hChildStdoutRd, &hChildStdoutWr, &saAttr, 0)) 
        errorExit("Stdout pipe creation failed"); 
 
    // Create noninheritable read handle and close the inheritable read 
    // handle. 
    fSuccess = DuplicateHandle(GetCurrentProcess(), hChildStdoutRd,
        GetCurrentProcess(), &hChildStdoutRdDup , 0, FALSE,
        DUPLICATE_SAME_ACCESS);
	if(!fSuccess) {
		PrintEvent(_T("DuplicateHandle failed"));
		errorExit("DuplicateHandle failed");
	}
    CloseHandle(hChildStdoutRd);

	//
	// Now create the child process. 
    fSuccess = createChildProcess(scriptFile, hChildStdoutWr);

	if (!fSuccess) {
		PrintEvent(_T("Could not create process!!"));
		errorExit("Create process failed"); 
	}

	//PrintEvent(_T("Created Child Process Successfully"));

	// Close the write end of the pipe before reading from the 
    // read end of the pipe. 
    if (!CloseHandle(hChildStdoutWr)) 
        errorExit("Closing handle failed"); 

    // Read from pipe that is the standard output for child process. 
    readFromPipe(hChildStdoutRdDup); 

    return 0;
} 
 


BOOL createChildProcess(char *scriptFile, HANDLE *hChildStdoutWr) { 
    PROCESS_INFORMATION piProcInfo; 
    STARTUPINFO siStartInfo;
    BOOL bFuncRetn = FALSE; 
    
	//DebugBreak();

    // Set up members of the PROCESS_INFORMATION structure. 
    ZeroMemory( &piProcInfo, sizeof(PROCESS_INFORMATION) );
 
     // Set up members of the STARTUPINFO structure. 
    ZeroMemory( &siStartInfo, sizeof(STARTUPINFO) );
    siStartInfo.cb = sizeof(STARTUPINFO); 
    siStartInfo.dwFlags          = STARTF_USESTDHANDLES;
    siStartInfo.hStdInput 		 = 0;
    siStartInfo.hStdOutput 	     = hChildStdoutWr;
    siStartInfo.hStdError 		 = hChildStdoutWr;

    PrintEvent(_T(scriptFile));

    // Create the child process. 
    bFuncRetn = CreateProcess(NULL, 
        scriptFile,         // command line 
        NULL,               // process security attributes 
        NULL,               // primary thread security attributes 
        TRUE,               // handles are inherited 
        0,                  // no creation flags 
        NULL,               // use parent's environment 
        NULL,               // use parent's current directory 
        &siStartInfo,       // STARTUPINFO pointer 
        &piProcInfo);       // receives PROCESS_INFORMATION 
   
    if (bFuncRetn == 0) {
		ErrorPrinter(_T("Could Not Create Process"), GetLastError());
        errorExit("CreateProcess failed");
        return 0;
    } else {
        CloseHandle(piProcInfo.hProcess);
        CloseHandle(piProcInfo.hThread);
        return bFuncRetn;
    }
}



void readFromPipe(HANDLE *hChildStdoutRdDup) { 
    DWORD dwRead;
    HANDLE hStdout = GetStdHandle(STD_OUTPUT_HANDLE);
    char chBuff[BUFSIZ]; 
    char *pchBuff, *pdelimiter;
    int lineCnt=0, ii=0, iRet=0, startOfCommandFound=FALSE, endOfCommandFound=FALSE;
    int foundSync=FALSE, conFlag=FALSE;
    int *pfoundSync, *plineCnt, *pconFlag;
    
    // set buffer pointer to buffer
    pchBuff=chBuff;
    pdelimiter = delimiter;
    pfoundSync = &foundSync;
    plineCnt = &lineCnt;
    pconFlag = &conFlag;
    
    // Read output from the child process, and write to parent's STDOUT. 
    lineCnt=0;
    //PrintEvent(_T("Reading output of process..."));


    for (;;) { 
        // read in one less than buffer so null pointer can be added
		if( !ReadFile( hChildStdoutRdDup, chBuff, BUFSIZ - 1, &dwRead, NULL) || dwRead == 0) {
			//ErrorPrinter(_T("ReadFile Returned a False"), GetLastError());
			if(getDebug()) {
				printf("ReadFile Returned False %d lines have been read", lineCnt);
            }
			break; 
		}

        // null terminate string for search, should need to check length, but just make sure
        // fgets does this automatically
        if (dwRead < BUFSIZ) {
            chBuff[dwRead] = '\0';
        }

        if(getDebug()) {
            // send string to stdout
            printf("\n\n\n>>Number Read in: %d\n%s", dwRead, chBuff);
            //WriteFile(hStdout, chBuff, dwRead, &dwWritten, NULL);
        }

        iRet=digestData(pchBuff, delimiter, pfoundSync, plineCnt, pconFlag);
        if(iRet == 1) {
            // start of command was found, so command should be okay
            printDebug("*** digestData returned START OF COMMAND FOUND");
            startOfCommandFound=TRUE;
        } else if (iRet == 2) {
            // end of command was found, so command should be okay
            printDebug("*** digestData returned END OF COMMAND FOUND");
            endOfCommandFound=TRUE;
        } else if (iRet == 3) {
            // both start and end of command was found, so command should be okay
            printDebug("*** digestData returned START AND END OF COMMAND FOUND");
            startOfCommandFound=TRUE;
            endOfCommandFound=TRUE;
        }
    }

    if (!startOfCommandFound || !endOfCommandFound) {
        // error either start of end of command was not found
        errorExit("Either the Start or End command tokens where not found by the native launcher. Set the \"DEBUGX\" environment variable and re-run to debug the problem.");
    }

    if(getDebug()) {
        for(ii=0; ii < lineCnt; ii++) {
            printf("\n%d = %s", ii, commandLine[ii]);
        }
    }
} 




int executeJavaCommand(char *childExecutorFile, int iVerbose, int iLauncherReturn, char *displayName) {

    SECURITY_ATTRIBUTES saAttr; 
    BOOL fSuccess; 
    PROCESS_INFORMATION piProcInfo; 
    STARTUPINFO siStartInfo; 
    BOOL bFuncRetn = FALSE; 
    DWORD dwWritten; 
    int ii=0;
    DWORD dwFlag, dwRet;
    HANDLE hChildStdinRd, hChildStdinWr, hChildStdinWrDup;
    char *commandSyncStart = "STARTOFCOMMAND";
    char *commandSyncEnd = "ENDOFCOMMAND";
    HANDLE hStdin = GetStdHandle(STD_INPUT_HANDLE);
    char chBuff[BUFSIZ];
    char *startChar;


 
    // The steps for redirecting child process's STDIN: 
    //     1.  Save current STDIN, to be restored later. 
    //     2.  Create anonymous pipe to be STDIN for child process.  
    //     3.  Set STDIN of the parent to be the read handle to the 
    //         pipe, so it is inherited by the child process. 
    //     4.  Create a noninheritable duplicate of the write handle, 
    //         and close the inheritable write handle. 
 
    // Set the bInheritHandle flag so pipe handles are inherited. 
    saAttr.nLength = sizeof(SECURITY_ATTRIBUTES); 
    saAttr.bInheritHandle = TRUE; 
    saAttr.lpSecurityDescriptor = NULL; 

    // Create a pipe for the child process's STDIN. 
   if (! CreatePipe(&hChildStdinRd, &hChildStdinWr, &saAttr, 0)) 
      errorExit("Stdin pipe creation failed"); 
 
    // Duplicate the write handle to the pipe so it is not inherited. 
    fSuccess = DuplicateHandle(GetCurrentProcess(), hChildStdinWr, 
        GetCurrentProcess(), &hChildStdinWrDup, 0, 
        FALSE,                  // not inherited 
        DUPLICATE_SAME_ACCESS); 

    if (! fSuccess) 
        errorExit("DuplicateHandle failed"); 
 
    CloseHandle(hChildStdinWr); 
 
    // Now create the child process. 

    // Set up members of the PROCESS_INFORMATION structure. 
    ZeroMemory( &piProcInfo, sizeof(PROCESS_INFORMATION) );

    // Set up members of the STARTUPINFO structure. 
    ZeroMemory( &siStartInfo, sizeof(STARTUPINFO) );
    siStartInfo.cb = sizeof(STARTUPINFO); 
    siStartInfo.lpReserved          = 0;
    siStartInfo.lpDesktop           = NULL;
    siStartInfo.lpTitle             = displayName;
    siStartInfo.dwX                 = 0;
    siStartInfo.dwY                 = 0;
    siStartInfo.dwXSize             = 0;
    siStartInfo.dwYSize             = 0;
    siStartInfo.dwXCountChars       = 0;
    siStartInfo.dwYCountChars       = 0;
    siStartInfo.dwFillAttribute     = 0;
    siStartInfo.dwFlags             = STARTF_USESHOWWINDOW | STARTF_USESTDHANDLES;
    siStartInfo.wShowWindow         = SW_SHOWDEFAULT;
    siStartInfo.cbReserved2         = 0;
    siStartInfo.lpReserved2         = 0;
    siStartInfo.hStdInput           = hChildStdinRd;
    siStartInfo.hStdOutput 	    = GetStdHandle(STD_OUTPUT_HANDLE);
    siStartInfo.hStdError           = GetStdHandle(STD_OUTPUT_HANDLE);

    if (getDebug()) {
        printf("\n\n*** Executing ->%s<-\n", childExecutorFile);
        fflush(stdout);
    }

    // create console for verbose mode
    if (iVerbose) {
        dwFlag=CREATE_NEW_CONSOLE | CREATE_NEW_PROCESS_GROUP;
    } else {
        dwFlag=CREATE_NEW_PROCESS_GROUP;
    }

    // Create the child process. 
    bFuncRetn=CreateProcess(NULL, 
        childExecutorFile,  // command line 
        NULL,               // process security attributes 
        NULL,               // primary thread security attributes 
        TRUE,               // handles are inherited 
        dwFlag,             // creation flags 
        NULL,               // use parent's environment 
        NULL,               // use parent's current directory 
        &siStartInfo,       // STARTUPINFO pointer 
        &piProcInfo);       // receives PROCESS_INFORMATION 

    //printf("\n*** Child Process ID = %s\n", piProcInfo.dwProcessId);
    if (bFuncRetn == 0) {
		ErrorPrinter(_T("Could Not Create Process"), GetLastError());
        errorExit("CreateProcess for java command failed");
        return 0;
    }

    if (getDebug()) printf("\n*** Writing to Child process' input using delimiter '%s'\n", delimiter); fflush(stdout);

    // Write to pipe that is the standard input for a child process. 
    // write out start of command
    WriteFile(hChildStdinWrDup, commandSyncStart, strlen(commandSyncStart), &dwWritten, NULL); 
    
    WriteFile(hChildStdinWrDup, delimiter, 1, &dwWritten, NULL); 
    while (commandLine[ii] != NULL) { 
        WriteFile(hChildStdinWrDup, commandLine[ii], strlen(commandLine[ii]), &dwWritten, NULL); 
        WriteFile(hChildStdinWrDup, delimiter, 1, &dwWritten, NULL); 
        ii++;
    } 
    // writeout end command
    WriteFile(hChildStdinWrDup, commandSyncEnd, strlen(commandSyncEnd), &dwWritten, NULL); 
    WriteFile(hChildStdinWrDup, delimiter, 1, &dwWritten, NULL); 

    // write out identity information that should be on the stdin
    WriteFile(hChildStdinWrDup, "IDENTITYINFORMATION", 19, &dwWritten, NULL); 
    WriteFile(hChildStdinWrDup, delimiter, 1, &dwWritten, NULL); 


    while (fgets(chBuff, BUFSIZ, stdin) != NULL) {
        //printf("%s", buff);
        if(getDebug()) {
            // send string to stdout
            printf("\n\n\n>>STDIN Number Read in: %d - %s", strlen(chBuff), chBuff);
        }
        // parse for carrage return
        startChar=strtok(chBuff, "\n");
        while (startChar != NULL) {

            if(getDebug()) {
                // send string to pipe for child process
                printf("Writing Identity element to input pipe - %s", startChar);
            }
            WriteFile(hChildStdinWrDup, startChar, strlen(startChar), &dwWritten, NULL); 
            WriteFile(hChildStdinWrDup, delimiter, 1, &dwWritten, NULL); 

            // get next token, if exists
            startChar=strtok(NULL, "\n");
        }
    }

    // Close the pipe handle so the child process stops reading. 
    if (! CloseHandle(hChildStdinWrDup)) {
        errorExit("Close pipe failed"); 
    }

    if (iVerbose || iLauncherReturn) {
        // Wait until process exits.
        WaitForSingleObject(piProcInfo.hProcess, INFINITE);

        GetExitCodeProcess(piProcInfo.hProcess, &dwRet);
        if(getDebug()) printf("\nChildProcess returned with and exit code = %d\n", dwRet); fflush(stdout);
    }


    // need to detach parent from child
    return dwRet;
}


//VOID errorExit (LPTSTR lpszMessage) { 
void errorExit (char *lpszMessage) { 
    fprintf(stderr, "\n\n*** EXITING ERROR: %s ***\n\n", lpszMessage); 
	ExitProcess(1); 
} 


int ErrorPrinter(const TCHAR* psz, DWORD dwErr)
{
	TCHAR szMsg[512];
	TCHAR sz[512];
	HANDLE hes = RegisterEventSource(0, _T("appserv"));
	const TCHAR* rgsz[] = { sz };
	LookupErrorMsg(szMsg, sizeof szMsg / sizeof *szMsg, dwErr);

	//wsprintf(sz, _T( "%s failed: %s" ), psz, szMsg);
	if(hes)
	{
		ReportEvent(hes, EVENTLOG_ERROR_TYPE, 0, 0, 0, 1, 0, rgsz, 0);
		DeregisterEventSource(hes);
	}
	return dwErr;
}


void LookupErrorMsg(TCHAR* pszMsg, int cch, DWORD dwError)
{
    if(!FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, 0, dwError, 0, pszMsg, cch, 0)) {

        //wsprintf(pszMsg, _T("Unknown: %x"), dwError);
    }

}


void PrintEvent(const TCHAR* psz)
{
	const TCHAR* rgsz[] = { psz };

	HANDLE hes = RegisterEventSource(0, _T("appserv"));
	if(hes)
	{
		ReportEvent(hes, EVENTLOG_INFORMATION_TYPE, 0, 0, 0, 1, 0, rgsz, 0);
		DeregisterEventSource(hes);
	}
}
