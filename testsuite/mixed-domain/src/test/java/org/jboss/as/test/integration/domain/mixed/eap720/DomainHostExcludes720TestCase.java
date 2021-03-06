/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.integration.domain.mixed.eap720;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.test.integration.domain.mixed.DomainHostExcludesTest;
import org.jboss.as.test.integration.domain.mixed.Version;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import org.junit.BeforeClass;

/**
 * Tests of the ability of a DC to exclude resources from visibility to an EAP 7.2.0 slave.
 *
 * @author Brian Stansberry
 */
@Version(Version.AsVersion.EAP_7_2_0)
public class DomainHostExcludes720TestCase extends DomainHostExcludesTest {

    @BeforeClass
    public static void beforeClass() throws InterruptedException, TimeoutException, MgmtOperationException, IOException {
        LegacyConfig720TestSuite.initializeDomain();
        // FIXME Test is failing if WildFly14.0 is passed to the hostRelease.
        // Using the 8.0 management version (which corresponds to WildFly 14) works...
        setup(DomainHostExcludes720TestCase.class, "WildFly14.0", ModelVersion.create(8, 0));
    }
}
