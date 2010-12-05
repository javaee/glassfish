/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jaspic.config.factory;

/**
 *
 * @author ronmonzillo
 */
public class AuthConfigFileFactory extends BaseAuthConfigFactory {

    // MUST "hide" regStore in derived class.
    protected static RegStoreFileParser regStore = null;

    /**
     * to specialize the defaultEntries passed to the RegStoreFileParser
     * constructor, create another subclass of BaseAuthconfigFactory, that is
     * basically a copy of this class, with a change to the third argument
     * of the call to new ResSToreFileParser. 
     * to ensure runtime use of the the associated regStore, make sure that
     * the new subclass also contains an implementation of the getRegStore method.
     * As done within this class, use the locks defined in
     * BaseAuthConfigFactory to serialize access to the regStore (both within
     * the class constructor, and within getRegStore)
     *
     * All EentyInfo OBJECTS PASSED as deualtEntries MUST HAVE BEEN
     * CONSTRCTED USING THE FOLLOWING CONSTRUCTOR:
     *
     * EntryInfo(String className, Map<String, String> properties);
     *
     */
    public AuthConfigFileFactory() {
        rLock.lock();
        try {
            if (regStore != null) {
                return;
            }
        } finally {
            rLock.unlock();
        }
        String userDir = System.getProperty("user.dir");
        wLock.lock();
        try {
            if (regStore == null) {
                regStore = new RegStoreFileParser(userDir,
                        BaseAuthConfigFactory.CONF_FILE_NAME, null);
                _loadFactory();
            }
        } finally {
            wLock.unlock();
        }
    }

    @Override
    protected RegStoreFileParser getRegStore() {
        rLock.lock();
        try {
            return regStore;
        } finally {
            rLock.unlock();
        }
    }
}
