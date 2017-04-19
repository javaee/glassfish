package com.acme;

public class SomeEvent {

    private int someInt;

    public SomeEvent(int i) { 
	someInt = i;
    }

    public String toString() {
	return "" + someInt;
    }
}