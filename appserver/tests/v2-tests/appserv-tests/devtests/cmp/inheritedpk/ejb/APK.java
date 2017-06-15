package pkvalidation;

public class APK extends TestOidSuper implements java.io.Serializable {

        // public long id;

        public APK() {}
        
        public boolean equals(java.lang.Object obj) {
            if( obj==null ||
            !this.getClass().equals(obj.getClass()) ) return( false );
            APK o=(APK) obj;
            if( this.id!=o.id ) return( false );
            return( true );
        }
        
        public int hashCode() {
            int hashCode=0;
            hashCode += id;
            return( hashCode );
        }
}
