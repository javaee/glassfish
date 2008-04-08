/*
 * HttpAlgorithmParameterSpec.java
 * 
 * Created on 10 Aug, 2007, 4:40:22 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.security.auth.digest.impl;

import java.security.spec.AlgorithmParameterSpec;
import javax.servlet.http.HttpServletRequest;

/**
 * represents HttpServlet request parameter
 * @author K.Venugopal@sun.com
 */
public class HttpAlgorithmParameterImpl implements AlgorithmParameterSpec {

    private HttpServletRequest req = null;
    public HttpAlgorithmParameterImpl(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletRequest getValue(){
        return req;
    } 
}
