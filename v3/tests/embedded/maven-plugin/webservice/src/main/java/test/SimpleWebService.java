package test;

import javax.jws.*;

@WebService
public class SimpleWebService {

	@WebMethod(operationName="add")
	public int add(int i, int j) throws Exception {
		int k = i +j ;
		System.out.println(i + "+" + j +" = " + k); 
		return k;
	}

	@WebMethod(operationName="sayHi")
	public String sayHi() throws Exception {
	  System.out.println("hi from SimpleWebService");
	  return "hi from SimpleWebService";
	}

}


