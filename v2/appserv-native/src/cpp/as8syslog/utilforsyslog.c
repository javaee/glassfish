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

#include <grp.h>
#include <pwd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <syslog.h>
#include <unistd.h>
#include <security/pam_appl.h>

#ifndef STANDALONE
#include <jni.h>
#include "com_sun_enterprise_server_logging_SystemLogHandler.h"
#endif


/*
 * Class:     UtilForSyslog
 * Method:    logMessage
 * Signature:
 */
JNIEXPORT void JNICALL Java_com_sun_enterprise_server_logging_SystemLogHandler_logMessage
(JNIEnv *env, jobject obj, jstring fName, jstring level, jstring message) 
{

    static char *SEVERE = "SEVERE";
    static char *WARNING = "WARNING";
    static char *INFO = "INFO";
    static char *CONFIG = "CONFIG";
    static char *FINE = "FINE";
    static char *FINER = "FINER";
    static char *FINEST = "FINEST";

    const char *fileName = (*env)->GetStringUTFChars(env, fName, 0);
    const char *loggedLevel = (*env)->GetStringUTFChars(env, level, 0);
    const char *loggedMessage = (*env)->GetStringUTFChars(env, message, 0);

    if( strcmp( loggedLevel, SEVERE ) == 0 ) {
        openlog (fileName, LOG_NDELAY, LOG_DAEMON);
        syslog (LOG_ERR,  loggedMessage );
        closelog ();
    } else if( strcmp( loggedLevel, WARNING ) == 0 ) {
        openlog (fileName, LOG_NDELAY, LOG_DAEMON);
        syslog (LOG_WARNING,  loggedMessage );
        closelog ();
    } else if( strcmp( loggedLevel, INFO ) == 0 ) {
        openlog (fileName, LOG_NDELAY, LOG_DAEMON);
        syslog (LOG_INFO,  loggedMessage );
        closelog ();
    } else if( ( strcmp( loggedLevel, CONFIG ) == 0 )
             ||( strcmp( loggedLevel, FINE ) == 0 )
             ||( strcmp( loggedLevel, FINER ) == 0 )
             ||( strcmp( loggedLevel, FINEST) == 0 ) )
    {
        openlog (fileName, LOG_NDELAY, LOG_MAIL); // LOG_CONS | LOG_PID |
        syslog (LOG_DEBUG,  loggedMessage );
        closelog ();
    }
    (*env)->ReleaseStringUTFChars(env, fName, fileName);
    (*env)->ReleaseStringUTFChars(env, level, loggedLevel);
    (*env)->ReleaseStringUTFChars(env, message, loggedMessage);
}
