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

/*
 * The provides the functionality of reading NSS database and return
 * the results in PKCS12 for keys and PKCS7 format for certificates.
 * The logic for conversion to PKCS12 is adapted from pk12util in
 * NSS tools.
 * @author Shing Wai Chan
 */

#include "com_sun_enterprise_ee_security_NssStore.h"

//in dist/public/seccmd
#include "seccomon.h"
#include "secerr.h"
#include <stdio.h>

//in dist/public/nss
#include "pk11func.h"
#include "pkcs12.h"
#include "p12plcy.h"
#include "nss.h"
#include "certdb.h"
#include "secmodt.h"
#include "prerror.h"
#include "prmem.h"
#include "secmod.h"

const int defaultBufferSize = 3000;

PRBool pk12_debugging = PR_FALSE;
char *errMsg = NULL;

typedef struct p12uContextStr {
        unsigned char*    buffer;
        int32             size;
        int32             bufSize;
} p12uContext;

//secuPWData is used to be defined in secutil.h. But this header file
//is gone since NSS3.10.
//we copied the secuPWData typedef from SECURITY-3_9_5_RTM.
typedef struct {
    enum {
	PW_NONE = 0,
	PW_FROMFILE = 1,
	PW_PLAINTEXT = 2,
	PW_EXTERNAL = 3
    } source;
    char *data;
} secuPWData;

#ifdef NSSDEBUG
FILE *debugFp = NULL;
char tempBuf[512]; //assuming all sprintf debug message can be fitted into tempBuf

static void
debug(char* msg)
{
    printf(msg);
    fprintf(debugFp, msg);
    fflush(debugFp);
}
#endif

static p12uContext *
allocateP12uContext(PRIntn bufSize)
{
    p12uContext *p12cxt;
    unsigned char *buf;

    p12cxt = (p12uContext *)PORT_ZAlloc(sizeof(p12uContext));
    if (!p12cxt) {
	PR_SetError(SEC_ERROR_NO_MEMORY, 0);
	return NULL;
    }

    buf = (unsigned char*)PORT_ZAlloc(bufSize);
    if(!buf) {
        PR_Free(p12cxt);
	PR_SetError(SEC_ERROR_NO_MEMORY, 0);
	return NULL;
    }

    p12cxt->buffer = buf;
    p12cxt->size = 0;
    p12cxt->bufSize = bufSize;

    return p12cxt;
}

void
freeP12uContext(p12uContext *p12cxt)
{
#ifdef NSSDEBUG
    debug("Free p12cxt\n");
#endif
    if (p12cxt != NULL) {
       if (p12cxt->buffer != NULL) {
           PR_Free(p12cxt->buffer);
           p12cxt->buffer = NULL;
       }
       PR_Free(p12cxt);
    }
}

char *
SECU_GetModulePassword(PK11SlotInfo *slot, PRBool retry, void *arg)
{
    char *pwd = (char *)arg;

    if (retry == PR_FALSE && pwd != NULL) {
#ifdef NSSDEBUG
        char* tempVar = PK11_GetTokenName(slot);
        if (tempVar == NULL) {
            tempVar = "internal";
        }
        sprintf(tempBuf, "slot %s with pwd %s \n", tempVar, pwd);
        debug(tempBuf);
#endif
        //this will be released in pk11slot.c or
        //pk11auth.c in later version of NSS
        return PORT_Strdup(pwd);
    } else {
        return NULL;
    }
}

PK11SlotInfo *
getSlot(char* tokenName)
{
    PK11SlotInfo *slot = NULL;
    if (tokenName != NULL) {
#ifdef NSSDEBUG
        sprintf(tempBuf, "Get slot %s\n", tokenName);
        debug(tempBuf);
#endif
        slot = PK11_FindSlotByName(tokenName);
    } else {
#ifdef NSSDEBUG
        debug("Get internal slot\n");
#endif
        slot = PK11_GetInternalKeySlot();
    }
    return slot;
}

static SECStatus
p12u_SwapUnicodeBytes(SECItem *uniItem)
{
    unsigned int i;
    unsigned char a;
    if((uniItem == NULL) || (uniItem->len % 2)) {
	return SECFailure;
    }
    for(i = 0; i < uniItem->len; i += 2) {
	a = uniItem->data[i];
	uniItem->data[i] = uniItem->data[i+1];
	uniItem->data[i+1] = a;
    }
    return SECSuccess;
}

