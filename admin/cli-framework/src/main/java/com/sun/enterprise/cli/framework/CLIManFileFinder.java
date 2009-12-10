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

package com.sun.enterprise.cli.framework;

import java.util.Locale;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.InputStreamReader;

/**
 *	A utility class which gets the I18N xml help file name for the
 *	given command. It searches (using Class.getSystemResource()) for
 *	the pages, and returns the first one found.
 *
 *  For any given man page multiple instances of that page can
 *  exist. Man pages are come in sections (1 through 9, 1m through
 *  9m), locales (language, country, variant), and by command
 *  version. These instances are ordered by section number (1 - 9, 1m
 *  - 9m), local
 *  specificity (most specific to least specific) and then by version
 *  (later versions before earlier versions).
 *
 *  This is probably <em>not</em> what is wanted (I think what is
 *  wanted is versions before sections before language specificity),
 *  but is this way because of the way the Java Class.getResource()
 *  mechanism works.
 *
 *  All methods will throw a NullPointerException if given null object
 *  arguments.
 *
 *  All methods will throw an IllegalArgumentException if their
 *  arguments are non-null but are otherwise meaningless.
 */

public class CLIManFileFinder
{
	  /**
	   * Get the man page for the given command, using the default locale
	   *
	   * @param commandName the name of the command
	   */
  public Reader getCommandManFile(String commandName) {
	return getCommandManFile(commandName, Locale.getDefault());
  }

	  /**
	   * Get the man page for the given command for the given locale
	   * @param commandName the name of the command
	   * @param currentLocale the locale to be used to find the man page
	   */
  public Reader getCommandManFile(String commandName, Locale currentLocale) {
	return getCommandManFile(commandName, currentLocale, this.getClass().getClassLoader());
  }


	  /**
	   * Get the man page for the given command for the given locale
	   * using the give classloader.
	   *
	   * @param commandName the name of the command
	   * @param locale the locale to be used to find the man page
	   * @param classLoader the class loader to be used to find the
	   * man page
	   */
  public Reader getCommandManFile(String commandName, Locale locale, ClassLoader classLoader){
	if (commandName.length() == 0) {
	  throw new IllegalArgumentException ("Command name cannot be empty");
	}


	InputStream s = null;
    
    // special case "help" --> "asadmin"
    if(commandName.equals("help"))
        commandName = "asadmin";
    
	Iterator it = getPossibleLocations(commandName, locale);
	while (s == null && it.hasNext()){
	  s = classLoader.getResourceAsStream((String) it.next());
	}
	
	return (s == null ? (InputStreamReader) null : new InputStreamReader(s));
  }

  Iterator getPossibleLocations(final String commandName, final Locale locale){
	return new Iterator(){
		final String[] locales = getLocaleLocations(locale);
		int i = 0;
		int j = 0;
        private String HELPDIR = getHelpDir(commandName);

        private String getHelpDir(String commandName) {
              //The manpage are assumed to be packaged with the command class.
              //If not, then take the default location.
            try {
                CLIDescriptorsReader cdr = CLIDescriptorsReader.getInstance();
                String className =  cdr.getCommand(commandName).getClassName();
                Class  commandClass = Class.forName(className);
                Package pkg = commandClass.getPackage();
                return pkg.getName().replace('.', '/');
            }
            catch (Exception e) {
                    //if unable to get the command or class then set it
                    //to the default location.
                return "com/sun/enterprise/admin/cli";                
            }
        }
            
            
		public boolean hasNext() {
		  return i < locales.length && j < sections.length;
		}
		public Object next() throws NoSuchElementException{
		  if (!hasNext()){
			throw new NoSuchElementException();
		  }
		  final String result = HELPDIR + locales[i] + "/" + commandName+"." + sections[j++];

		  if (j == sections.length) {
			i++;
			if (i < locales.length ){
			  j = 0;
			}
		  }
          CLILogger.getInstance().printDebugMessage("Trying to get this manpage: " + result);
		  return result;
		}
		public void remove() {
		  throw new UnsupportedOperationException();
		}
	  };
  }

  String[] getLocaleLocations(Locale locale){
	String language = locale.getLanguage();
	String country = locale.getCountry();
	String variant = locale.getVariant();
	List<String> l = new ArrayList<String>();
	l.add("");

	if (language != null && language.length() > 0) {
	  l.add("/" + language);
	  if (country != null && country.length() > 0){
		l.add("/" + language +"/"+country);
		if (variant != null && variant.length() > 0){
		  l.add("/" + language +"/" + country +"/" + variant);
		}
	  }
	}
	Collections.reverse(l);
	return (String[]) l.toArray(new String[0]);
  }
  
  private static final String[] sections = {
      "1", "1m", "2", "2m", "3", "3m", "4", "4m", "5", "5m", "6", "6m", "7", "7m", "8", "8m", "9", "9m", "5asc"};

  
}
