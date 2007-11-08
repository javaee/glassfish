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

int digestData(char *chBuff, char *delimiter, int *foundSync, int *lineCnt, int *conFlag);
int getDebug(void);
void printDebug(char *message);


/*
Generic return to digest data from the ProcessLauncher invoked with the 
"display" command.
*/
int digestData(char *chBuff, char *delimiter, int *foundSync, int *lineCnt, int *conFlag) { 
    int ii=0, iRet=0;
    char *startChar, *chTemp, *lineTemp;
    char *commandSyncStart = "STARTOFCOMMAND";
    char *commandSyncEnd = "ENDOFCOMMAND";

    if(getDebug()) {
        printf("\n\n IN DIGEST found %d, con %d", *foundSync, *conFlag);
    }

    if (!*foundSync) {
        // find sync leader that comes from ProcessLauncher "STARTOFCOMMAND"
        if(getDebug()) printf("\n\nLooking for Sync..."); fflush(stdout);
        startChar=strstr(chBuff, commandSyncStart);
        if (startChar != NULL)  {
            // found start, so set delimeter
            if (strlen(commandSyncStart) <  strlen(startChar)) {
                // make sure delimiter is attached, else keep default
                if (getDebug()) printf("\nDelimeter %c is set to %c\n", delimiter[0], startChar[strlen(commandSyncStart)]); fflush(stdout);
                // get delimter from passed in command
                delimiter[0]=startChar[strlen(commandSyncStart)];
            }
        *foundSync=TRUE;
        if(getDebug()) printf("\nLooking for Sync, Found string: %s - with delimiter: %s\n", chBuff, delimiter); fflush(stdout);
        }
    }

    // see if sync found
    if (*foundSync) {
        // sync has been found, see if the rest of the buffer of subsequent buffers have delimiters
        // for command to run

        if(getDebug()) {
            printf("\n\nFound Sync, now break into delimiter");
        }
        // copy chBuf in to a structure that can be tokenized into command lines
        // +1 to allow space for '\0'
        chTemp=(char *) malloc(sizeof(char) * (strlen(chBuff) + 1));
        strcpy(chTemp, chBuff);
        //if(getDebug()) printf("\n %s - %s", delimiter, chTemp); fflush(stdout);
        // start tokenization of read buffer
        startChar=strtok(chTemp, delimiter);
        while (startChar != NULL) {
            if(getDebug()) {
                printf("\n\nIN TOKEN LOOP found %d, con %d", *foundSync, *conFlag);
                //printf("\nFound delimiter->%s<-", startChar);
                fflush(stdout);
            }
            // see if matches begin or end of command strings
            // use strstr to find start command, because can't guarentee what is in
            // output stream from startserv script
            if (strstr(startChar, commandSyncStart) == NULL && strcmp(startChar, commandSyncEnd) != 0) {
                // see if need to concatinate lines and that we are beyond the first line
                // Also, make sure first char of newly readin line isn't a delimiter, strtok, skips it
                // so it doesn't go into the loop
                if (*conFlag == TRUE && *lineCnt > 0 && chBuff[0] != delimiter[0]) {
                    // concatinate with last commandLine
                    // get memory big enough for new string
                    lineTemp=(char *) malloc(sizeof(char) * (strlen(commandLine[*lineCnt - 1]) + strlen(startChar) + 1));
                    
                    // copy content of string into newly allocated space
                    lineTemp=strcpy(lineTemp, commandLine[*lineCnt - 1]);
                    
                    // concatinate strings
                    strcat(lineTemp, startChar);
                    
                    // free memory that is currently pointed to by commandline[*lineCnt - 1]
                    free(commandLine[*lineCnt - 1]);

                    // see if this string cat = commandSyncStop which could happen if stop retrieved in parts ???
                    if (strcmp(lineTemp, commandSyncEnd) == 0) {
                        // found stop command, don't add to command

                        // free memory that was just created
                        free(lineTemp);

                        // decrement pointer
                        *lineCnt = *lineCnt -1;

                        // return proper code for cataloging the stop command was found
                        iRet=2;
                    } else {                   
                        // set point back to command array
                        commandLine[*lineCnt - 1]=lineTemp;
                        if(getDebug()) {
                            printf("\nCONCAT:%s",commandLine[*lineCnt - 1]);
                        }
                    }

                } else {
                    // add to new line
                    lineTemp=(char *) malloc(sizeof(char) * strlen(startChar) + 1);
                    // copy content of string into newly allocated space
                    lineTemp=strcpy(lineTemp, startChar);
                    commandLine[*lineCnt]=lineTemp;
                    if(getDebug()) {
                            printf("\nADD:%s",commandLine[*lineCnt]);
                    }
                    *lineCnt = *lineCnt + 1;
                }
            } else {
                // signal that start and/or end of command was found
                // use strstr to find start command, because can't guarentee what is in
                // output stream from startserv script
                if (strstr(startChar, commandSyncStart) != NULL) {
                    iRet=1;
                }
                if (strcmp(startChar, commandSyncEnd) == 0) {
                    if(iRet == 1) {
                        iRet=3;
                    } else {
                        iRet=2;
                    }
                }
            }

            // reset concat after any interactions
            *conFlag=FALSE;

            // get next token, if exists
            startChar=strtok(NULL, delimiter);
        }

        // free chTemp
        free(chTemp);

        // check to see if string ends with a delimiter, if not,
        // is a continuation with the next read
        if(getDebug()) {
            printf("\n***** see if concat %c - %c", delimiter[0], chBuff[strlen(chBuff) - 1]);
        }
        if (chBuff[strlen(chBuff) - 1] != delimiter[0]) {
            // set continiation flag
            if(getDebug()) {
                printf("\nset concat flag to true");
            }
            *conFlag=TRUE;
        }
        
    }

    return iRet;
}
