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
#ifdef XP_WIN32
#include <conio.h>
#else
#include <unistd.h>
#include <termios.h>
#include <string.h>
#endif
#include "com_sun_enterprise_cli_framework_CliUtil.h"
#include "com_sun_enterprise_util_natives_NativeUtils.h"
//C++ include
#include <vector>

#ifndef Darwin
#define bzero(b,len) (memset((b), '\0', (len)), (void) 0)
#endif

using namespace std;

#if defined(__APPLE__) && defined(__DYNAMIC__)
#include<crt_externs.h>
static char ** environ;
#else
extern char ** environ; // Standard environment definition
#endif 


/*
 * Class:     CliUtil
 * Method:    getStringEnv
 * Signature: ()Ljava/lang/String;
 */
vector<char*> getStringEnv(const char *prefix)
{
    int ii=0;
    vector<char*> vEnv;
        /* const char* chName = "AS_ADMIN_"; */
    const char* chName = prefix;

#if defined(__APPLE__) && defined(__DYNAMIC__)
    environ = *_NSGetEnviron();
#endif

    int len = strlen(chName);
    while (environ[ii] != NULL) {
      if (strncmp(chName, (char*)environ[ii], len ) == 0) {
         vEnv.push_back((char*)environ[ii]);
      }
      ii++;
   }
   return vEnv;
}


/*
 * Class:     CliUtil
 * Method:    getEnv
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_sun_enterprise_cli_framework_CliUtil_getEnv (JNIEnv *env, jobject obj, jstring prefix)
{
  const char *envPrefix = env->GetStringUTFChars(prefix, 0);
  vector<char*> vString = getStringEnv(envPrefix);
  
  /*avoid memory leak in JVM so need to call ReleaseStringUTFChars */
  env->ReleaseStringUTFChars(prefix, 0);
  
  jclass stringClass = env->FindClass("java/lang/String");
  jobjectArray retArr = env->NewObjectArray(vString.size(), stringClass, 0);
  char* str;
  jstring javaString;
  /* create object array */
  for (int ii=0; ii<vString.size(); ++ii) {
    str = vString[ii];
    //the length of AS_ADMIN_ is length 9
    //printf("str = %s\n", str+9);
    javaString = env->NewStringUTF(str);
    env->SetObjectArrayElement(retArr, ii, javaString);
  }
  return retArr;
}


#ifdef XP_UNIX

#ifdef Darwin

#include "readpassphrase.h"

    static jstring
getPasswordDarwin(JNIEnv *env)
{
    char    buf[128];
    jstring result  = NULL;
    char*   pw  = readpassphrase( "", buf, sizeof(buf), RPP_ECHO_OFF );
    if ( pw != NULL )
    {
        result  = env->NewStringUTF(buf);
    }
    
    bzero( buf, sizeof(buf));
    return result;
}

#else
   static jstring
getPassword_getpass(JNIEnv *env)
{
    #ifdef LINUX
    char *buf = (char *)getpass("");
    #else
    char *buf = (char *)getpassphrase("");
    #endif
 
    jstring result  = NULL;
    if ( buf != NULL )
    {
        result  = env->NewStringUTF(buf);
        bzero( buf, strlen(buf));
    }
    return( result );
}
#endif

#else //XP_UNIX

    static jstring
getPassword_manually(JNIEnv *env)
{
    char passwd[80];
    int i = 0;
    int ch;

    while (1) {
        if (i >= sizeof(passwd) - 1)
            break;

        ch = _getch();

        if (ch == '\r')
            break;
    
        switch(ch) {
        case 0x08:
   
            if (i > 0)
                i--;
            break;
        default:
            passwd[i++] = ch;
            break;
        }
    }
    passwd[i] = '\0';
    _putch('\n');
    
    jstring result  = env->NewStringUTF(passwd);
    bzero( passwd, sizeof(passwd) );
    
    return result;
}

#endif //XP_UNIX


/*
 * Class:     CliUtil
 * Method:    getPassword
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_sun_enterprise_cli_framework_CliUtil_getPassword
  (JNIEnv *env, jobject obj)
{

    jstring result  = NULL;
#ifdef XP_UNIX
#ifdef Darwin
    result  = getPasswordDarwin(env);
#else // Darwin
    result  = getPassword_getpass(env);
#endif // Darwin
#else
    result  = getPassword_manually( env );
#endif
    return result;
}


/*
 * Class:     CliUtil
 * Method:    getAllEnv
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_sun_enterprise_cli_framework_CliUtil_getAllEnv (JNIEnv *env, jobject obj)
{
    int jj=0;
    vector<char*> vString;

#if defined(__APPLE__) && defined(__DYNAMIC__)
    environ = *_NSGetEnviron();
#endif
    
    while (environ[jj] != NULL) {
        vString.push_back((char*)environ[jj]);
        jj++;
    }
    
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray retArr = env->NewObjectArray(vString.size(), stringClass, 0);
    char* str;
    
    jstring javaString;
    /* create object array */
    for (int ii=0; ii<vString.size(); ++ii) {
        str = vString[ii];
        //the length of AS_ADMIN_ is length 9
        //printf("str = %s\n", str+9);
        javaString = env->NewStringUTF(str);
        env->SetObjectArrayElement(retArr, ii, javaString);
    }
    return retArr;
}


/*
 * Class:     com_sun_enterprise_util_natives_NativeUtils
 * Method:    getPasswordNative
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_sun_enterprise_util_natives_NativeUtils_getPasswordNative(JNIEnv *env, jobject obj)
{
    return Java_com_sun_enterprise_cli_framework_CliUtil_getPassword(env, obj);
}

#ifndef XP_WIN32

static struct termios stored_settings;

void set_keypress(void)
{
    struct termios new_settings;
    tcgetattr(0,&stored_settings);
    new_settings = stored_settings;

    /* Disable canonical mode, and set buffer size to 1 byte */

    new_settings.c_lflag &= (~ICANON);
    new_settings.c_cc[VTIME] = 0;
    new_settings.c_cc[VMIN] = 1;
    
        //disable echoing
    new_settings.c_lflag &= (~ECHO);
    tcsetattr(0,TCSANOW,&new_settings);
    return;
    
}


void reset_keypress(void)
{
    tcsetattr(0,TCSANOW,&stored_settings);
    return;
}
#endif


/*
 * Class:     CliUtil
 * Method:    getKeyboardInput
 * Signature: ()
 */
JNIEXPORT jchar JNICALL Java_com_sun_enterprise_cli_framework_CliUtil_getKeyboardInput
  (JNIEnv *env, jobject obj)
{
#ifdef XP_WIN32
    char ch = getch();
    return (jchar)ch;    
#else
    set_keypress();
    char ch = getchar();
    reset_keypress();
    return (jchar)ch;
#endif

}
