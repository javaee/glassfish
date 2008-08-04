/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.services.impl;

import java.nio.ByteBuffer;

/**
 * Object, which saves the parser state during processing a HTTP requests
 * @author Alexey Stashok
 */
public class HttpParserState {

    public static final int PARAMETER_NOT_SET = Integer.MIN_VALUE;
    public static final int DEFAULT_STATE_PARAMETERS_NUM = 5;
    private ByteBuffer buffer;

    private boolean isCompleted;
    private int state;
    private int position;
    private int stateParameters[];

    public HttpParserState() {
        this(DEFAULT_STATE_PARAMETERS_NUM);
    }

    public HttpParserState(int stateParametersNum) {
        stateParameters = new int[stateParametersNum];
        reset();
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getStateParameter(int i) {
        return stateParameters[i];
    }

    public int getStateParameter(int i, int defaultValue) {
        int value = stateParameters[i];

        return value != PARAMETER_NOT_SET ? value : defaultValue;
    }

    public void setStateParameter(int i, int value) {
        stateParameters[i] = value;
    }

    public void reset() {
        buffer = null;
        position = 0;
        state = 0;
        isCompleted = false;

        for (int i = 0; i < stateParameters.length; i++) {
            stateParameters[i] = PARAMETER_NOT_SET;
        }
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