static PRBool
p12u_ucs2_ascii_conversion_function(PRBool	   toUnicode,
				    unsigned char *inBuf,
				    unsigned int   inBufLen,
				    unsigned char *outBuf,
				    unsigned int   maxOutBufLen,
				    unsigned int  *outBufLen,
				    PRBool	   swapBytes)
{
    SECItem it = { 0 };
    SECItem *dup = NULL;
    PRBool ret;
    /* If converting Unicode to ASCII, swap bytes before conversion
     * as neccessary.
     */
    if (pk12_debugging) {
	int i;
	printf("Converted from:\n");
	for (i=0; i<inBufLen; i++) {
	    printf("%2x ", inBuf[i]);
	    /*if (i%60 == 0) printf("\n");*/
	}
	printf("\n");
    }
    it.data = inBuf;
    it.len = inBufLen;
    dup = SECITEM_DupItem(&it);
    if (!toUnicode && swapBytes) {
	if (p12u_SwapUnicodeBytes(dup) != SECSuccess) {
	    SECITEM_ZfreeItem(dup, PR_TRUE);
	    return PR_FALSE;
	}
    }
    /* Perform the conversion. */
    ret = PORT_UCS2_UTF8Conversion(toUnicode, dup->data, dup->len,
                                   outBuf, maxOutBufLen, outBufLen);
    if (dup)
	SECITEM_ZfreeItem(dup, PR_TRUE);
    /* If converting ASCII to Unicode, swap bytes before returning
     * as neccessary.
     */
#if 0
    if (toUnicode && swapBytes) {
	it.data = outBuf;
	it.len = *outBufLen;
	dup = SECITEM_DupItem(&it);
	if (p12u_SwapUnicodeBytes(dup) != SECSuccess) {
	    SECITEM_ZfreeItem(dup, PR_TRUE);
	    return PR_FALSE;
	}
	memcpy(outBuf, dup->data, *outBufLen);
	SECITEM_ZfreeItem(dup, PR_TRUE);
    }
#endif
    if (pk12_debugging) {
	int i;
	printf("Converted to:\n");
	for (i=0; i<*outBufLen; i++) {
	    printf("%2x ", outBuf[i]);
	    /*if (i%60 == 0) printf("\n");*/
	}
	printf("\n");
    }
    return ret;
}

SECStatus
P12U_UnicodeConversion(PRArenaPool *arena, SECItem *dest, SECItem *src,
		       PRBool toUnicode, PRBool swapBytes)
{
    unsigned int allocLen;
    if(!dest || !src) {
	return SECFailure;
    }
    allocLen = ((toUnicode) ? (src->len << 2) : src->len);
    if(arena) {
	dest->data = PORT_ArenaZAlloc(arena, allocLen);
    } else {
	dest->data = PORT_ZAlloc(allocLen);
    }
    if(PORT_UCS2_ASCIIConversion(toUnicode, src->data, src->len,
				 dest->data, allocLen, &dest->len,
				 swapBytes) == PR_FALSE) {
	if(!arena) {
	    PORT_Free(dest->data);
	}
	dest->data = NULL;
	return SECFailure;
    }
    return SECSuccess;
}

/*
 *
 */
SECItem *
P12U_GetP12FilePassword(secuPWData *p12FilePw)
{
    SECItem *pwItem = NULL;

    if (p12FilePw != NULL && p12FilePw->source != PW_NONE) {
        /* Plaintext */
        pwItem = SECITEM_AllocItem(NULL, NULL, PORT_Strlen(p12FilePw->data) + 1);
        memcpy(pwItem->data, p12FilePw->data, pwItem->len);
    }

    return pwItem;
}

static void
p12u_WriteToExportArray(void *arg, const char *buf, unsigned long len)
{
    p12uContext *p12cxt = arg;
    int32 writeLen = (int32)len;
    int32 i = 0;
    unsigned char* totalBuf = p12cxt->buffer;
    int32 size = p12cxt->size;
    int32 bufSize = p12cxt->bufSize;
    int32 expandedSize = bufSize;

    //expand buffer if necessary
    //I try to put this loop inside if block below. It does not comply in
    //Solaris env. I think there is a bug in compiler.
    while (size + writeLen > expandedSize) {
        expandedSize += defaultBufferSize;
    }

#ifdef NSSDEBUG
    if (expandedSize > bufSize) {
        debug("growing p12cxt buffer\n");
    }
#endif

    if (expandedSize > bufSize) {
        unsigned char* newBuf = (unsigned char*)PORT_ZAlloc(expandedSize);
        if (newBuf == NULL) {
            errMsg = "Cannot allocate expanded buffer.";
            return;
        }
        //copy data of unsigned char*
        for (i = 0; i < size; i++) {
            newBuf[i] = totalBuf[i];
        }

        PR_Free(totalBuf);
        p12cxt->buffer = newBuf;
        p12cxt->bufSize = expandedSize;
        totalBuf = newBuf;
    }
    
    for (i = 0; i <  writeLen; i++) {
        totalBuf[i + size] = buf[i];
    }

    p12cxt->size = size + writeLen;
}

