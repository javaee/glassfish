/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.localization;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>
 * <b>Purpose</b>: Any TopLink message in Foundation Library & J2EE Integration JARs
 * should be a subclass of this class.
 *
 * Creation date: (7/12/00)
 * @author Shannon Chen
 * @since TOPLink/Java 5.0
 */
public abstract class ToplinkLocalization {

    /**
     * Return the message for the given exception class and error number.
     */
    public static String buildMessage(String localizationClassName, String key, Object[] arguments) {
        String message = key;
        ResourceBundle bundle = null;

        // JDK 1.1 MessageFormat can't handle null arguments
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] == null) {
                    arguments[i] = "null";
                }
            }
        }

        bundle = ResourceBundle.getBundle("oracle.toplink.essentials.internal.localization.i18n." + localizationClassName + "Resource", Locale.getDefault());

        try {
            message = bundle.getString(key);
        } catch (java.util.MissingResourceException mre) {
            // Found bundle, but couldn't find translation.
            // Get the current language's NoTranslationForThisLocale message.
            bundle = ResourceBundle.getBundle("oracle.toplink.essentials.internal.localization.i18n.ToplinkLocalizationResource", Locale.getDefault());
            String noTranslationMessage = bundle.getString("NoTranslationForThisLocale");

            return MessageFormat.format(message, arguments) + noTranslationMessage;
        }
        return MessageFormat.format(message, arguments);
    }
}
