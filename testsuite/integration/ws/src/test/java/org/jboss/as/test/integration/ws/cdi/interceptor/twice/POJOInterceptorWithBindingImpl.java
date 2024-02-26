/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.test.integration.ws.cdi.interceptor.twice;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@POJOInterceptor
@Interceptor
public class POJOInterceptorWithBindingImpl {
    public static int count;
    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {
        POJOInterceptorWithBindingImpl.count++;
        return ic.proceed();
    }
}