p12uContext*
P12U_ExportPKCS12Object(char *nn, PK11SlotInfo *inSlot,
			secuPWData *slotPw, secuPWData *p12FilePw)
{
    SEC_PKCS12ExportContext *p12ecx = NULL;
    SEC_PKCS12SafeInfo *keySafe = NULL, *certSafe = NULL;
    SECItem *pwitem = NULL;
    p12uContext *p12cxt = NULL;
    CERTCertList* certlist = NULL;
    CERTCertListNode* node = NULL;
    PK11SlotInfo* slot = NULL;
    unsigned char *buf = NULL;

#ifdef NSSDEBUG
    debug("Finding Cert\n");
#endif
    certlist = PK11_FindCertsFromNickname(nn, slotPw);
    if(!certlist) {
        errMsg = "find user certs from nickname failed";
	return NULL;
    }

    //In later version of NSS, one can check the following:
    /*
    if ((SECSuccess != CERT_FilterCertListForUserCerts(certlist)) ||
        CERT_LIST_EMPTY(certlist)) {
    */
    //CERT_LIST_EMPTY is defined in some more recent version of certt.h 
    if (CERT_LIST_END(CERT_LIST_HEAD(certlist), certlist)) {
        errMsg = "no user certs from given nickname";
        goto loser;
    }

    /*	Password to use for PKCS12 file.  */
    pwitem = P12U_GetP12FilePassword(p12FilePw);
    if(!pwitem) {
	goto loser;
    }

    p12cxt = allocateP12uContext(defaultBufferSize);
    if(!p12cxt) {
        errMsg = "Initialization failed.";
	goto loser;
    }

#ifdef NSSDEBUG
    debug("Processing certlist\n");
#endif
    if (certlist) {
        CERTCertificate* cert = NULL;
        node = CERT_LIST_HEAD(certlist);
        if (node) {
            cert = node->cert;
        }
        if (cert) {
            slot = cert->slot; /* use the slot from the first matching
                certificate to create the context . This is for keygen */
        }
    }
    if (!slot) {
        errMsg = "cert does not have a slot";
        goto loser;
    }

#ifdef NSSDEBUG
    debug("Create export context\n");
#endif
    p12ecx = SEC_PKCS12CreateExportContext(NULL, NULL, slot, slotPw);
    if(!p12ecx) {
        errMsg = "export context creation failed";
        goto loser;
    }

#ifdef NSSDEBUG
    debug("Add password integrity\n");
#endif
    if(SEC_PKCS12AddPasswordIntegrity(p12ecx, pwitem, SEC_OID_SHA1)
       != SECSuccess) {
        errMsg = "PKCS12 add password integrity failed";
        goto loser;
    }

#ifdef NSSDEBUG
    debug("Looping cert list\n");
#endif
    for (node = CERT_LIST_HEAD(certlist);!CERT_LIST_END(node, certlist);node=CERT_LIST_NEXT(node))
    {
        CERTCertificate* cert = node->cert;
        if (!cert->slot) {
            errMsg = "cert does not have a slot";
            goto loser;
        }
    
        keySafe = SEC_PKCS12CreateUnencryptedSafe(p12ecx);
        if(/*!SEC_PKCS12IsEncryptionAllowed() || */ PK11_IsFIPS()) {
            certSafe = keySafe;
        } else {
            certSafe = SEC_PKCS12CreatePasswordPrivSafe(p12ecx, pwitem,
                SEC_OID_PKCS12_V2_PBE_WITH_SHA1_AND_40_BIT_RC2_CBC);
        }
    
        if(!certSafe || !keySafe) {
            errMsg = "key or cert safe creation failed";
            goto loser;
        }
    
        if(SEC_PKCS12AddCertAndKey(p12ecx, certSafe, NULL, cert,
            CERT_GetDefaultCertDB(), keySafe, NULL, PR_TRUE, pwitem,
            SEC_OID_PKCS12_V2_PBE_WITH_SHA1_AND_3KEY_TRIPLE_DES_CBC)
            != SECSuccess) {
                errMsg = "add cert and key failed";
                goto loser;
        }
    }

    CERT_DestroyCertList(certlist);
    certlist = NULL;

#ifdef NSSDEBUG
    debug("pkcs12 encode\n");
#endif
    if(SEC_PKCS12Encode(p12ecx, p12u_WriteToExportArray, p12cxt)
                        != SECSuccess || errMsg != NULL) {
        errMsg = "PKCS12 encode failed";
        goto loser;
    }

#ifdef NSSDEBUG
    debug("Free pwItem, export context\n");
#endif
    SECITEM_ZfreeItem(pwitem, PR_TRUE);
    SEC_PKCS12DestroyExportContext(p12ecx);

    return p12cxt;

loser:
    SEC_PKCS12DestroyExportContext(p12ecx);
    freeP12uContext(p12cxt);

    if (certlist) {
        CERT_DestroyCertList(certlist);
        certlist = NULL;
    }    

    if (slotPw)
        PR_Free(slotPw->data);

    if (p12FilePw)
        PR_Free(p12FilePw->data);

    if(pwitem) {
        SECITEM_ZfreeItem(pwitem, PR_TRUE);
    }
    return NULL;
}

