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
#include <stdarg.h>
#include <sys/types.h>          /* Required by statfs, chmod */
#include <sys/statvfs.h>                /* Required by chmod */
#include <sys/stat.h>
#include <unistd.h> /* for chown  */
#include "com_sun_enterprise_ee_selfmanagement_mbeans_DiskToolkit.h"

#ifdef linux
#include <mntent.h>             /* Required by getmntent */
#include <sys/time.h>           /* Required by gettimeofday */
#include <string.h> /* for strncmp */
#include <limits.h>   /* Defines PATH_MAX */
#include <sys/sysinfo.h>
#endif

#ifdef sparc
#include <strings.h>
#include <sys/mnttab.h>         /* Required by getmntent */
#include <dlfcn.h>              /* Required by dlopen, ... */
#endif

/*
 * Calculates the kilobytes free given the information from
 * a statvfs call.  This process is slightly complicated because
 * interim calculations must not cause the result to overflow.
 * To achieve this, we always try to divide before multiplication.
 * Also, we do the conversion in a way that does not require
 * floating point arithmetic.
 */
static unsigned long
getKilobytesFreeLinux(struct statvfs *statInfo)
{
        if (statInfo->f_bavail == (long)-1) {
                return ((unsigned long) 0);
        } else if (statInfo->f_frsize >= 1024) {
                return (statInfo->f_bavail *
                    (unsigned long) (statInfo->f_frsize / 1024));
        } else {
                return (statInfo->f_bavail /
                    (unsigned long) (1024 / statInfo->f_frsize));
        }
}
                                                                                                                                               
static unsigned long
getKilobytesFreeSparc(struct statvfs64 *statInfo)
{
        if (statInfo->f_bavail == (long)-1) {
                return ((unsigned long) 0);
        } else if (statInfo->f_frsize >= 1024) {
                return (statInfo->f_bavail *
                    (unsigned long) (statInfo->f_frsize / 1024));
        } else {
                return (statInfo->f_bavail /
                    (unsigned long) (1024 / statInfo->f_frsize));
        }
}

JNIEXPORT jlong JNICALL Java_com_sun_enterprise_ee_selfmanagement_mbeans_DiskToolkit_getPartitionFreeSpace
  (JNIEnv *env, jobject object, jstring partitionNameString) {
        long bytesFree = 0L;
        jboolean iscopy;
        const char *str = env->GetStringUTFChars(partitionNameString, &iscopy);                                                                        
#ifdef linux
        struct statvfs statInfo;
        int status = statvfs(str, &statInfo);
        if (status == 0) {
                unsigned long kilobytesFree = (unsigned long)getKilobytesFreeLinux(&statInfo);
                bytesFree = kilobytesFree;
        }
#elif defined sparc
        struct statvfs64 statInfo;
        int status = statvfs64(str, &statInfo);
        if (status == 0) {
                unsigned long kilobytesFree = (unsigned long)getKilobytesFreeSparc(&statInfo);
                bytesFree = kilobytesFree;
        }
#endif
        env->ReleaseStringUTFChars(partitionNameString, str);
        return (jlong) bytesFree;
}

