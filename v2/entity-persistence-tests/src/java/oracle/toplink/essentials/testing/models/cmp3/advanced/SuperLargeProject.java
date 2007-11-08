package oracle.toplink.essentials.testing.models.cmp3.advanced;

/**
 * This class represents a non-entity subclass of an entity superclass.
 */
public class SuperLargeProject extends Project {
    public SuperLargeProject () {
        super();
    }
    public SuperLargeProject (String name) {
        this();
        this.setName(name);
    }

}
