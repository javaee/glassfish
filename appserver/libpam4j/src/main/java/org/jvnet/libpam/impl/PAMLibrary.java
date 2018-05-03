/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.jvnet.libpam.impl;

import com.sun.jna.Library;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import com.sun.jna.Native;
import com.sun.jna.Callback;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;
import static org.jvnet.libpam.impl.CLibrary.libc;

/**
 * libpam.so binding.
 *
 * See http://www.opengroup.org/onlinepubs/008329799/apdxa.htm
 * for the online reference of pam_appl.h
 *
 * @author Kohsuke Kawaguchi
 */
public interface PAMLibrary extends Library {
    class pam_handle_t extends PointerType {
        public pam_handle_t() {}
        public pam_handle_t(Pointer pointer) { super(pointer); }
    }

    class pam_message extends Structure {
        public int msg_style;
        public String msg;

        /**
         * Attach to the memory region pointed by the given pointer.
         */
        public pam_message(Pointer src) {
            useMemory(src);
            read();
        }

        protected List getFieldOrder() {
            return Arrays.asList("msg_style", "msg");
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void setMsgStyle(int msg_style) {
            this.msg_style = msg_style;
        }
    }

    class pam_response extends Structure {
        /**
         * This is really a string, but this field needs to be malloc-ed by the conversation
         * method, and to be freed by the caler, so I bind it to {@link Pointer} here.
         *
         * The man page doesn't say that, but see
         * http://www.netbsd.org/docs/guide/en/chap-pam.html#pam-sample-conv
         * This behavior is confirmed with a test, too; if I don't do strdup,
         * libpam crashes.
         */
        public Pointer resp;
        public int resp_retcode; 

        /**
         * Attach to the memory region pointed by the given memory.
         */
        public pam_response(Pointer src) {
            useMemory(src);
            read();
        }

        public pam_response() {}

        /**
         * Sets the response code.
         */
        public void setResp(String msg) {
           this.resp = libc.strdup(msg);
        }

        protected Pointer getResp() {
           return resp;
        }
        
        public void setRespCode(int resp_retcode) {
            this.resp_retcode = resp_retcode;
        }

        public int getRespCode() {
            return resp_retcode;
        }

        protected List getFieldOrder() {
            return Arrays.asList("resp", "resp_retcode");
        }

        public static final int SIZE = new pam_response().size();
    }

    class pam_conv extends Structure {
        public interface PamCallback extends Callback {
            /**
             * According to http://www.netbsd.org/docs/guide/en/chap-pam.html#pam-sample-conv,
             * resp and its member string both needs to be allocated by malloc,
             * to be freed by the caller.
             */
            int callback(int num_msg, Pointer msg, Pointer resp, Pointer _);
        }
        public PamCallback conv;
        public Pointer _;

        public pam_conv(PamCallback conv) {
            this.conv = conv;
        }

        protected List getFieldOrder() {
            return Arrays.asList("conv", "_");
        }
        protected PamCallback getConv(){
            return conv;
        }
    }

    int pam_start(String service, String user, pam_conv conv, PointerByReference/* pam_handle_t** */ pamh_p);
    int pam_end(pam_handle_t handle, int pam_status);
    int pam_set_item(pam_handle_t handle, int item_type, String item);
    int pam_get_item(pam_handle_t handle, int item_type, PointerByReference item);
    int pam_authenticate(pam_handle_t handle, int flags);
    int pam_setcred(pam_handle_t handle, int flags);
    int pam_acct_mgmt(pam_handle_t handle, int flags);
    String pam_strerror(pam_handle_t handle, int pam_error);

    final int PAM_USER = 2;

    // error code
    final int PAM_SUCCESS = 0;
    final int PAM_CONV_ERR = 6;


    final int PAM_PROMPT_ECHO_OFF  = 1; /* Echo off when getting response */
    final int PAM_PROMPT_ECHO_ON   = 2; /* Echo on when getting response */
    final int PAM_ERROR_MSG        = 3; /* Error message */
    final int PAM_TEXT_INFO        = 4; /* Textual information */

    public static final PAMLibrary libpam = (PAMLibrary)Native.loadLibrary("pam",PAMLibrary.class);
}
