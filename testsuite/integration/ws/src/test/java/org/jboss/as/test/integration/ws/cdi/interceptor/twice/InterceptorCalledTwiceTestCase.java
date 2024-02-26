/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.test.integration.ws.cdi.interceptor.twice;

import jakarta.xml.ws.Service;
import java.net.URL;
import javax.xml.namespace.QName;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class InterceptorCalledTwiceTestCase {
    @ArquillianResource
    URL baseUrl;

    public static final String ARCHIVE_NAME = "jaxws-cdi-interceptors-twice";

    @Deployment
    public static Archive<?> archive() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, ARCHIVE_NAME + ".war");
        war.addPackage(InterceptorCalledTwiceTestCase.class.getPackage());
        war.addAsWebInfResource(new StringAsset(BEANS_CONFIG), "beans.xml");
        return war;

    }

    private EndpointIface getPojo() throws Exception {
        final URL wsdlURL = new URL(baseUrl + "POJOEndpointService?wsdl");
        final QName serviceName = new QName("http://org.jboss.test.ws/jbws3441", "POJOEndpointService");
        final Service service = Service.create(wsdlURL, serviceName);
        return service.getPort(EndpointIface.class);
    }

    @Test
    public void testPojoCall() throws Exception {
        String message = "Pojo";
        getPojo().restCounter();
        String response = getPojo().echo(message);
        Assert.assertEquals("Pojo interceptor called for 1 times", response);
    }


    private EndpointIface getInterceptorBindingPojo() throws Exception {
        final URL wsdlURL = new URL(baseUrl + "InterceptorBindingEndpointService?wsdl");
        final QName serviceName = new QName("http://org.jboss.test.ws/jbws3441", "InterceptorBindingEndpointService");
        final Service service = Service.create(wsdlURL, serviceName);
        return service.getPort(EndpointIface.class);
    }

    @Test
    public void testInterceptorBindingPojoCall() throws Exception {
        String message = "InterceptorBindingPojo";
        getInterceptorBindingPojo().restCounter();
        String response = getInterceptorBindingPojo().echo(message);
        Assert.assertEquals("InterceptorBindingPojo interceptor called for 1 times", response);
    }

    private EndpointIface getEjb3() throws Exception {
        final URL wsdlURL = new URL(baseUrl + "EJB3EndpointService/EJB3Endpoint?wsdl");
        final QName serviceName = new QName("http://org.jboss.test.ws/jbws3441", "EJB3EndpointService");
        final Service service = Service.create(wsdlURL, serviceName);
        return service.getPort(EndpointIface.class);
    }


    @Test
    public void testEjb3Call() throws Exception {
        String message = "EJB";
        getEjb3().restCounter();
        String response = getEjb3().echo(message);
        Assert.assertEquals("EJB interceptor called for 1 times", response);
    }

    //CDI interceptor with NameBinding has to be enabled with this configuration
    private static final String BEANS_CONFIG = "<beans><interceptors>"
            + "<class>org.jboss.as.test.integration.ws.cdi.interceptor.twice.POJOInterceptorWithBindingImpl</class>"
            + "</interceptors></beans>";
}
