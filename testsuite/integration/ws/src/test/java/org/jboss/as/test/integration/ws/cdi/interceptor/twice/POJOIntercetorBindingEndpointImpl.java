package org.jboss.as.test.integration.ws.cdi.interceptor.twice;

import jakarta.jws.WebService;
@WebService(name = "InterceptorBindingEndpoint", serviceName = "InterceptorBindingEndpointService", targetNamespace = "http://org.jboss.test.ws/jbws3441")
public class POJOIntercetorBindingEndpointImpl implements EndpointIface {

    @POJOInterceptor
    public String echo(final String message) {
        return   message + " interceptor called for " + POJOInterceptorWithBindingImpl.count + " times";
    }

    @Override
    public void restCounter() {
        POJOInterceptorWithBindingImpl.count = 0;
    }
}
