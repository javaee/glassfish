package factoryBy;

/**
 * @author Kohsuke Kawaguchi
 */
public enum Pig {
    BABE {
        public String toString() {
            return "Babe";
        }
    }
}
