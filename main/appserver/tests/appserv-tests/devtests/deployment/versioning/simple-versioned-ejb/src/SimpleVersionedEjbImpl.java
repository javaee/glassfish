package versionedejb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;

@Stateful(name="SimpleVersionedEjb", mappedName="ejb/SimpleVersionedEjb")
public class SimpleVersionedEjbImpl implements SimpleVersionedEjb {

    public String getVersion() {
	Properties prop = new Properties();
	InputStream in = null;
        try {
            in = this.getClass().getResource("version-infos.properties").openStream();
            prop.load(in);
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(SimpleVersionedEjbImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
	return prop.getProperty("version.identifier", "");
    }
}
