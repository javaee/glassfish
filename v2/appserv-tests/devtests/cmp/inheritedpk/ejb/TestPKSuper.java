package pkvalidation;

/*
 * TestPKSuper.java
 *
 * Created on May 16, 2003, 12:32 PM
 */

/**
 *
 * @author  Marina Vatkina
 */
public class TestPKSuper implements java.io.Serializable {
    public long id;

    /** Creates a new instance of TestPKSuper */
    public TestPKSuper() {
    }
    
    public boolean equals(java.lang.Object obj) {
        if( obj==null ||
            !this.getClass().equals(obj.getClass()) ) return( false );

        TestPKSuper o=(TestPKSuper) obj;
        return ( this.id == o.id );
    }
        
    public int hashCode() {
        int hashCode=0;
        hashCode += id;
        return( hashCode );
    }

}
