package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.Oneway;

@WebService(
    name="EndpointMapping",
    serviceName="EndpointMappingService",
    targetNamespace="http://example.web.service/EndpointMapping"
)
public class EndpointMapping {
	public EndpointMapping() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) throws Exception {
                int k = i +j ;
                System.out.println(i + "+" + j +" = " + k);
		//throw new java.lang.Exception("This is my exception"); 
		return k;
	}
}
