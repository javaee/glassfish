package factoryBy;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Pig {
    public static Pig BABE = new Pig() {
        public String toString() {
            return "Babe";
        }
    };
}