static void
p12u_EnableAllCiphers()
{
    SEC_PKCS12EnableCipher(PKCS12_RC4_40, 1);
    SEC_PKCS12EnableCipher(PKCS12_RC4_128, 1);
    SEC_PKCS12EnableCipher(PKCS12_RC2_CBC_40, 1);
    SEC_PKCS12EnableCipher(PKCS12_RC2_CBC_128, 1);
    SEC_PKCS12EnableCipher(PKCS12_DES_56, 1);
    SEC_PKCS12EnableCipher(PKCS12_DES_EDE3_168, 1);
    SEC_PKCS12SetPreferredCipher(PKCS12_DES_EDE3_168, 1);
}

void JNU_Throw(JNIEnv *env, const char *msg) {
    jclass cls = (*env)->FindClass(env, "java/lang/Exception");
    if (cls != NULL) {
        (*env)->ThrowNew(env, cls, msg);
    }
    (*env)->DeleteLocalRef(env, cls);
}

// ----- JNI calls --------------
JNIEXPORT void JNICALL Java_com_sun_enterprise_ee_security_NssStore_initNSSNative (JNIEnv *env, jobject jobj, jstring jdir) {

    const char *dir;
    SECStatus rv;
    PRUint32 initFlag = NSS_INIT_COOPERATE;

    dir = (*env)->GetStringUTFChars(env, jdir, NULL);

    if (dir != NULL) {
        PK11_SetPasswordFunc(SECU_GetModulePassword);
#ifdef NSSDEBUG
        debugFp = fopen("nssdebug.log", "w");
        debug("Invoke NSS init\n");
#endif
        rv = NSS_Initialize(dir, "", "", "secmod.db", initFlag);
#ifdef NSSDEBUG
        debug("Finish NSS init\n");
#endif
        if (rv != SECSuccess) {
            errMsg = "Cannot init NSS.";
        }

        /* setup unicode callback functions */
        PORT_SetUCS2_ASCIIConversionFunction(p12u_ucs2_ascii_conversion_function);
        /* use the defaults for UCS4-UTF8 and UCS2-UTF8 */
        p12u_EnableAllCiphers();
    } else {
        errMsg = "Cannot init NSS with NULL directory.";
    }
        
    if (dir != NULL) {
        (*env)->ReleaseStringUTFChars(env, jdir, dir);
    }

    if (errMsg != NULL) {
        JNU_Throw(env, errMsg);
        errMsg = NULL;
    }
}

