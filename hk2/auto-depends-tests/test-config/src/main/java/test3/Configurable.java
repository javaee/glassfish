package test3;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Configurable<T> {
    public void setConfig(T config);
}
