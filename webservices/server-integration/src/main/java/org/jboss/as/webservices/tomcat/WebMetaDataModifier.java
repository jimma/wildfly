/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.webservices.tomcat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.as.webservices.logging.WSLogger;
import org.jboss.as.webservices.util.ASHelper;
import org.jboss.as.webservices.util.WebMetaDataHelper;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.AuthConstraintMetaData;
import org.jboss.metadata.web.spec.FilterMappingMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.metadata.web.spec.LoginConfigMetaData;
import org.jboss.metadata.web.spec.SecurityConstraintMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionMetaData;
import org.jboss.metadata.web.spec.WebResourceCollectionsMetaData;
import org.jboss.security.SecurityConstants;
import org.jboss.ws.common.integration.WSConstants;
import org.jboss.ws.common.integration.WSHelper;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.deployment.WSFServlet;
import org.jboss.wsf.spi.deployment.ManagementFilter;

/**
 * The modifier of jboss web meta data. It configures WS transport for every webservice endpoint plus propagates WS stack
 * specific context parameters if required.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:tdiesler@redhat.com">Thomas Diesler</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
final class WebMetaDataModifier {
    private final String MANAGEMENT_URL_PATTERN = "management/*";
    private final String MANAGEMENT_ROLE = "admin";
    private final String MANAGEMENT_RESOURCE_NAME = "EndpointManagement";
    private final String MANAGEMENT_REALM = "ManagementRealm";
    private final String MANAGEMENT_AUTH_METHOD = "BASIC";
    WebMetaDataModifier() {
        super();
    }

    /**
     * Modifies web meta data to configure webservice stack transport and properties.
     *
     * @param dep webservice deployment
     */
    void modify(final Deployment dep) {
        final JBossWebMetaData jbossWebMD = WSHelper.getOptionalAttachment(dep, JBossWebMetaData.class);

        if (jbossWebMD != null) {
            this.configureFilter(dep, jbossWebMD);
            this.configureEndpoints(dep, jbossWebMD);
            this.modifyContextRoot(dep, jbossWebMD);
        }
    }

    private void configureFilter(final Deployment dep, final JBossWebMetaData jbossWebMD) {
        //Add filter and filter mapping for management urls
        FiltersMetaData filtersData = jbossWebMD.getFilters();
        if (filtersData == null) {
            filtersData = new FiltersMetaData();
        }
        List<FilterMappingMetaData> filterMappingList = jbossWebMD.getFilterMappings();
        if (filterMappingList == null) {
            filterMappingList = new ArrayList<FilterMappingMetaData>();
        }
        List<String> filterUrlPatterns = new ArrayList<String>();
        for (Endpoint endpoint : dep.getService().getEndpoints()) {
            if (endpoint instanceof HttpEndpoint) {
                HttpEndpoint httpEndpoint = (HttpEndpoint) endpoint;
                String pattern = httpEndpoint.getURLPattern();
                FilterMetaData filterMD = new FilterMetaData();
                filterMD.setFilterName(endpoint.getShortName());
                filterMD.setFilterClass(ManagementFilter.class.getName());
                ParamValueMetaData paramValueMD = new ParamValueMetaData();
                paramValueMD.setParamName("httpURLPattern");
                paramValueMD.setParamValue(pattern.toString());
                List<ParamValueMetaData> paramList = new ArrayList<ParamValueMetaData>();
                paramList.add(paramValueMD);
                filterMD.setInitParam(paramList);
                filtersData.add(filterMD);

                FilterMappingMetaData filterMappingData = new FilterMappingMetaData();
                filterMappingData.setFilterName(endpoint.getShortName());
                String managementURL = pattern + "/ " + MANAGEMENT_URL_PATTERN;
                if (pattern.endsWith("/*")) {
                    managementURL = pattern.substring(0, pattern.length()-2) + "/" + MANAGEMENT_URL_PATTERN;
                }
                if (pattern.endsWith("/")) {
                    managementURL = pattern + MANAGEMENT_URL_PATTERN;
                }
                filterUrlPatterns.add(managementURL);
                filterMappingData.setUrlPatterns(filterUrlPatterns);
                filterMappingList.add(filterMappingData);
            }
        }
        jbossWebMD.setFilters(filtersData);
        jbossWebMD.setFilterMappings(filterMappingList);
        //Add SecurityConstraintMetaData for management url if not exists.
        SecurityConstraintMetaData securityConstraintMD = new SecurityConstraintMetaData();
        AuthConstraintMetaData authConstaintMD = new AuthConstraintMetaData();
        List<String> roles = new ArrayList<String>();
        roles.add(MANAGEMENT_ROLE);
        authConstaintMD.setRoleNames(roles);
        securityConstraintMD.setAuthConstraint(authConstaintMD);
        WebResourceCollectionsMetaData webResources = new WebResourceCollectionsMetaData();
        WebResourceCollectionMetaData webResource = new WebResourceCollectionMetaData();
        webResource.setWebResourceName(MANAGEMENT_RESOURCE_NAME);
        webResource.setUrlPatterns(filterUrlPatterns);
        webResources.add(webResource);
        securityConstraintMD.setResourceCollections(webResources);
        if (jbossWebMD.getSecurityConstraints() == null) {
            List<SecurityConstraintMetaData> securityConstraintsList = new ArrayList<SecurityConstraintMetaData>();
            securityConstraintsList.add(securityConstraintMD);
            jbossWebMD.setSecurityConstraints(securityConstraintsList);
        } else {
            boolean endpointManagementExists = false;
            for (SecurityConstraintMetaData item : jbossWebMD.getSecurityConstraints()) {
                 for (WebResourceCollectionMetaData resourceCollectionMD : item.getResourceCollections()) {
                      if (MANAGEMENT_RESOURCE_NAME.endsWith(resourceCollectionMD.getWebResourceName())) {
                          endpointManagementExists = true;
                          break;
                      }
                 }
            }
            if (!endpointManagementExists) {
                jbossWebMD.getSecurityConstraints().add(securityConstraintMD);
            }
        }
        //Add default login config for management resource
        if (jbossWebMD.getLoginConfig() == null) {
            LoginConfigMetaData login = new LoginConfigMetaData();
            login.setRealmName(MANAGEMENT_REALM);
            login.setAuthMethod(MANAGEMENT_AUTH_METHOD);
            jbossWebMD.setLoginConfig(login);
        }
        //Add default security domain if not exists
        if (jbossWebMD.getSecurityDomain() == null) {
            jbossWebMD.setSecurityDomain(SecurityConstants.DEFAULT_APPLICATION_POLICY);
        }
    }

    /**
     * Configures transport servlet class for every found webservice endpoint.
     *
     * @param dep webservice deployment
     * @param jbossWebMD web meta data
     */
    private void configureEndpoints(final Deployment dep, final JBossWebMetaData jbossWebMD) {
        final String transportClassName = this.getTransportClassName(dep);
        WSLogger.ROOT_LOGGER.trace("Modifying servlets");

        // get a list of the endpoint bean class names
        final Set<String> epNames = new HashSet<String>();
        for (Endpoint ep : dep.getService().getEndpoints()) {
            epNames.add(ep.getTargetBeanName());
        }

        // fix servlet class names for endpoints
        for (final ServletMetaData servletMD : jbossWebMD.getServlets()) {
            final String endpointClassName = ASHelper.getEndpointClassName(servletMD);
            if (endpointClassName != null && endpointClassName.length() > 0) { // exclude JSP
                if (epNames.contains(endpointClassName)) {
                    // set transport servlet
                    servletMD.setServletClass(WSFServlet.class.getName());
                    WSLogger.ROOT_LOGGER.tracef("Setting transport class: %s for endpoint: %s", transportClassName, endpointClassName);
                    final List<ParamValueMetaData> initParams = WebMetaDataHelper.getServletInitParams(servletMD);
                    // configure transport class name
                    WebMetaDataHelper.newParamValue(WSFServlet.STACK_SERVLET_DELEGATE_CLASS, transportClassName, initParams);
                    // configure webservice endpoint
                    WebMetaDataHelper.newParamValue(Endpoint.SEPID_DOMAIN_ENDPOINT, endpointClassName, initParams);
                } else if (endpointClassName.startsWith("org.apache.cxf")) {
                    throw WSLogger.ROOT_LOGGER.invalidWSServlet(endpointClassName);
                }
            }
        }
    }

    /**
     * Modifies context root.
     *
     * @param dep webservice deployment
     * @param jbossWebMD web meta data
     */
    private void modifyContextRoot(final Deployment dep, final JBossWebMetaData jbossWebMD) {
        final String contextRoot = dep.getService().getContextRoot();
        WSLogger.ROOT_LOGGER.tracef("Setting context root: %s for deployment: %s", contextRoot, dep.getSimpleName());
        jbossWebMD.setContextRoot(contextRoot);
    }

    /**
     * Returns stack specific transport class name.
     *
     * @param dep webservice deployment
     * @return stack specific transport class name
     * @throws IllegalStateException if transport class name is not found in deployment properties map
     */
    private String getTransportClassName(final Deployment dep) {
        String transportClassName = (String) dep.getProperty(WSConstants.STACK_TRANSPORT_CLASS);
        if (transportClassName == null) throw WSLogger.ROOT_LOGGER.missingDeploymentProperty(WSConstants.STACK_TRANSPORT_CLASS);
        return transportClassName;
    }

}