JNIEXPORT jobject JNICALL Java_com_sun_enterprise_ee_security_NssStore_getTokenNamesNative(
    JNIEnv *env, jobject jobj) {

    PK11SlotList *slotList;
    PK11SlotListElement *listEntry;
    PK11SlotInfo *slot;

    jstring jtokenName;
    jclass jarrayListClass;
    jobject jarrayListObj;
    jmethodID jconstructor;
    jmethodID jaddMethod;

    jarrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
    if (jarrayListClass == NULL) {
        errMsg = "Cannot load java.util.ArrayList";
        goto done;
    }

    jconstructor = (*env)->GetMethodID(env, jarrayListClass, "<init>", "()V");
    if (jconstructor == NULL) {
        errMsg = "Cannot load constructor for ArrayList";
        goto done;
    }

    jaddMethod = (*env)->GetMethodID(env, jarrayListClass,
            "add", "(Ljava/lang/Object;)Z");
    if (jaddMethod == NULL) {
        errMsg = "Cannot load add method for ArrayList";
        goto done;
    }

    jarrayListObj = (*env)->NewObject(env, jarrayListClass, jconstructor);
    if (jarrayListObj == NULL) {
        errMsg = "Cannot construct an ArrayList";
        goto done;
    }

    slotList = PK11_GetAllTokens(CKM_INVALID_MECHANISM, PR_FALSE, PR_TRUE, NULL);
    for (listEntry = PK11_GetFirstSafe(slotList); listEntry; listEntry = listEntry->next) {
        slot = listEntry->slot;
        if (!slot) {
            errMsg = "Invalid Slot.";
            goto done;
        }
        if (PK11_NeedLogin(slot) && (PK11_NeedUserInit(slot) == PR_FALSE) &&
                (PK11_IsInternal(slot) == PR_FALSE)) {

            jtokenName = (*env)->NewStringUTF(env, PK11_GetTokenName(slot));
            (*env)->CallObjectMethod(env, jarrayListObj, jaddMethod, jtokenName);
        }
    }

done:
    if (slotList != NULL) {
        PK11_FreeSlotList(slotList);
    }
    if (errMsg != NULL) {
        JNU_Throw(env, errMsg);
        errMsg = NULL;
    }

    return jarrayListObj;
}

JNIEXPORT jobject JNICALL Java_com_sun_enterprise_ee_security_NssStore_getTokenInfoListNative(
    JNIEnv *env, jobject jobj) {

    SECMODListLock *lock = NULL;
    SECMODModuleList *list;
    SECMODModule *module;
    PK11SlotInfo *slot;

    jstring jtokenName;
    jstring jlibname;

    jclass jarrayListClass;
    jobject jarrayListObj;
    jmethodID jconstructor;
    jmethodID jaddMethod;

    jobject jnssTokenInfoObj;
    jclass jnssTokenInfoClass;
    jmethodID jnssTokenInfoConstructor;


    jarrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
    if (jarrayListClass == NULL) {
        errMsg = "Cannot load java.util.ArrayList";
        goto done;
    }

    jconstructor = (*env)->GetMethodID(env, jarrayListClass, "<init>", "()V");
    if (jconstructor == NULL) {
        errMsg = "Cannot load constructor for ArrayList";
        goto done;
    }

    jaddMethod = (*env)->GetMethodID(env, jarrayListClass,
            "add", "(Ljava/lang/Object;)Z");
    if (jaddMethod == NULL) {
        errMsg = "Cannot load add method for ArrayList";
        goto done;
    }

    jarrayListObj = (*env)->NewObject(env, jarrayListClass, jconstructor);
    if (jarrayListObj == NULL) {
        errMsg = "Cannot construct an ArrayList";
        goto done;
    }

    jnssTokenInfoClass = (*env)->FindClass(env, "com/sun/enterprise/ee/security/NssTokenInfo");
    if (jnssTokenInfoClass == NULL) {
        errMsg = "Cannot load com.sun.enterprise.security.ee.NssTokenInfo";
        goto done;
    }

    jnssTokenInfoConstructor = (*env)->GetMethodID(env, jnssTokenInfoClass,
            "<init>", "(Ljava/lang/String;Ljava/lang/String;I)V");
    if (jnssTokenInfoConstructor == NULL) {
        errMsg = "Cannot load constructor for NssTokenInfoConstructor";
        goto done;
    }

    lock = SECMOD_GetDefaultModuleListLock();
    if (!lock) {
        errMsg = "Can't lock module";
        goto done;
    }       
    SECMOD_GetReadLock(lock);
    list = SECMOD_GetDefaultModuleList();   
    if (!list) {
        errMsg = "Can't get default module";
        goto done;
    }

    for (; list != NULL; list = list->next) {
        module = list->module;
        if (module->internal == PR_FALSE) {
            int slotCount = module->loaded ? module->slotCount : 0;
            if (slotCount > 0) {
                char* libname = module->dllName;
                int i;
                for (i = 0; i < slotCount; i++) {
                    slot = module->slots[i];
                    jtokenName = (*env)->NewStringUTF(env, PK11_GetTokenName(slot));
                    jlibname = (*env)->NewStringUTF(env, libname);

                    jnssTokenInfoObj = (*env)->NewObject(env, jnssTokenInfoClass,
                            jnssTokenInfoConstructor, jtokenName, jlibname, i);
                    if (jnssTokenInfoObj == NULL) {
                        errMsg = "Cannot construct an NssTokenInfo";
                        goto done;
                    }

                    (*env)->CallObjectMethod(env, jarrayListObj, jaddMethod,
                            jnssTokenInfoObj);
                }
            }
        }
    }
    
done:
    if (lock != NULL) {
        SECMOD_ReleaseReadLock(lock);
    }

    if (errMsg != NULL) {
        JNU_Throw(env, errMsg);
        errMsg = NULL;
    }

    return jarrayListObj;
}

