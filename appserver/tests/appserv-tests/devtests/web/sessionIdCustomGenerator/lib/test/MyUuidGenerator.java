package test;

import com.sun.enterprise.util.uuid.UuidGenerator;

public class MyUuidGenerator implements UuidGenerator {

    public String generateUuid() {
        return "abc";
    }

    public String generateUuid(Object obj) {
        return "abc";
    }

}
