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


#include <windows.h>
#include <stdio.h>
#include <pdh.h>
#include <pdhmsg.h>
#include "com_sun_enterprise_ee_selfmanagement_mbeans_CPU.h"

HQUERY *query;
HCOUNTER counter;

JNIEXPORT jdouble JNICALL Java_com_sun_enterprise_ee_selfmanagement_mbeans_CPU_getCPUUtilPercentFromOS
  (JNIEnv *env, jobject obj)
{
    if ( query == NULL ) {
        // Allocate query structure. XXX Use HeapAlloc instead ?
        query = (HQUERY *)GlobalAlloc(GPTR, sizeof(HQUERY));
                                                                                                                                               
        if ( query == NULL ) {
            printf("Error allocating query");
            return (jdouble)-1;
        }
                                                                                                                                               
        // Create a PDH Query for reading real-time data
        PDH_STATUS status = PdhOpenQuery(NULL, 0, query);
                                                                                                                                               
        if ( status != ERROR_SUCCESS) {
            // XXX Use FormatMessage function to get message for status code
            printf("Error creating Windows PDH query, status = %d\n", status);
            return (jdouble)-1;
        }
                                                                                                                                               
        // Add a counter to the query
        // Object is Processor, Instance is "_Total",
        // Counter is "% processor time"
        LPCTSTR counterName = TEXT("\\Processor(_Total)\\% processor time");
        status = PdhAddCounter(*query, counterName, 0, &counter);
                                                                                                                                               
        if ( status != ERROR_SUCCESS) {
            // XXX Use FormatMessage function to get message for status code
            printf("Error adding counter to Windows PDH query, status = %d\n",
                   status);
            return (jdouble)-1;
        }
                                                                                                                                               
        // Collect first sample to start counter
        PdhCollectQueryData(*query);
    }

    if ( query == NULL ) {
	printf("Error PDH query not initialized, call initializeLoadMetrics before calling getCPUUtilPercentFromOS");
	return (jdouble)-1;
    }

    // Get the counter value
    PDH_STATUS status = PdhCollectQueryData(*query);
    DWORD counterType;
    PDH_FMT_COUNTERVALUE counterValue;
    status = PdhGetFormattedCounterValue(counter, PDH_FMT_DOUBLE, &counterType,
					 &counterValue);

    if ( status != ERROR_SUCCESS ) {
	// XXX Use FormatMessage function to get message for status code
	printf("Error getting Windows PDH counter value, status = %d\n", 
	       status);
	return (jdouble)-1;
    }

    if ( counterValue.CStatus != PDH_CSTATUS_NEW_DATA 
         && counterValue.CStatus != PDH_CSTATUS_VALID_DATA ) {
	// XXX Use FormatMessage function to get message for status code
	printf("Error getting Windows PDH counter value, CStatus = %d\n", 
	       counterValue.CStatus);
	return (jdouble)-1;
    }

    printf("Got PDH processor-time counter value = %f\n", 
	   counterValue.doubleValue);

    /** not needed, the query is open forever till the JVM exits
    PdhCloseQuery(*query);

    GlobalFree(query); // XXX should this be GlobalFree(*query) ?
    **/

    return (jdouble)counterValue.doubleValue;
}
