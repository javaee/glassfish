import com.sun.appserv.server.util.Version;

public class VersionPrinter {

  public static void main(final String[] arg) {
    try {
       System.out.println("Full Version: " + Version.getFullVersion());   
       System.out.println("Abbreviated Version: " + Version.getAbbreviatedVersion());   
       System.out.println("Build Version: " + Version.getBuildVersion());   
       System.out.println("Major Version: " + Version.getMajorVersion());   
       System.out.println("Minor Version: " + Version.getMinorVersion());   
       System.out.println("Product Name: " + Version.getProductName());   
     } catch (final NoClassDefFoundError e) {
          System.out.println("Please run this class as: java -cp .:{install-dir}/lib/appserv-ext.jar:${install-dir}/lib/appserv-se.jar:${install-dir}/lib/appserv-rt.jar\nwhere ${install-dir} is the installation location");
    }
  }
}
