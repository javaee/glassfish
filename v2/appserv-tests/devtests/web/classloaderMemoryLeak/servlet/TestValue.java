package test;

public class TestValue {
     private transient ClassLoader value;
     public TestValue() {
         value = this.getClass().getClassLoader();
     }
}
