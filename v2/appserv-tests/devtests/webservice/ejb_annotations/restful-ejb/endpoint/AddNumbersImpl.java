package endpoint;

import java.io.ByteArrayInputStream;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.BindingType;
import javax.ejb.Stateless;

@WebServiceProvider
@Stateless
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class AddNumbersImpl implements Provider<Source> {

    @Resource
    protected WebServiceContext wsContext;

    public Source invoke(Source source) {
        try {
            MessageContext mc = wsContext.getMessageContext();
            String query = (String)mc.get(MessageContext.QUERY_STRING);
            String path = (String)mc.get(MessageContext.PATH_INFO);
            System.out.println("Query String = "+query);
            System.out.println("PathInfo = "+path);
            if (query != null && query.contains("num1=") &&
                query.contains("num2=")) {
                return createSource(query);
            } else if (path != null && path.contains("/num1") &&
                       path.contains("/num2")) {
                return createSource(path);
            } else {
                throw new HTTPException(404);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new HTTPException(500);
        }
    }
    
    private Source createSource(String str) {
        StringTokenizer st = new StringTokenizer(str, "=&/");
        String token = st.nextToken();
        int number1 = Integer.parseInt(st.nextToken());
        st.nextToken();
        int number2 = Integer.parseInt(st.nextToken());
        int sum = number1+number2;
        String body =
            "<ns:addNumbersResponse xmlns:ns=\"http://duke.org\"><ns:return>"
            +sum
            +"</ns:return></ns:addNumbersResponse>";
        Source source = new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
        return source;
    }
    
}
