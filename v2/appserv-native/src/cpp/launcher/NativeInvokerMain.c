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

//#include <jni.h>
#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#include "processLauncher.h" 

int _debugProg=0;

int invokeProcess(char *scriptFile, char *childExecutorFile, int holdService);
int getDebug(void);
void printDebug(char *message);

/*
This is a simple main program to call the methods intended to be used by the windows service 
and by the native launcher
*/

int main(int argc, char *argv[])
{
    // turn on debugging if necessary
    char *debugx=getenv("DEBUGX");
    char *startservScript;
    int ii=0, commandSize=0, iRet=0;

    // fix for bug# 6356910
    // To make Launcher bourne shell friendly by disassociating from 
    // controlling tty. Otherwise DAS and NAs will be killed after logout.
    #ifndef WIN32
    setsid();
    #endif
    
    if (debugx != NULL) {
        _debugProg=1;
        printf("\n*** The Debug flag for the Native Launcher has be turned ON ***\n");
    }

    // call the invoker with the startserv script that contains the display option
	// the int TRUE tells the invoker to execute the destroy on the jvm, which
	// holds until all the threads in the jvm are terminiated.
	if (getDebug()) {
	    printf("\nCalling invokeProcess with %d args");
	    for(ii=0; ii < argc; ii++) {
	        printf("\narg %d = %s", ii, argv[ii]);
	    }
	    fflush(stdout);
	}
	
	// need at least 3 args
	if (argc < 3) {
	    printf("Usage: appservLauncher childLauncher startserv_script_with_args");
	    exit(1);
	}

    // check to see if need to concat args	
	if (argc > 3) {
	    // should only be 3, if more, then it could be unix script anomally where
	    // it seems that only 80 chars can be put in each args
	    // loop and concat all the remaining args into on string for execution
	    
	    // get max size
	    for(ii=2; ii < argc; ii++) {
	        // also add spaces that are between args
	        commandSize+= (strlen(argv[ii]) + 1);
	    }
	    
	    // alloc memory, DO NOT FREE THIS MEMORY it is used by childprocess when parent exits
        startservScript=(char *) calloc((commandSize + 1), sizeof(char));
	    // get max size
	    for(ii=2; ii < argc; ii++) {
	        strcat(startservScript, argv[ii]);
	        if( ii < (argc -1)) {
	            // add space between args
	            strcat(startservScript, " ");
	        }
	    }
	    
	} else {
	    // no run over on script args
	    startservScript=argv[2];
	}
	
	if (getDebug()) {
	    printf("\n\nCalling invokeProcess with childExecutor: \n->%s<-\n and startserv script: \n->%s<-\n", argv[1], startservScript);
	    fflush(stdout);
	}

	iRet=invokeProcess(startservScript, argv[1], TRUE);
        if (getDebug()) printf("\nNativeInvokerMain exit code ->%d\n", iRet); fflush(stdout);
        exit(iRet);
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
