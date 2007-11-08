package endpoint;

import javax.jws.WebService;
import javax.jws.WebMethod;

@WebService(
    name="EndpointMappingConsumer",
    serviceName="EndpointMappingConsumerService",
    targetNamespace="http://example.web.service/EndpointMappingConsumer"
)
public class EndpointMappingConsumer {
	public EndpointMappingConsumer() {}

	@WebMethod(operationName="add", action="urn:Add")
	public int add(int i, int j) {
                int k = i +j ;
                System.out.println(i + "+" + j +" = " + k);
 
		return k;
	}
}
