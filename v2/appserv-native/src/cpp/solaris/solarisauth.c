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
#include "com_sun_enterprise_security_auth_realm_solaris_SolarisRealm.h"
#endif

int _getgroupsbymember(const char *username, gid_t gid_array[], int maxgids, int numgids);


/* This needs to be pulled in from somewhere */
#define	SERVICE_NAME	"appserver"

static int
converse(int num_msg, struct pam_message **msgm,
	struct pam_response **response, void *appdata_ptr)
{
	int i;
	struct pam_response *reply;

	if (num_msg <= 0)
		return (PAM_CONV_ERR);

	if ((reply = calloc(num_msg, sizeof (struct pam_response))) == NULL) {
		return (PAM_CONV_ERR);
	}

	for (i = 0; i < num_msg; i++) {
		switch (msgm[i]->msg_style) {
		case PAM_PROMPT_ECHO_OFF:
		case PAM_PROMPT_ECHO_ON:
		case PAM_ERROR_MSG:
		case PAM_TEXT_INFO:
		case PAM_MSG_NOCONF:
		case PAM_CONV_INTERRUPT:
		default:
			break;
		}
		reply[i].resp_retcode = 0;
		reply[i].resp = "";
	}

	*response = reply;

	return (PAM_SUCCESS);
}

static int
authenticate(const char *user, const char *password)
{
	struct pam_conv pam_conv;
	pam_handle_t *pamh = NULL;
	int ret;

	pam_conv.conv = converse;
	pam_conv.appdata_ptr = NULL;

	if ((ret = pam_start(SERVICE_NAME, user, &pam_conv, &pamh)) != PAM_SUCCESS) {
#ifdef STANDALONE
		printf("failed pam_start\n");
#endif
		pam_end(pamh, ret);
		return (ret);
	}

	/*
	 * This doesn't work correctly in Solaris 8 due to bug 4393399, which
	 * was fixed in Solaris 9.
	 */
	if ((ret = pam_set_item(pamh, PAM_AUTHTOK, password)) != PAM_SUCCESS) {
#ifdef STANDALONE
		printf("failed pam_set_item\n");
#endif
                pam_end(pamh, ret);
		return (ret);
	}

	if ((ret = pam_authenticate(pamh, 0)) != PAM_SUCCESS) {
#ifdef STANDALONE
		printf("failed pam_authenticate: %s\n", pam_strerror(pamh, ret));
#endif
		pam_end(pamh, ret);
		return (ret);
	}

	if ((ret = pam_acct_mgmt(pamh, 0)) != PAM_SUCCESS) {
#ifdef STANDALONE
		printf("failed pam_acct_mgmt: %s\n", pam_strerror(pamh, ret));
#endif
		pam_end(pamh, ret);
		return (ret);
	}

	/* If we get here, we're all ok */
#ifdef STANDALONE
	printf("succeeded!\n");
#endif

	pam_end(pamh, ret);

	return (0);
}

static int
digits(int i)
{
	int digits = 0;

	while (i /= 10)
		++digits;

	return (digits + 1);
}

static int
nativeGetGroups(const char *user, char ***groups)
{
	struct group *gr;
	struct passwd *pw;
	gid_t *gids = NULL;
	int ngroups, ngroups_max, i, cgroups;

	ngroups_max = sysconf(_SC_NGROUPS_MAX);
	if (ngroups_max <= 0) {
		/* zero is actually a perfectly good value for _SC_NGROUPS_MAX */
		return (-1);
	}

	if ((pw = getpwnam(user)) == NULL) {
		return (-1);
	}

	if ((gids = calloc((uint_t)ngroups_max, sizeof (gid_t))) == 0) {
		return (-1);
	}

	gids[0] = pw->pw_gid;
	ngroups = _getgroupsbymember(user, gids, ngroups_max, 1);
	/* 2 allows names and numbers, though not all names might exist */
	*groups = calloc(2 * ngroups, sizeof (char **));

#ifdef STANDALONE
	(void) printf("%s (%d groups):", user, ngroups);
#endif
	for (i = 0, cgroups = 0; i < ngroups; i++) {
		if ((gr = getgrgid(gids[i]))) {
#ifdef STANDALONE
			(void) printf(" %s", gr->gr_name);
#endif
			(*groups)[cgroups++] = strdup(gr->gr_name);
		}
#ifdef STANDALONE
		(void) printf(" %d", (int) gids[i]);
#endif
		(*groups)[cgroups] = calloc(1, digits(gids[i]));
		sprintf((*groups)[cgroups++], "%d", (int) gids[i]);
	}
#ifdef STANDALONE
	(void) printf("\n");
#endif

	free(gids);

	return (cgroups);
}

#ifndef STANDALONE
static jobjectArray JNICALL
java_GetGroups(JNIEnv *env, const char *user)
{
	char **groups;
	int i, ngroups;
	jstring js;
	jobjectArray jgroups;

	if ((ngroups = nativeGetGroups(user, &groups)) < 0)
		return (NULL);

	jgroups = (*env)->NewObjectArray(env, ngroups,
			(*env)->FindClass(env, "java/lang/String"),
			(*env)->NewStringUTF(env, ""));
	for (i = 0; i < ngroups; i++) {
		js = (*env)->NewStringUTF(env, groups[i]);
		(*env)->SetObjectArrayElement(env, jgroups, i, js);
		free(groups[i]);
	}

	free(groups);

	return (jgroups);
}

/*
 * Class:     Auth
 * Method:    nativeGetGroups
 * Signature: (Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL
Java_com_sun_enterprise_security_auth_realm_solaris_SolarisRealm_nativeGetGroups(JNIEnv *env, jclass class, jstring juser)
{
	const char *user;
	jobjectArray jgroups;

	user = (*env)->GetStringUTFChars(env, juser, NULL);

	jgroups = java_GetGroups(env, user);

	(*env)->ReleaseStringUTFChars(env, juser, user);

	return (jgroups);
}
#endif

static int
nativeAuthenticate(const char *user, const char *password)
{
#ifdef STANDALONE
	printf("Password: \"%s\"\n", password);
#endif
	return (!authenticate(user, password));
}

#ifndef STANDALONE
/*
 * Class:     Auth
 * Method:    nativeAuthenticate
 * Signature: (Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL
Java_com_sun_enterprise_security_auth_realm_solaris_SolarisRealm_nativeAuthenticate(JNIEnv *env, jclass class, jstring juser, jstring jpassword)
{
	const char *user, *password;
	jobjectArray jgroups;

	user = (*env)->GetStringUTFChars(env, juser, NULL);
	password = (*env)->GetStringUTFChars(env, jpassword, NULL);

        jgroups = NULL;
        /* Need to enforce that user name is not empty, or PAM will go
           into a loop calling converse() until we run out of memory. */
        if (user != NULL  && strlen(user) > 0) {
            if (nativeAuthenticate(user, password))
		jgroups = java_GetGroups(env, user);
        }

	(*env)->ReleaseStringUTFChars(env, juser, user);
	(*env)->ReleaseStringUTFChars(env, jpassword, password);

	return (jgroups);
}
#endif

#ifdef STANDALONE
int
main(int argc, char *argv[])
{
	char password[64] = "mypasswd";
	char **groups;

	printf("Password: ");
	scanf("%s", password);
	(void) nativeAuthenticate(argv[1], password);
	(void) nativeGetGroups(argv[1], &groups);
	return (0);
}
#endif
