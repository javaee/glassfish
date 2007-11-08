<!--
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.

  Portions Copyright Apache Software Foundation.

  The contents of this file are subject to the terms of either the GNU
  General Public License Version 2 only ("GPL") or the Common Development
  and Distribution License("CDDL") (collectively, the "License").  You
  may not use this file except in compliance with the License. You can obtain
  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
  language governing permissions and limitations under the License.

  When distributing the software, include this License Header Notice in each
  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
  Sun designates this particular file as subject to the "Classpath" exception
  as provided by Sun in the GPL Version 2 section of the License file that
  accompanied this code.  If applicable, add the following below the License
  Header, with the fields enclosed by brackets [] replaced by your own
  identifying information: "Portions Copyrighted [year]
  [name of copyright owner]"

  Contributor(s):

  If you wish your version of this file to be governed by only the CDDL or
  only the GPL Version 2, indicate your decision by adding "[Contributor]
  elects to include this software in this distribution under the [CDDL or GPL
  Version 2] license."  If you don't indicate a single choice of license, a
  recipient has the option to distribute your version of this file under
  either the CDDL, the GPL Version 2 or to extend the choice of license to
  its licensees as provided above.  However, if you add GPL Version 2 code
  and therefore, elected the GPL Version 2 license, then the option applies
  only if the new code is made subject to such option by the copyright
  holder.
-->
<html>
  <body>
    <h1>Demonstration of pluggable ELResolvers</h1>
    <hr/>
    In JSP 2.1, you can plug in your own ELResolvers, which allows you
    to customize the behavior of resolving variables and properties in
    EL expressions.  This demo illustrates how a new resolver can be created
    that resolves \${Color} and properties on that object.
    <hr/>
    <table border="1">
      <tr>
        <td>EL Expression</td>
        <td>Result</td>
        <td>Color Sample</td>
      </tr>
      <tr>
        <td>\${Color}</td>
        <td>${Color}</td>
        <td>&nbsp;</td>
      </tr>
      <tr>
        <td>\${Color[100][150][200]}</td>
        <td>${Color[100][150][200]}</td>
        <td bgcolor="${Color[100][150][200].hex}">&nbsp;</td>
      </tr>
      <tr>
        <td>\${Color.LightSeaGreen}</td>
        <td>${Color.LightSeaGreen}</td>
        <td bgcolor="${Color.LightSeaGreen.hex}">&nbsp;</td>
      </tr>
      <tr>
        <td>\${Color[100][100][100].darker}</td>
        <td>${Color[100][100][100].darker}</td>
        <td bgcolor="${Color[100][100][100].hex}">&nbsp;</td>
      </tr>
      <tr>
        <td>\${Color[100][100][100].brighter}</td>
        <td>${Color[100][100][100].brighter}</td>
        <td bgcolor="${Color[100][100][100].brighter.hex}">&nbsp;</td>
      </tr>
      <tr>
        <td>\${Color.MidnightBlue.hex}</td>
        <td>${Color.MidnightBlue.hex}</td>
        <td bgcolor="${Color.MidnightBlue.hex}">&nbsp;</td>
      </tr>
      <tr>
        <td>\${Color['#a0b0c0']}</td>
        <td>${Color['#a0b0c0']}</td>
        <td bgcolor="${Color['#a0b0c0'].hex}">&nbsp;</td>
      </tr>
    </table>
  </body>
</html>