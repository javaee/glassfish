package org.glassfish.admin.mbeanserver.ssl;

import com.sun.enterprise.security.store.PasswordAdapter;
import org.glassfish.security.common.MasterPassword;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Singleton;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: naman
 * Date: 13 Jan, 2011
 * Time: 11:46:39 AM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="JMX SSL Password Provider Service")
@Scoped(Singleton.class)
public class JMXMasterPasswordImpl implements MasterPassword, PreDestroy {

    private char[] _masterPassword;

    public void setMasterPassword(char[] masterPassword) {
        _masterPassword = Arrays.copyOf(masterPassword, masterPassword.length);
    }

    public PasswordAdapter getMasterPasswordAdapter() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        PasswordAdapter passwordAdapter = new PasswordAdapter(_masterPassword);
        return passwordAdapter;
    }

    public char[] getMasterPassword() {
        if (_masterPassword == null) {
            return null;
        }
        return Arrays.copyOf(_masterPassword, _masterPassword.length);
    }

    public void preDestroy() {
        Arrays.fill(_masterPassword, ' ');
    }
}
