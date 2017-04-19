package com.acme;

import javax.ejb.Remote;

@Remote
public interface Hello {
    String hello();
}
