package test3;

import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.ConfiguredBy;

/**
 * @author Kohsuke Kawaguchi
 */
@ConfiguredBy(PointConfig.class)
public class Point { // implements Configurable<PointConfig> {
    private PointConfig config;

    @Inject
    public void setConfig(PointConfig pc) {
        this.config = pc;
    }

    //public int getX() {
    //    return config.x;
    //}
    //
    //public int getY() {
    //    return config.y;
    //}
}
