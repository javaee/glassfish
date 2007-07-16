package org.jvnet.hk2.config;

/**
 * Used by {@link Configured} component to indicate its name.
 *
 * <p>
 * A named component can be referenced
 * [{@link FromAttribute#reference() 1},{@link FromElement#reference() 2}],
 * or they can be aggregated into a map by its parent object.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Named {
    String getName();
}