JNIEXPORT void JNICALL Java_com_sun_enterprise_ee_security_NssStore_initSlotNative(
    JNIEnv *env, jobject jobj, jstring jtokenName, jstring jpasswd) {

    const char *passwd;
    char *newpw = NULL;
    char *tokenName = NULL;
    PK11SlotInfo *slot = NULL;

    if (jtokenName != NULL) {
        tokenName = (*env)->GetStringUTFChars(env, jtokenName, NULL);
        if (jtokenName == NULL) {
            return;
        }
    }
    
    if (jpasswd == NULL) {
        return;
    }
    passwd = (*env)->GetStringUTFChars(env, jpasswd, NULL);
    if (passwd == NULL) {
        return;
    }

    slot = getSlot(tokenName);
    if (!slot) {
        errMsg = "Invalid slot.";
        goto done;
    }

    /* New databases, initialize keydb password. */
    if (PK11_NeedUserInit(slot)) {
        newpw = PORT_Strdup(passwd);
        PK11_InitPin(slot, (char*)NULL, newpw);
        PORT_Memset(newpw, 0, PORT_Strlen(newpw));
        PORT_Free(newpw);
    }

#ifdef NSSDEBUG
    debug("PK11_Authenticate\n");
#endif
    if (PK11_Authenticate(slot, PR_TRUE, passwd) != SECSuccess) {
        char errBuf[512]; // ok as token_name less than 33 chars, see secmodti.h
        char* printName = tokenName;
        if (printName == NULL) {
            printName = "internal";
        }
        sprintf(errBuf, "NSS password is invalid. Failed to authenticate to PKCS11 slot: %s", printName);
        errMsg = errBuf;
        goto done;
    }

done:
    if (slot) {
#ifdef NSSDEBUG
        debug("free slot\n");
#endif
        PK11_FreeSlot(slot);
    }

    if (tokenName != NULL) {
#ifdef NSSDEBUG
        debug("free tokenName\n");
#endif
        (*env)->ReleaseStringUTFChars(env, jtokenName, tokenName);
    }

    if (passwd != NULL) {
#ifdef NSSDEBUG
        debug("free passwd\n");
#endif
        (*env)->ReleaseStringUTFChars(env, jpasswd, passwd);
    }

    if (errMsg != NULL) {
        JNU_Throw(env, errMsg);
	errMsg = NULL;
    }
}


