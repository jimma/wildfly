package org.jboss.as.webservices.security;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
//TODO: Talk with ElY team to see what ELY or ELY wrapper class can proivde this ability. 
//In old picketbox, if credential is valid, the subject has roles info. 
//Now Elytron's SecurityDomain, ServerAuthenticationContext,SecurityIdentity are not public .    
public class ElytronSecurityDomainContextImpl implements org.jboss.wsf.spi.security.SecurityDomainContext {
   
    
    @Override
    public String getSecurityDomain() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isValid(Principal principal, Object credential, Subject activeSubject) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean doesUserHaveRole(Principal principal, Set<Principal> roles) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<Principal> getUserRoles(Principal principal) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pushSubjectContext(Subject subject, Principal principal, Object credential) {
        // TODO Auto-generated method stub
        
    }

}
