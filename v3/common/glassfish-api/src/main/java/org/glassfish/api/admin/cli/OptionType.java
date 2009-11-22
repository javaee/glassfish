/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.api.admin.cli;

/** Represents the type of an option or an operand.  An OptionType tells the generic command line implementation about
 *  how to treat (interpret) a particular option or operand. 
 * @author Kedar Mhaswade(km@dev.java.net)
 */
public enum OptionType {
  
  /** Specifies a boolean option, that can specified as <code> -b, --bool, --bool=true, --bool=false, --bool true, --bool false or
   *  --no-bool given that its name is "bool" with symbol 'b'</code>. */
  BOOLEAN, 
  /** Specifies a folder, such that the value of the option should represent a file system folder. */
  DIRECTORY, // implies a folder
  /** Specifies a <i>File</i> whose contents are treated specially. */
  FILE,      // implies a file upload
  /** Specifies <i>path</i> to a file and client makes sure it points to a valid file. */
  FILE_PATH, // implies operating system path to a file
  /** Specifies credential information which is treated specially on the client side. */
  PASSWORD,  // implies secret information that should be prompted for
  /** Specifies a number of properties delimited with a predefined delimiter ':', like <code>--props first=joe:second=blo</code>
      on the command line. */
  PROPERTIES, //format is a=b:c=d  
  /** Specifies a property option, name=value, which looks like <code>--property color=red </code>on the command line. */
  PROPERTY,  //format is a=b
  /** Specifis a generic string. */
  STRING,    // generic string
}
