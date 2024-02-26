/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.test.integration.ws.cdi.interceptor.twice;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class POJOInterceptorImpl {
    public static int count;
    @AroundInvoke
    public Object intercept(final InvocationContext ic) throws Exception {
        POJOInterceptorImpl.count++;
        return ic.proceed();
    }
}