JNIEXPORT void JNICALL Java_com_sun_enterprise_ee_security_NssStore_getKeysAndCertificatesNative(
    JNIEnv *env, jobject jobj, jobject jkeyMap, jobject jcertMap, jstring jtokenName, jstring jpasswd) {

    const char *passwd;
    char* tokenName = NULL;

    jbyteArray bs;
    jclass jhashMapClass;
    jmethodID jputMethod;
    jstring jnickname;

    CERTCertList *certs;
    CERTCertListNode *node;
    CERTCertificate *cert = NULL;
    secuPWData slotPw = { PW_NONE, NULL };
    secuPWData p12FilePw = { PW_NONE, NULL };
    PK11SlotInfo *slot;
    p12uContext* p12cxt = NULL;

    if (jtokenName != NULL) {
        tokenName = (*env)->GetStringUTFChars(env, jtokenName, NULL);
        if (jtokenName == NULL) {
            return;
        }
    }
    
    if (jpasswd == NULL) {
        return;
    }

    passwd = (*env)->GetStringUTFChars(env, jpasswd, NULL);
    if (passwd == NULL) {
        return;
    }

    p12FilePw.source = PW_PLAINTEXT;
    p12FilePw.data = PORT_Strdup(passwd);
    slotPw.source = PW_PLAINTEXT;
    slotPw.data = PORT_Strdup(passwd);

    jhashMapClass = (*env)->GetObjectClass(env, jkeyMap);
    if (jhashMapClass == NULL) {
        errMsg = "Cannot load java.lang.HashMap";
        goto done;
    }

    jputMethod = (*env)->GetMethodID(env, jhashMapClass,
            "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    if (jputMethod == NULL) {
        errMsg = "Cannot load java.lang.HashMap.put(Object, Object)";
        goto done;
    }

    slot = getSlot(tokenName);
    if (!slot) {
        errMsg = "Invalid slot.";
        goto done;
    }

    // keys and certs for given slot
#ifdef NSSDEBUG
    debug("Getting slot certs\n");
#endif
    certs = PK11_ListCertsInSlot(slot);
#ifdef NSSDEBUG
    debug("Getting slot certs done\n");
#endif

    if (certs) {
        for (node = CERT_LIST_HEAD(certs); !CERT_LIST_END(node, certs);
				node = CERT_LIST_NEXT(node)) {
            cert = node->cert;
            jnickname = (*env)->NewStringUTF(env, cert->nickname);

            if (PK11_FindKeyByAnyCert(cert, NULL) != NULL) {
#ifdef NSSDEBUG
                sprintf(tempBuf, "CALLING ExportPCKCS12Object with %s\n", cert->nickname);
                debug(tempBuf);
#endif
                p12cxt= P12U_ExportPKCS12Object(cert->nickname, slot,
                        &slotPw, &p12FilePw);

                if (errMsg != NULL || p12cxt == NULL) {
                    goto done;
                }
                bs = (*env)->NewByteArray(env, p12cxt->size);
#ifdef NSSDEBUG
                sprintf(tempBuf, "Creating %s key bytes with length %d\n",
                        cert->nickname, p12cxt->size);
                debug(tempBuf);
#endif
                (*env)->SetByteArrayRegion(env, bs, 0, p12cxt->size, p12cxt->buffer);
                (*env)->CallObjectMethod(env, jkeyMap, jputMethod, jnickname, bs);
                freeP12uContext(p12cxt);
                p12cxt = NULL;
            } else {
#ifdef NSSDEBUG
                sprintf(tempBuf, "Creating %s cert bytes with length %d\n",
                        cert->nickname, cert->derCert.len);
                debug(tempBuf);
#endif
                bs = (*env)->NewByteArray(env, cert->derCert.len);
                (*env)->SetByteArrayRegion(env, bs, 0, cert->derCert.len, cert->derCert.data);
                (*env)->CallObjectMethod(env, jcertMap, jputMethod, jnickname, bs);
            }
        }

        CERT_DestroyCertList(certs);
    }

done:
#ifdef NSSDEBUG
    debug("begin of done\n");
#endif
    
    if (slot) {
#ifdef NSSDEBUG
        debug("Clean slot\n");
#endif
        PK11_FreeSlot(slot);
    }

    if (p12cxt) {
        freeP12uContext(p12cxt);
    }

    if (passwd != NULL) {
#ifdef NSSDEBUG
        debug("release passwd\n");
#endif
        (*env)->ReleaseStringUTFChars(env, jpasswd, passwd);
    }


    if (p12FilePw.data != NULL) {
#ifdef NSSDEBUG
        debug("free p12FilePw\n");
#endif
        PORT_Memset(p12FilePw.data, 0, PORT_Strlen(p12FilePw.data));
        PORT_Free(p12FilePw.data);
    }
    if (slotPw.data != NULL) {
#ifdef NSSDEBUG
        debug("free slotPw\n");
#endif
        PORT_Memset(slotPw.data, 0, PORT_Strlen(slotPw.data));
        PORT_Free(slotPw.data);
    }

    if (errMsg != NULL) {
        JNU_Throw(env, errMsg);
	errMsg = NULL;
    }

#ifdef NSSDEBUG
    debug("return from JNI getKeysAndCerts\n");
#endif
    return;
}

