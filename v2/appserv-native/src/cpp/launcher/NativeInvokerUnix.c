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
char delimiter[2]="\0\0";

#if defined(__APPLE__) && defined(__DYNAMIC__)
#include<crt_externs.h>
static char ** environ;
#else
extern char ** environ; // Standard environment definition
#endif 


int getJavaStartCommand(char *scriptFile); 
int digestData(char *chBuff, char *delimiter, int *foundSync, int *lineCnt, int *conFlag);
int executeJavaCommand(char *childExecutorFile, int iVerbose, int iLauncherReturn, char *displayName);
int getDebug(void);
void printDebug(char *message);

int getJavaStartCommand(char *scriptFile) { 
    FILE *childSTDOUT;
    int iRet=0, startOfCommandFound=FALSE, endOfCommandFound=FALSE;
    int foundSync=FALSE, conFlag=FALSE;
    int *pfoundSync, *plineCnt, *pconFlag;
    char chBuff[BUFSIZ];
    char *pchBuff, *pdelimiter;
    int lineCnt=0, ii=0;
    char *commandSyncStart = "STARTOFCOMMAND";
    char *commandSyncEnd = "ENDOFCOMMAND";
    
    // Read output from the child process, and write to parent's STDOUT. 
    lineCnt=0;

    // set buffer to pointer
    pchBuff=chBuff;
    pdelimiter = delimiter;
    pfoundSync = &foundSync;
    plineCnt = &lineCnt;
    pconFlag = &conFlag;
     
    if ((childSTDOUT = popen(scriptFile, "r")) != NULL) {
        while (fgets(pchBuff, BUFSIZ, childSTDOUT) != NULL) {
            //printf("%s", pchBuff);
            if(getDebug()) {
                // send string to stdout
                printf("\n\n\n>>Number Read in: %d\n%s", strlen(pchBuff), pchBuff);
            }
            // digest data from read
            iRet=digestData(pchBuff, pdelimiter, pfoundSync, plineCnt, pconFlag);
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
            fprintf(stderr, "\nERROR: Either the Start or End command tokens where not found by the native launcher. Set the \"DEBUGX\" environment variable and re-run to debug the problem.");
            exit(1);
        }

        if(getDebug()) {
            for(ii=0; ii < lineCnt; ii++) {
                printf("\n%d = %s", ii, commandLine[ii]);
            }
        }
    }

    // close output stream
    pclose(childSTDOUT);
    return 0;
}



int executeJavaCommand(char *childExecutorFile, int iVerbose, int iLauncherReturn, char *displayName) {

    char *commandSyncStart = "STARTOFCOMMAND";
    char *commandSyncEnd = "ENDOFCOMMAND";
    FILE *fp;
    int pid, pipefds[2], ii=0, iRet=0;
    char chBuff[BUFSIZ];
    char *startChar;
    char *argv[3];
    
#if defined(__APPLE__) && defined(__DYNAMIC__)
    environ = *_NSGetEnviron();
#endif

    // create pipe for input into sub process
    if(pipe(pipefds) < 0) {
        perror("pipe");
        exit(1);
    }   

    // fork and exec sub process
    if ((pid = fork()) < 0) {
        perror("fork");
        exit(1);
    }

    // make childs stdin the pipe
    if (pid == 0) {
        // child
        close(0);
        // will dup into lowest fd available
        dup(pipefds[0]);
        close(pipefds[0]);

        // close write side of pipe
        close(pipefds[1]);

        if(getDebug()) {
            ii=0;
            while (environ[ii] != NULL) {
                printf("\n%s", (char*)environ[ii]);
                ii++;
            }
            // super test
            //environ[ii]="LD_LIBRARY_PATH=/space/s1as8se-20040726/jdk/jre/lib/i386/server:/space/s1as8se-20040726/jdk/jre/lib/i386/client:/space/s1as8se-20040726/jdk/jre/lib/i386:/space/s1as8se-20040726/jdk/jre/../lib/i386:/space/s1as8se-20040726/lib:/usr/lib/lwp:/space/s1as8se-20040726/lib:/usr/lib/lwp:/space/s1as8se-20040726/jdk/jre/lib/i386/client:/space/s1as8se-20040726/lib:/usr/lib/lwp:/usr/lib:/usr/X/lib:/usr/openwin/lib:/usr/bin:/usr/openwin/bin:/usr/ccs/bin:/usr/dt/lib:/usr/ucblib:/export/oracle8/lib:/export/home/basler/goglobal/lib";
            //ii++;
            //environ[ii]=NULL;
            // now execute child command
            printf("\nExec - %s\n", childExecutorFile);fflush(stdout);
        }

        argv[0]=childExecutorFile;
        argv[1]=displayName;
        argv[2]=NULL;
        execv(childExecutorFile, argv);
        // exec should not return if executed correctly
        perror("execv");
        exit(1);
    }

    // parent will execute this portion
    
    // close read side of pipe. child isn't writing
    close(pipefds[0]);

    // push java command into pipe
    fp=fdopen(pipefds[1], "w");

    if (getDebug()) printf("\n*** Writing to Child process' input using delimiter '%s'\n", delimiter); fflush(stdout);

    // Write to pipe that is the standard input for a child process. 
    // write out start of command
    fprintf(fp, commandSyncStart); 
    fprintf(fp, delimiter); 
    ii=0;
    while (commandLine[ii] != NULL) { 
        fprintf(fp, commandLine[ii]); 
        fprintf(fp, delimiter); 
        ii++;
    } 
    // writeout end command
    fprintf(fp, delimiter); 
    fprintf(fp, commandSyncEnd); 
    fprintf(fp, delimiter); 

    // write out identity information that should be on the stdin
    fprintf(fp, "IDENTITYINFORMATION"); 
    fprintf(fp, delimiter); 


    while (fgets(chBuff, BUFSIZ, stdin) != NULL) {
        //printf("%s", buff);
        if(getDebug()) {
            // send string to stdout
            printf("\n\n\n>>STDIN Number Read in: %d - %s", strlen(chBuff), chBuff); fflush(stdout);
        }
        // parse for carrage return
        startChar=strtok(chBuff, "\n");
        while (startChar != NULL) {

            if(getDebug()) {
                // send string to pipe for child process
                printf("Writing Identity element to input pipe - %s", startChar); fflush(stdout);
            }
            fprintf(fp, "%s", startChar); 
            fprintf(fp, delimiter); 

            // get next token, if exists
            startChar=strtok(NULL, "\n");
        }
    }

    // close write fp
    fclose(fp);

    // hang until process exits if in verbose mode
    if(iVerbose || iLauncherReturn) {
        // wait to exit process
        while(iRet=wait((int*) 0)) {
            if (iRet != pid) {
                break;
            }
        }
    }
    
    if(getDebug()) printf("\nChildProcess returned with and exit code = %d\n", iRet); fflush(stdout);
    return iRet;
}
