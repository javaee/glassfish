package test;

import javax.ejb.Remote;

@Remote
public interface Hello {
    String injectedURL();
}
