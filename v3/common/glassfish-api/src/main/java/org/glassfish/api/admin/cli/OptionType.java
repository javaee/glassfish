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
