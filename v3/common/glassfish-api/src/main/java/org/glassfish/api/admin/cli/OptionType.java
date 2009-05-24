package org.glassfish.api.admin.cli;

/** Represents the type of an option or an operand.  An OptionType tells the generic command line implementation about
 *  how to treat (interpret) a particular option or operand. Boolean options are special cased, because they are
 *  represented differently on the command line.
 * @author Kedar Mhaswade(km@dev.java.net)
 */
public enum OptionType {
  
  BOOLEAN,   // implies it can be specified as --name or -n or --name=true or --name=false or --no-name
  DIRECTORY, // implies a folder
  FILE,      // implies a file upload
  PASSWORD,  // implies secret information that should be prompted for
  FILE_PATH, // implies operating system path to a file
  STRING,    // generic string
  PROPERTY,  //format is a=b
  PROPERTIES //format is a=b:c=d  
}