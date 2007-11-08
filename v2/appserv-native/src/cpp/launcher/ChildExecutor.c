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

#include <jni.h>
#include <string.h>
#include <stdio.h> 
#include <stdlib.h> 
#ifdef XP_WIN32
#include <io.h>
#endif
#include <fcntl.h>
#include "processLauncher.h"

char *commandLine[COMMAND_LINE_SIZE];
JavaVM *jvm;
int _debugProg=0;

int getJavaStartCommand(char *scriptFile); 
void errorExit(char*); 
int invokeProcess(char *scriptFile, int holdService); 
void createProcessTest(char *scriptFile);
int executeStop(char *scriptFile);
int digestData(char *chBuff, char *delimiter, int *foundSync, int *lineCnt, int *conFlag);
int getDebug(void);
void printDebug(char *message);

/*
This is the actual program that executes the java command and redirects the log, if not in verbose mode.
*/
int main(int argc, char *argv[]) {
    JNIEnv *env;
    jint res=0;
    jclass cls;
    jmethodID mid;
    jstring jstr;
    jclass stringClass;
    jobjectArray args;
    int ii=0, jj=0, iStart=0, iLength=0, numberOfJvmOptions=0, numberOfArgs=0, numberOfIdentity=0;
    char *className, *strPointer, *defaultLogFile=NULL;
    char *commandLineArgs[100];
    char *identityArgs[10];
    int iRet=0, startOfCommandFound=FALSE, endOfCommandFound=FALSE;
    int foundSync=FALSE, conFlag=FALSE;
    int *pfoundSync, *plineCnt, *pconFlag;
    char chBuff[BUFSIZ], delimiter[2]="\0\0";
    char *pchBuff, *pdelimiter;
    int lineCnt=0, iVerbose=0, iLauncherReturn=0;
    JavaVMInitArgs vm_args;
    JavaVMOption options[200];
    FILE *fp;

    /*
    HANDLE tempHandle;
    BOOL bFlag;
    DWORD dwWritten;
    */

    // turn on debugging if necessary
    char *debugx=getenv("DEBUGX");
    if (debugx != NULL) {
        _debugProg=1;
        printf("\n\n\n\n************************************************************************");
        printf("\n*** The Debug flag for the Native Launcher has be turned ON ***\n");
        printf("\nRunning start up of Child executor for Native Launcher!!");
    }

    // set memory to NULL
    memset(commandLine, '\0', 1); 
    memset(chBuff, '\0', 1); 
    memset(commandLineArgs, '\0', 1); 
    memset(identityArgs, '\0', 1); 

    // Read output from the child process, and write to parent's STDOUT. 
    lineCnt=0;

    // set buffer to pointer
    pchBuff=chBuff;
    pdelimiter = delimiter;
    pfoundSync = &foundSync;
    plineCnt = &lineCnt;
    pconFlag = &conFlag;

    while (fgets(pchBuff, BUFSIZ, stdin) != NULL) {
        //printf("%s", buff);
        if(getDebug()) printf("\n\n\n>>Number Read in: %d\n%s", strlen(pchBuff), pchBuff); fflush(stdout);
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
        // need to fix code so it can handle when tokens come in as broken segments ???
        errorExit("\nERROR: Either the Start or End command tokens where not found by the native launcher. Set the \"DEBUGX\" environment variable and re-run to debug the problem.\n");
    }

    if(getDebug()) {
        for(ii=0; ii < lineCnt; ii++) {
            printf("\n%d = %s", ii, commandLine[ii]);
        }
        fflush(stdout);
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
        if(strncmp(commandLine[ii], "-Dcom.sun.aas.defaultLogFile=", 28) == 0) {
            // see if there is a default log set to send standard input and error
            if(getDebug()) printf("\n *** found default log malloc %d  --   %s", strlen(commandLine[ii]), commandLine[ii]); fflush(stdout);
            // alloc memory for new string that will set environment variable
            //defaultLogFile=(char *) malloc(sizeof(char) * (strlen(commandLine[ii]) + 1) );
            defaultLogFile=(char *) calloc((strlen(commandLine[ii]) + 1), sizeof(char)); 
            iLength=strlen(commandLine[ii]);
            for(jj=29; jj <= iLength; jj++) {
                defaultLogFile[jj - 29]=commandLine[ii][jj];
            }
            if(getDebug()) printf("\n*** default log location to %s", defaultLogFile); fflush(stdout);
            
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
        }

        // add arguments to jvm options
        options[numberOfJvmOptions].optionString=commandLine[ii];
        if(getDebug()) printf("\nJVM & System arg:%s", commandLine[ii]); fflush(stdout);
        numberOfJvmOptions++;
        ii++;
    }

    // redirect threaddump to log if not in verbose node
    if((!iVerbose || iLauncherReturn) && defaultLogFile != NULL) {
        printf("stdout and stderr has be redirected to %s", defaultLogFile); fflush(stdout);
        fp = fopen(defaultLogFile, "a");
        dup2(fileno(fp), fileno(stdout));
        dup2(fileno(fp), fileno(stderr));
        if (getDebug()) printf("stdout and stderr has be redirected to %s", defaultLogFile); fflush(stdout);
    }
    // free space, shouldn't need it anymore
    free(defaultLogFile);


    // get main java class
    className=commandLine[ii];
    // for jni invocation api, need to change ". to "/" in package name path
    while((strPointer=strpbrk(className,".")) != NULL) {
        *strPointer='/';
    }
    if(getDebug()) printf("\nClass to execute: %s", className); fflush(stdout);
    ii++;

    // loop and retrieve command line arguments until hit a -
    while (commandLine[ii] != NULL) {

        // check to see in identity info also came in with command
        if (strcmp(commandLine[ii], "IDENTITYINFORMATION") == 0) {
            ii++;
            break;
        }

        commandLineArgs[numberOfArgs]=commandLine[ii];
        if(getDebug()) printf("\nCommand Args:%s", commandLine[ii]); fflush(stdout);
        numberOfArgs++;
        ii++;
    }

    // loop see if any identity information is present
    while (commandLine[ii] != NULL) {
        identityArgs[numberOfIdentity]=commandLine[ii];
        if(getDebug()) printf("\nIdentity Args:%s", commandLine[ii]); fflush(stdout);
        numberOfIdentity++;
        ii++;
    }
    // finalize identity args with a null
    identityArgs[numberOfIdentity]=NULL;

    vm_args.version = JNI_VERSION_1_4;
    vm_args.options = options;
    vm_args.nOptions = numberOfJvmOptions;
    vm_args.ignoreUnrecognized = JNI_TRUE;
    
    printDebug("\nCreating vm ...\n");
    // Create the Java VM
    res = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
	if(getDebug()) {
	    printf("Creating vm return = %i\n", res);
    }

    // if jvm isn't created then error, better propagation ???
    if (res < 0) {
        errorExit("\nCan't create Java VM\n");
    }


    printDebug("Finding IdentityManager...");
    // *************************************
    // find identity class so passed in information can be stored
    cls = (*env)->FindClass(env, "com/sun/enterprise/security/store/IdentityManager");
    if (cls == NULL) {
        goto destroy;
    }

    //
    // get static method id for getUser
    if(getDebug()) printf("Setting identity user to %s\n", identityArgs[0]); fflush(stdout);
    mid = (*env)->GetStaticMethodID(env, cls, "setUser", "(Ljava/lang/String;)V");
    if (mid == NULL) {
        goto destroy;
    }

    jstr = (*env)->NewStringUTF(env, identityArgs[0]);
    if (jstr == NULL) {
        goto destroy;
    }

    // call the static main method
    (*env)->CallStaticVoidMethod(env, cls, mid, jstr);

    // recapture memory
    (*env)->DeleteLocalRef(env, jstr);

    //
    // get static method id for getPassword
    if(getDebug()) printf("Setting identity password to %s\n", identityArgs[1]); fflush(stdout);
    mid = (*env)->GetStaticMethodID(env, cls, "setPassword", "(Ljava/lang/String;)V");
    if (mid == 0) {
        goto destroy;
    }

    jstr = (*env)->NewStringUTF(env, identityArgs[1]);
    if (jstr == 0) {
        goto destroy;
    }

    // call the static main method
    (*env)->CallStaticVoidMethod(env, cls, mid, jstr);

    // recapture memory
    (*env)->DeleteLocalRef(env, jstr);

    //
    // get static method id for getMasterPassword
    if(getDebug()) printf("Setting identity masterPassword to %s\n", identityArgs[2]); fflush(stdout);
    mid = (*env)->GetStaticMethodID(env, cls, "setMasterPassword", "(Ljava/lang/String;)V");
    if (mid == 0) {
        goto destroy;
    }

    jstr = (*env)->NewStringUTF(env, identityArgs[2]);
    if (jstr == 0) {
        goto destroy;
    }

    // call the static main method
    (*env)->CallStaticVoidMethod(env, cls, mid, jstr);

    // recapture memory
    (*env)->DeleteLocalRef(env, jstr);

    //
    // now put in extra tokenized data for security, if present
    mid = (*env)->GetStaticMethodID(env, cls, "putTokenizedString", "(Ljava/lang/String;)V");
    if (mid == 0) {
        goto destroy;
    }

    // initialize to point after first know 3 identiy items
    jj=3;
    while (identityArgs[jj] != NULL) {

        if(getDebug()) printf("Setting identity token to %s\n", identityArgs[jj]); fflush(stdout);
        jstr = (*env)->NewStringUTF(env, identityArgs[jj]);
        if (jstr == 0) {
            goto destroy;
        }

        // call the static main method
        (*env)->CallStaticVoidMethod(env, cls, mid, jstr);

        // recapture memory
        (*env)->DeleteLocalRef(env, jstr);

        jj++;
    }

    // delete reference to IndentityManager
    if (cls == 0) {
        (*env)->DeleteLocalRef(env, cls);
    }

    // *************************************
    // now find the main program to be executed
    cls = (*env)->FindClass(env, className);
    if (cls == 0) {
        goto destroy;
    }
 
    // get static method id
    mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
    if (mid == 0) {
        goto destroy;
    }
    
    // make a new string
    jstr = (*env)->NewStringUTF(env, "");
    if (jstr == 0) {
        goto destroy;
    }
    stringClass = (*env)->FindClass(env, "java/lang/String");
    args = (*env)->NewObjectArray(env, numberOfArgs, stringClass, jstr);
    if (args == 0) {
        goto destroy;
    }
    
    // populate args
    for(ii=0; ii < numberOfArgs; ii++) {
        jstring argstr = (*env)->NewStringUTF(env, commandLineArgs[ii]);
        if (argstr == 0) {
            goto destroy;
        }
        (*env)->SetObjectArrayElement(env, args, ii, argstr);
        (*env)->DeleteLocalRef(env, argstr);
    }
    
    if(getDebug()) printf("Created args array of size %d\n", numberOfArgs); fflush(stdout);
    
    // call the static main method
    (*env)->CallStaticVoidMethod(env, cls, mid, args);

destroy:
    // incase there is an error in jvm creation, cleanup errors, better propagation ???
    printDebug("Deleting JNI references...\n");
    if ((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
    }
    
    if (cls == 0) {
        (*env)->DeleteLocalRef(env, cls);
    }
    if (jstr == 0) {
        (*env)->DeleteLocalRef(env, jstr);
    }
    if (stringClass == 0) {
        (*env)->DeleteLocalRef(env, stringClass);
    }
    if (args == 0) {
        (*env)->DeleteLocalRef(env, args);
    }


    // destroy, which will only return when
    // all the thread in the jvm are exited
    printDebug("Waiting on Destroy of JVM ...\n");
    res=(*jvm)->DestroyJavaVM(jvm);
    if(getDebug()) printf("\nReceived exit code from destroy = %d\n", res); fflush(stdout);
    return res;
}



//VOID errorExit (LPTSTR lpszMessage) { 
void errorExit (char *lpszMessage) { 
    fprintf(stderr, "\n\n*** EXITING ERROR: %s ***\n\n", lpszMessage); 
    // sleep so they can see it
    #ifdef XP_WIN32
        _sleep(20000);
    #else
        sleep(20000);
    #endif
    exit(1); 
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

