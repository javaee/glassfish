package versionedejb;

import javax.ejb.Remote;

@Remote
public interface SimpleVersionedEjb {
    public String getVersion();
}
