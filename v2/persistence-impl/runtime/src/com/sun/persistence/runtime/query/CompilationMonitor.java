/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.runtime.query;


/**
 * Allows invokers of EJBQLC.compile to see the AST after each phase in the
 * compilation of a query.
 * 
 * @author db13166
 */
public interface CompilationMonitor {
    /** Invoked before syntax analysis of <code>qstr</code>.
     * @param qstr query string being compiled.
     */
    public void preSyntax(String qstr);
    
    /** Invoked after syntax analysis of <code>qstr</code>, which resulted in
     * <code>ast</code>.
     * @param qstr query string being compiled.
     * @param EJBQL ast after syntax analysis.
     */
    public void postSyntax(String qstr, EJBQLAST ast);
    
    /** Invoked before semantic analysis of <code>qstr</code>.
     * @param qstr query string being compiled.
     */
    public void preSemantic(String qstr);
    
    /** Invoked after semantic analysis of <code>qstr</code>, which resulted in
     * <code>ast</code>.
     * @param qstr query string being compiled.
     * @param EJBQL ast after semantic analysis.
     */
    public void postSemantic(String qstr, EJBQLAST ast);
    
    /** Invoked after optimizationof <code>qstr</code>.
     * @param qstr query string being compiled.
     */
    public void preOptimize(String qstr);
    
    /** Invoked after optimizationof <code>qstr</code>, which resulted in
     * <code>ast</code>.
     * @param qstr query string being compiled.
     * @param EJBQL ast after optimization.
     */
    public void postOptimize(String qstr, EJBQLAST ast);
}
