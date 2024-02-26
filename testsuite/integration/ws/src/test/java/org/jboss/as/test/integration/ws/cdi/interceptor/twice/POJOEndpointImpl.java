/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.test.integration.ws.cdi.interceptor.twice;

import jakarta.interceptor.Interceptors;
import jakarta.jws.WebService;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebService(name = "POJOEndpoint", serviceName = "POJOEndpointService", targetNamespace = "http://org.jboss.test.ws/jbws3441")
public class POJOEndpointImpl implements EndpointIface {

    //@POJOInterceptor
    @Interceptors({ POJOInterceptorImpl.class })
    public String echo(final String message) {
        return   message + " interceptor called for " + POJOInterceptorImpl.count + " times";
    }

    @Override
    public void restCounter() {
        POJOInterceptorImpl.count = 0;
    }
}
