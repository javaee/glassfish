/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.common.util.admin;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

/**
 * Coordinates generation and consumption of one-time authentication tokens.
 * <p>
 * Some DAS commands submit admin commands to be run elsewhere - either in
 * another process on the same host or, via ssh, to another host.  Given that it
 * is already executing, the DAS command in progress has already been authenticated (if
 * required).  Therefore we want the soon-to-be submitted commands to also
 * be authenticated, but we do not want to send the username and/or password
 * information that was used to authenticate the currently-running DAS command
 * to the other process for it to use.
 * <p>
 * Instead, the currently-running DAS command can use this service to obtain
 * a one-time authentication token.  The DAS command then includes the token,
 * rather than username/password credentials, in the submitted command.
 * <p>
 * This service records which tokens have been given out but not yet used.
 * When an admin request arrives with a token, the AdminAdapter consults this
 * service to see if the token is valid and, if so, the AdminAdapter
 * allows the request to run.
 * <p>
 * We allow each token to be used twice, once for retrieving the command
 * metadata and then the second time to execute the command.
 *
 *
 *                              NOTE
 *
 * Commands that trigger other commands on multiple hosts - such as
 * start-cluster - will need to reuse the authentication token more than twice.
 * So, temporarily, we allow each token unlimited use.
 *
 * @author Tim Quinn
 */
@Service
@Scoped(Singleton.class)
public class AuthTokenManager {

    public static final String AUTH_TOKEN_OPTION = "--authtoken";

    private final static int TOKEN_SIZE = 10;

    private final SecureRandom rng = new SecureRandom();

    private final Map<String,AtomicInteger> liveTokens = new HashMap<String,AtomicInteger>();

    /* hex conversion stolen shamelessly from Bill's LocalPasswordImpl - maybe refactor to share later */
    private static final char[] hex = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * Creates a new limited use authentication token.
     * @return auth token
     */
    public String createToken() {
        final byte[] newToken = new byte[TOKEN_SIZE];
        rng.nextBytes(newToken);
        final String token = toHex(newToken);
        liveTokens.put(token, new AtomicInteger(2));
        return token;
    }

    /**
     * Records the use of an authentication token by an admin request.
     *
     * @param token the token consumed
     * @return true if the token was valid (and had remaining uses on it); false otherwise
     */
    public boolean consumeToken(final String token) {
        final boolean isReusedToken = (token.endsWith("+"));
        final String storedToken = (isReusedToken ? token.substring(0, token.length()-1) : token);
        //TODO - Need to limit the use of each token in some way that allows cmds like start-cluster to work with many reuses of a token
//        final AtomicInteger ai = liveTokens.get(storedToken);
//        if (ai == null) {
//            return false;
//        }
//        if ( ! isReusedToken) {
//            if (ai.decrementAndGet() == 0) {
//                liveTokens.remove(token);
//            }
//        }
//        return true;
        return liveTokens.containsKey(storedToken);
    }

    /**
     * Convert the byte array to a hex string.
     */
    private static String toHex(byte[] b) {
        char[] bc = new char[b.length * 2];
        for (int i = 0, j = 0; i < b.length; i++) {
            byte bb = b[i];
            bc[j++] = hex[(bb >> 4) & 0xF];
            bc[j++] = hex[bb & 0xF];
        }
        return new String(bc);
    }
}
