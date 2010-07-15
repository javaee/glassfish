package org.glassfish.web.ha.session.management;

/**
 * @author Mahesh Kannan
 *         Date: Jun 17, 2009
 */
public class SimpleMetadataFactory {

    //Full save
    public static SimpleMetadata createSimpleMetadata(long version, long lastAccessTime,
                                                      long maxInactiveInterval, byte[] state) {
        return new SimpleMetadata(version, lastAccessTime, maxInactiveInterval, state);
    }

    //Full save
/*
    public static <E> SimpleMetadata createSimpleMetadata(long version, long lastAccessTime,
                                                      long maxInactiveInterval, byte[] state,
                                                      E extraParam) {
        return new SimpleMetadata(version, lastAccessTime, maxInactiveInterval, state, extraParam);
    }

    //updateContainerExtraParam
    public static <E> SimpleMetadata createSimpleMetadata(long version, long lastAccessTime,
                                                      E extraParam) {
        return new SimpleMetadata(version, lastAccessTime, extraParam);
    }
*/
    //updateTimeStamp
    public static SimpleMetadata createSimpleMetadata(long version, long lastAccessTime) {
        return new SimpleMetadata(version, lastAccessTime);
    }


}
