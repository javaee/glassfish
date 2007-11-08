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

#include <pwd.h>

#ifdef STANDALONE
#include <stdio.h>
#else
#include <jni.h>;
#include "com_sun_enterprise_security_audit_bsm_BSMAuditManager.h";
#endif

int*
nativeGetUidGid(char* user)
{
    struct passwd *pw;
    int ids[2] = { -1, -1};
    if (user != NULL && strlen(user)) {
       pw = getpwnam(user);
       if (pw != NULL) {
          ids[0] = pw->pw_uid;
          ids[1] = pw->pw_gid;
       }
    }
    return ids;
}

#ifndef STANDALONE
JNIEXPORT jintArray JNICALL
Java_com_sun_enterprise_security_audit_bsm_BSMAuditManager_nativeGetUidGid
(JNIEnv *env, jclass class, jstring juser)
{
    int* ids; 
    int* temp; 
    jint* jids;
    jintArray idArray;
    const char *user;
    user = (*env)->GetStringUTFChars(env, juser, NULL);
    ids = nativeGetUidGid(user);

    jids = (jint*)malloc(2 *sizeof(jint));
    temp = ids;
    if (temp != NULL && temp++ != NULL) {
        jids[0] = ids[0];
        jids[1] = ids[1];
    } else {
        // should never be here
        jids[0] = -1;
        jids[1] = -1;
    }
    idArray = (*env)->NewIntArray(env, 2);
    (*env)->SetIntArrayRegion(env, idArray, 0, 2, jids);

    (*env)->ReleaseStringUTFChars(env, juser, user);
    free(jids);
    ids = NULL;
    temp = NULL;

    return idArray;
}
#endif

#ifdef STANDALONE
int
main(int argc, char *argv[])
{
    char *user;
    int* ids;
    int* temp;
    if (argc != 2) {
        printf("Please input a username as argument.\n");
        exit(2);
    }
    user = argv[1];
    printf("user = %s\n", user);
    ids = nativeGetUidGid(user);
    temp = ids;
    if (temp != NULL && temp++ != NULL) {
        printf("uid = %d, gid = %d\n", ids[0], ids[1]);
    } else {
        printf("uid, gid is NULL\n");
    }
}
#endif
