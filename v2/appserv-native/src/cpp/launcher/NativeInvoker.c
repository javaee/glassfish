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

#include <string.h>
#include <stdio.h> 
#include <stdlib.h> 
#include "processLauncher.h" 

char *commandLine[COMMAND_LINE_SIZE];

int getJavaStartCommand(char *scriptFile); 
int invokeProcess(char *scriptFile, char *childExecutorFile, int holdService);
int digestData(char *chBuff, char *delimiter, int *foundSync, int *lineCnt, int *conFlag);
int executeJavaCommand(char *childExecutorFile, int iVerbose, int iLauncherReturn, char *displayName);
int getDebug(void);
void printDebug(char *message);

/* 
This method is used by the main entry points and represents the main porttion
of the native launcher
*/
int invokeProcess(char *scriptFile, char *childExecutorFile, int holdService) {

    /*
    JNIEnv *env;
    jint res;
    jclass cls;
    jmethodID mid;
    jstring jstr;
    jclass stringClass;
    jobjectArray args;
    JavaVMInitArgs vm_args;
    JavaVMOption options[200];
    */
    int ii=0, jj=0, iRet=0, iStart=0, iLength=0, numberOfJvmOptions=0, numberOfArgs=0, iVerbose=0, iLauncherReturn=0;
    char *defaultLogFile=NULL, *serverName="server", *domainName="domain", *displayName=NULL;
    char *className=NULL, *strPointer=NULL;

    if(getDebug()) printf("\nRunning native launcer for Script to execute: %s", scriptFile);

    // get executable java command string will be in commandLineArgs
    getJavaStartCommand(scriptFile);

    // check to make sure that some type of command was returned
    if (commandLine[0] == NULL) {
        printf("\nERROR:No command to run, exiting process!!\n");
        // no command found so exit
        // the log should already show the error that was encountered
        exit(1);
    }

    /*
    commandline array order is in standard java command format
    jvm args & system args (all beginning with a "-")
    The main class (does not begin with a "-")
    args for the main class
    */
    
    // loop and retrieve jvm & system args
    // read in the following jvmArgs that start with -
    ii=0;
    while (commandLine[ii][0] == '-') {
        
        // see if jvmarg is for the librarpath if set path
        if(strncmp(commandLine[ii], "-Djava.library.path=", 19) == 0) {
            if(getDebug()) printf("\n *** found libraryPath malloc %d  --   %s", strlen(commandLine[ii]), commandLine[ii]); fflush(stdout); 
            // alloc memory for new string that will set environment variable
            strPointer=(char *) calloc((strlen(commandLine[ii]) + 1), sizeof(char)); 

            // set per platform ???
            // may have to read in current path???
            #ifdef XP_WIN32
                iStart=4;
                strcpy(strPointer, "PATH");
            #else
                iStart=15;
                strcpy(strPointer, "LD_LIBRARY_PATH");
            #endif            

            iLength=strlen(commandLine[ii]);
            for(jj=19; jj <= iLength; jj++) {
                strPointer[jj - (19 - iStart)]=commandLine[ii][jj];
            }
            if(getDebug()) printf("\n*** environment to %s", strPointer);fflush(stdout); 
            putenv(strPointer);

            // DO NOT free this memory, it is used by the environment and it will corrupt the env variable.
            //free(strPointer);

        } else if(strncmp(commandLine[ii], "-Dcom.sun.aas.verboseMode=", 25) == 0) {
            // see if verbose is set so standard input and error will not be redirected
            if(getDebug()) printf("\n *** found verbose %s", commandLine[ii]); fflush(stdout);
            // set verbose to true so stderr and stdout will not be piped to the
            // log file
            iVerbose=1;
        
        } else if(strcmp(commandLine[ii], "-Dcom.sun.aas.launcherReturn=hold") == 0) {
            // see if method is suppose to hold and wait for child process to return
            if(getDebug()) printf("\n *** found launcherReturn %s", commandLine[ii]); fflush(stdout);
            iLauncherReturn=1;

        } else if(strncmp(commandLine[ii], "-Dcom.sun.aas.instanceName=", 26) == 0) {
            // see if there is a default log set to send standard input and error
            if(getDebug())  printf("\n *** found server name malloc %d  --   %s", strlen(commandLine[ii]), commandLine[ii]); fflush(stdout);
            // alloc memory for new string that will set environment variable
            serverName=(char *) calloc((strlen(commandLine[ii]) + 1), sizeof(char)); 
            iLength=strlen(commandLine[ii]);
            for(jj=27; jj <= iLength; jj++) {
                serverName[jj - 27]=commandLine[ii][jj];
            }
            if(getDebug()) printf("\n*** server name to %s", serverName); fflush(stdout);

        } else if(strncmp(commandLine[ii], "-Ddomain.name=", 13) == 0) {
            // see if there is a default log set to send standard input and error
            if(getDebug())  printf("\n *** found server name malloc %d  --   %s", strlen(commandLine[ii]), commandLine[ii]); fflush(stdout);
            // alloc memory for new string that will set environment variable
            domainName=(char *) calloc((strlen(commandLine[ii]) + 1), sizeof(char)); 
            iLength=strlen(commandLine[ii]);
            for(jj=14; jj <= iLength; jj++) {
                domainName[jj - 14]=commandLine[ii][jj];
            }
            if(getDebug()) printf("\n*** domain name to %s", domainName); fflush(stdout);
        }

        ii++;
    }

    if (getDebug()) printf("\n\n*** into executeJavaCommand ...\n"); fflush(stdout);
    // create pipe for input and excute new process

    // check for displayname to show
    if(strcmp(serverName,"server") == 0) {
        // so domain name instead
        displayName=domainName;
    } else {
        displayName=serverName;
    }

    iRet=executeJavaCommand(childExecutorFile, iVerbose, iLauncherReturn, displayName);
    if (getDebug()) printf("\n\n*** out of executeJavaCommand ...\n"); fflush(stdout);

    // make sure free memory
    free(serverName);
    free(domainName);

    return iRet;
}
