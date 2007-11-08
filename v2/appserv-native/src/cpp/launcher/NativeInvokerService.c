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

#include <stdio.h> 
#include <stdlib.h> 
#include <windows.h>
#include <winuser.h>
#include <TCHAR.h>
#include <process.h> 
//#include <jni.h>

void WINAPI ServiceMain(DWORD argc, LPTSTR* argv);
void WINAPI Handler(DWORD dwControl);
void SendStatus(DWORD dwCurrentStatus, DWORD dwCheckpoint, DWORD dwWaitHint,
    DWORD dwControlsAccepted, DWORD dwExitCode, DWORD dwSpecificErrorCode);
int ErrorPrinter(const TCHAR* pszFcn, DWORD dwErr);
void LookupErrorMsg(TCHAR* pszMsg, int cch, DWORD dwError);
void PrintEvent(const TCHAR* psz);
int executeCommand(char *scriptFile);
int getDebug(void);
void printDebug(char *message);
void errorExit(char*); 

TCHAR g_ServiceName[260]; // largest it can be is 256
SERVICE_STATUS_HANDLE g_hServiceStatus;
HANDLE g_hEvents[1];
DWORD g_state = 0;
DWORD g_status = 0;
char *g_executableStartScript=NULL, *g_executableStopScript=NULL;
int _debugProg=0;
PROCESS_INFORMATION piProcInfo; 
BOOL isVerbose = FALSE;

int main(int argc, char *argv[]) {

    
    SERVICE_TABLE_ENTRY svcTable[] =
    {
    	{ _T("appservService"), (LPSERVICE_MAIN_FUNCTION)ServiceMain },
    	//{ _T(argv[3]), (LPSERVICE_MAIN_FUNCTION)ServiceMain },
    	{ NULL, NULL }
    };
    ZeroMemory( &piProcInfo, sizeof(PROCESS_INFORMATION) );

    // need at least 3 args
    if (argc < 3) {
        printf("USAGE: This executable is meant to be used with the Windows sc.exe command in the form:\n");
        printf("sc.exe create SERVICE_NAME binPath= \"FULLY_QUALIFIED_PATH_TO_appservService.exe \\\"FULLY_QUALIFIED_asadmin.bat_start_command\\\" \\\"FULLY_QUALIFIED_asadmin.bat_stop_command\\\"\" DisplayName= \"DISPLAY_NAME\"");
        exit(1);
    }

    if (getDebug()) PrintEvent(_T("appservService starting ...."));

    // check args came in ???
    g_executableStartScript = argv[1];
    g_executableStopScript = argv[2];

    if(strstr(g_executableStartScript, "--verbose"))
        isVerbose = TRUE;
    else
        isVerbose = FALSE;

    if(!StartServiceCtrlDispatcher(svcTable))
	ErrorPrinter(_T("StartServiceCtrlDispatcher"), GetLastError());

    return 0;
}


void WINAPI ServiceMain(DWORD argc, LPTSTR* argv) {
	DWORD dwWait, dwError=NO_ERROR, dwRet=0;
	int iRet=0, i=0;
        TCHAR serviceStopEvent[300];

        ZeroMemory( &piProcInfo, sizeof(PROCESS_INFORMATION) );
        

        // SCM provides ServiceID as 1st argument, save for later use
        memset(g_ServiceName, 0, sizeof(g_ServiceName));
        strcpy(g_ServiceName, argv[0]);  

	// Register the control handler
	g_hServiceStatus = RegisterServiceCtrlHandler(g_ServiceName, (LPHANDLER_FUNCTION)Handler);

	if(!g_hServiceStatus)
            ErrorPrinter(_T("RegisterServiceCtrlHandler"), GetLastError());

	// Let the SCM know I'm working on starting
	SendStatus(SERVICE_START_PENDING, 1, 600000, 0, NO_ERROR, 0);
	
	// Do some initialization
        sprintf(serviceStopEvent, "%sStopEvent", g_ServiceName);  

	//g_hEvents[0] = CreateEvent(NULL, TRUE, FALSE, _T("StopEvent"));
	g_hEvents[0] = CreateEvent(NULL, TRUE, FALSE, serviceStopEvent);

        // call appservInvoker to start server ???
	dwRet=executeCommand(g_executableStartScript);
        if (!dwRet) {
            // succssful start
	
            // Notify the SCM I am running
            //SendStatus(SERVICE_RUNNING, 0, 0, SERVICE_ACCEPT_STOP | SERVICE_ACCEPT_SHUTDOWN, NO_ERROR, 0);
            SendStatus(SERVICE_RUNNING, 0, 0, SERVICE_ACCEPT_STOP, NO_ERROR, 0);

            // Wait for a signal to stop or shutdown
            //dwWait = WaitForMultipleObjects(1, g_hEvents, FALSE, INFINITE);

            dwWait=WaitForSingleObject(g_hEvents[0], INFINITE);
            if(getDebug() && WAIT_OBJECT_0 == dwWait) {
                PrintEvent(_T("Got the Stop or shutdown Event"));
            }

            if(isVerbose == TRUE)
            {
                CloseHandle(piProcInfo.hProcess);
                CloseHandle(piProcInfo.hThread);
            }

            // Let the SCM know I am stopping
            SendStatus(SERVICE_STOP_PENDING, 1, 120000, 0, NO_ERROR, 0);

            // stop server by call stop script
            dwRet=executeCommand(g_executableStopScript);
            

        } else {
            // error on startup
            dwError=ERROR_SERVICE_SPECIFIC_ERROR;
        }

	// De-initialize resources - do the steps necessary to stop
	for(i = 0 ; i < 1 ; i++) {
            CloseHandle(g_hEvents[i]);
	}
	
	// Let the SCM know I have stopped
	SendStatus(SERVICE_STOPPED, 0, 0, 0, dwError, dwRet);
}


