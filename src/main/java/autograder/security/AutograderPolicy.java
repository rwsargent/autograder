package autograder.security;

import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;

public class AutograderPolicy extends Policy {
    
    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        if (isPlugin(domain)) {
            return pluginPermissions();
        }
        else {
            return applicationPermissions();
        }        
    }
 
    private boolean isPlugin(ProtectionDomain domain) {
        return domain.getClassLoader() instanceof URLClassLoader;
    }
 
    private PermissionCollection pluginPermissions() {
        Permissions permissions = new Permissions(); // No permissions
        return permissions;
    }
 
    private PermissionCollection applicationPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        return permissions;
    }
}