package org.glassfish.api.admin.cli;

/** Represents the type of an option or an operand.  An OptionType tell the generic command line implementation about
 *  how to treat (interpret) a particular option or operand. Boolean options are special cased, because they are
 *  represented specially on the command line.
 * @author Kedar Mhaswade(km@dev.java.net)
 */
public enum OptionType {
  BOOLEAN,   // implies it can be specified as --name or -n or --name=true or --name=false or --no-name
  FILE,      // implies a file upload
  PASSWORD,  // implies secret information that should be prompted for
  PATH,      // implies operating system path to a file
  STRING,    // generic string
  PROPERTY,  //format is a=b
  PROPERTIES //format is a=b:c=d  
}