JNIEXPORT void JNICALL Java_com_sun_enterprise_ee_security_NssStore_getCACertificatesNative(
    JNIEnv *env, jobject jobj, jobject jcertMap, jstring jpasswd) {

    const char *passwd;
    
    jbyteArray bs;
    jstring jnickname;
    jclass jhashMapClass;
    jmethodID jputMethod;

    CERTCertList *certs;
    CERTCertListNode *node;
    CERTCertificate *cert = NULL;
    secuPWData slotPw = { PW_NONE, NULL };

    passwd = (*env)->GetStringUTFChars(env, jpasswd, NULL);
    if (passwd == NULL) {
        return;
    }

    slotPw.source = PW_PLAINTEXT;
    slotPw.data = PORT_Strdup(passwd);

    jhashMapClass = (*env)->GetObjectClass(env, jcertMap);
    if (jhashMapClass == NULL) {
        errMsg = "Cannot load java.lang.HashMap";
        goto done;
    }

    jputMethod = (*env)->GetMethodID(env, jhashMapClass,
            "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    if (jputMethod == NULL) {
        errMsg = "Cannot load java.lang.HashMap.put(Object, Object)";
        goto done;
    }

#ifdef NSSDEBUG
    debug("Getting CA certs\n");
#endif
    certs = PK11_ListCerts(PK11CertListCA, &slotPw);
#ifdef NSSDEBUG
    debug("Getting CA certs done\n");
#endif

    if (certs) {
        for (node = CERT_LIST_HEAD(certs); !CERT_LIST_END(node, certs);
				node = CERT_LIST_NEXT(node)) {
            cert = node->cert;
            jnickname = (*env)->NewStringUTF(env, cert->nickname);
#ifdef NSSDEBUG
            sprintf(tempBuf, "Creating %s CA cert bytes with length %d\n",
                    cert->nickname, cert->derCert.len);
            debug(tempBuf);
#endif
            bs = (*env)->NewByteArray(env, cert->derCert.len);
            (*env)->SetByteArrayRegion(env, bs, 0, cert->derCert.len, cert->derCert.data);
            (*env)->CallObjectMethod(env, jcertMap, jputMethod, jnickname, bs);
        }

        CERT_DestroyCertList(certs);
    }

done:
    if (slotPw.data != NULL) {
#ifdef NSSDEBUG
        debug("free slotPw\n");
#endif
        PORT_Memset(slotPw.data, 0, PORT_Strlen(slotPw.data));
        PORT_Free(slotPw.data);
    }

    if (passwd != NULL) {
        (*env)->ReleaseStringUTFChars(env, jpasswd, passwd);
    }

    if (errMsg != NULL) {
        JNU_Throw(env, errMsg);
	errMsg = NULL;
    }
}


JNIEXPORT void JNICALL Java_com_sun_enterprise_ee_security_NssStore_changePassword (
    JNIEnv *env, jobject jobj, jstring jpasswd, jstring jnewpasswd)
{
    const char *passwd;
    const char *newpasswd;
    PK11SlotInfo *slot;

    if (jpasswd == NULL) {
        return;
    }
    passwd = (*env)->GetStringUTFChars(env, jpasswd, NULL);
    if (passwd == NULL) {
        return;
    }

    if (jnewpasswd == NULL) {
        return;
    }
    newpasswd = (*env)->GetStringUTFChars(env, jnewpasswd, NULL);
    if (newpasswd == NULL) {
        return;
    }

    slot = PK11_GetInternalKeySlot();
    if (!slot) {
        errMsg = "Invalid slot.";
    } else {
        if (PK11_CheckUserPassword(slot, passwd) != SECSuccess) {
            errMsg = "Invalid database password.";
        } else if (PK11_ChangePW(slot, passwd, newpasswd) != SECSuccess) {
            errMsg = "Failed to change password.";
        }
    }

    //Cleanup
    if (slot) {
        PK11_FreeSlot(slot);
    }
   
    if (passwd != NULL) {
        (*env)->ReleaseStringUTFChars(env, jpasswd, passwd);
    }

    if (newpasswd != NULL) {
        (*env)->ReleaseStringUTFChars(env, jnewpasswd, newpasswd);
    }

    //Throw an exception if we have failed.
    if (errMsg != NULL) {
        JNU_Throw(env, errMsg);
        errMsg = NULL;
    }
}

JNIEXPORT void JNICALL Java_com_sun_enterprise_ee_security_NssStore_close
  (JNIEnv *env, jobject jobj)
{
    NSS_Shutdown();
}
