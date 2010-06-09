package endpoint;

public class SimpleInterceptor {

    @javax.interceptor.AroundInvoke
    public Object intercept(javax.interceptor.InvocationContext ic) {
        Object o = ic.getMethod().getName();
        System.out.println("Inside interceptor");
        return o;
    }
}
