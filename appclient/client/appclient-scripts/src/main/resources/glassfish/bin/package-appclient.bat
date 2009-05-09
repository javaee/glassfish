@echo off
rem/*
rem * To change this template, choose Tools | Templates
rem * and open the template in the editor.
rem *
rem * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
rem *
rem * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
rem *
rem * The contents of this file are subject to the terms of either the GNU
rem * General Public License Version 2 only ("GPL") or the Common Development
rem * and Distribution License("CDDL") (collectively, the "License").  You
rem * may not use this file except in compliance with the License. You can obtain
rem * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
rem * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
rem * language governing permissions and limitations under the License.
rem *
rem * When distributing the software, include this License Header Notice in each
rem * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
rem * Sun designates this particular file as subject to the "Classpath" exception
rem * as provided by Sun in the GPL Version 2 section of the License file that
rem * accompanied this code.  If applicable, add the following below the License
rem * Header, with the fields enclosed by brackets [] replaced by your own
rem * identifying information: "Portions Copyrighted [year]
rem * [name of copyright owner]"
rem *
rem * Contributor(s):
rem *
rem * If you wish your version of this file to be governed by only the CDDL or
rem * only the GPL Version 2, indicate your decision by adding "[Contributor]
rem * elects to include this software in this distribution under the [CDDL or GPL
rem * Version 2] license."  If you don't indicate a single choice of license, a
rem * recipient has the option to distribute your version of this file under
rem * either the CDDL, the GPL Version 2 or to extend the choice of license to
rem * its licensees as provided above.  However, if you add GPL Version 2 code
rem * and therefore, elected the GPL Version 2 license, then the option applies
rem * only if the new code is made subject to such option by the copyright
rem * holder.
rem */
setlocal
set AS_INSTALL=%~dp0..
set AS_INSTALL_MOD=%AS_INSTALL%\modules
java  -classpath "%AS_INSTALL_MOD%\gf-client.jar" org.glassfish.appclient.client.packageappclient.PackageAppClient %*