void WINAPI Handler(DWORD dwControl) {
    // Keep an additional control request from coming in when you're
    // already handling it.

    if(g_state == dwControl) return;

    switch(dwControl) {
        case SERVICE_CONTROL_SHUTDOWN:
        case SERVICE_CONTROL_STOP:
            g_state = dwControl;
            if (getDebug()) PrintEvent(_T("appservService received stop or shutdown event"));
            // Notify the main thread you want to stop...
            SetEvent(g_hEvents[0]);
            break;
        default:
            // Return current status on interrogation
            if (getDebug()) PrintEvent(_T("appservService received generic event"));
            //SendStatus(g_status, 0, 0, SERVICE_ACCEPT_STOP | SERVICE_ACCEPT_SHUTDOWN, NO_ERROR, 0);
            SendStatus(g_status, 0, 0, SERVICE_ACCEPT_STOP, NO_ERROR, 0);
    }
}


void SendStatus(DWORD dwCurrentStatus, 
  DWORD dwCheckpoint, DWORD dwWaitHint,
  DWORD dwControlsAccepted,
  DWORD dwExitCode, DWORD dwSpecificErrorCode) {
	
    SERVICE_STATUS ss = { SERVICE_WIN32_OWN_PROCESS,
      dwCurrentStatus, dwControlsAccepted,
      dwExitCode, dwSpecificErrorCode, dwCheckpoint, dwWaitHint };

    g_status = dwCurrentStatus;
	
    SetServiceStatus(g_hServiceStatus, &ss);
}


/*
* Method that is called only by the windows service to execute a stop.
* Other potions of the native launcher use the java stopserv script to stop the server
* causing the jvm to return and exit out of the native JNI call
*/

int executeCommand(char *scriptFile) { 
    STARTUPINFO siStartInfo;
    BOOL bFuncRetn = FALSE;
    DWORD dwRet;
    
     // Set up members of the STARTUPINFO structure. 
    ZeroMemory( &siStartInfo, sizeof(STARTUPINFO) );
    siStartInfo.cb = sizeof(STARTUPINFO); 

    PrintEvent(_T(scriptFile));

    if (getDebug()) printf("\nexecute Script %s", scriptFile);

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
        printf("\nprocess failure %x\n",  GetLastError());
        PrintEvent(_T("ProcessCreate Failed"));
	ErrorPrinter(_T("executeCommand"), GetLastError());
        return 1;
    } else if(isVerbose == FALSE) {
            // Wait until process exits.
            WaitForSingleObject( piProcInfo.hProcess, INFINITE );

            GetExitCodeProcess(piProcInfo.hProcess, &dwRet);
            printf("\nReturn code for start process - %d", dwRet);

            CloseHandle(piProcInfo.hProcess);
            CloseHandle(piProcInfo.hThread);
            return 0;
    } else {
        return 0;
    }
}


int getDebug(void) {
    return _debugProg;
}


void printDebug(char *message) {
    if (_debugProg) {
        printf("\n%s", message);
        fflush(stdout);
    }
}


void errorExit (char *lpszMessage) { 
    fprintf(stderr, "\n\n*** EXITING ERROR: %s ***\n\n", lpszMessage); 
    ExitProcess(1); 
} 

int ErrorPrinter(const TCHAR* psz, DWORD dwErr) {
    TCHAR szMsg[512];
    TCHAR sz[512];
    HANDLE hes = RegisterEventSource(0, _T("appservService"));
    const TCHAR* rgsz[] = { sz };
    LookupErrorMsg(szMsg, sizeof szMsg / sizeof *szMsg, dwErr);

    wsprintf(sz, _T( "%s failed: %s" ), psz, szMsg);

    printf("\nerrorprinter %s\n", szMsg);

    if(hes) {
        ReportEvent(hes, EVENTLOG_ERROR_TYPE, 0, 0, 0, 1, 0, rgsz, 0);
        DeregisterEventSource(hes);
    }
    return dwErr;
}


void LookupErrorMsg(TCHAR* pszMsg, int cch, DWORD dwError) {
    if(!FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, 0, dwError, 0, pszMsg, cch, 0)) {
        wsprintf(pszMsg, _T("Unknown: %x"), dwError);
    }
}


void PrintEvent(const TCHAR* psz) {
    const TCHAR* rgsz[] = { psz };

    HANDLE hes = RegisterEventSource(0, _T("appservService"));
    if(hes) {
        ReportEvent(hes, EVENTLOG_INFORMATION_TYPE, 0, 0, 0, 1, 0, rgsz, 0);
        DeregisterEventSource(hes);
    }
}
