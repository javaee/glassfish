package org.glassfish.ha.store.adapter.file;

import org.glassfish.ha.store.spi.BackingStore;
import org.glassfish.ha.store.spi.BackingStoreException;
import org.glassfish.ha.store.spi.BackingStoreFactory;
import org.glassfish.ha.store.spi.BatchBackingStore;
import org.jvnet.hk2.annotations.Service;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mahesh Kannan
 */
@Service(name="file")
public class FileBackingStoreFactory
    implements BackingStoreFactory {

    private String instanceName;

    private String groupName;

    private static ConcurrentHashMap<String, SimpleFileBackingStore> _stores
            = new ConcurrentHashMap<String, SimpleFileBackingStore>();

    @Override
    public <K, V> BackingStore<K, V> createBackingStore(String storeName, Class<K> keyClazz, Class<V> vClazz, Properties env) throws BackingStoreException {
        SimpleFileBackingStore<K, V> fs = new SimpleFileBackingStore<K, V>();
        fs.initialize(storeName, keyClazz, vClazz, env);

        _stores.put(storeName, fs);
        return fs;
    }

    @Override
    public BatchBackingStore createBatchBackingStore(Properties env) throws BackingStoreException {
        return new FileTxStoreManager();
    }

    static SimpleFileBackingStore getFileBackingStore(String storeName) {
        return _stores.get(storeName);
    }

    static void removemapping(String storeName) {
        _stores.remove(storeName);
    }

}
