package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;

@WebService(
    name="Adder",
    serviceName="AdderService",
    targetNamespace="http://example.web.service/Adder"
)
public class Adder {
	public Adder() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) {
                int k = i -j ;
                System.err.println(i + "-" + j +" = " + k);
				return k;

				/*
		if(i == 101)
			throw new RuntimeException("This is my exception in add ...");
			*/
	}
}
