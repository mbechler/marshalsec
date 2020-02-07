/* MIT License

Copyright (c) 2017 Moritz Bechler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package marshalsec;


import java.io.FilePermission;
import java.io.SerializablePermission;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;


/**
 * @author mbechler
 *
 */
public class SideEffectSecurityManager extends SecurityManager {

    /**
     * {@inheritDoc}
     *
     * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
     */
    @Override
    public void checkPermission ( Permission perm ) {
        if ( perm instanceof RuntimePermission ) {
            if ( checkRuntimePermission((RuntimePermission) perm) ) {
                return;
            }
        }
        else if ( perm instanceof ReflectPermission ) {
            return;
        }
        else if ( perm instanceof LoggingPermission ) {
            return;
        }
        else if ( perm instanceof SecurityPermission ) {
            return;
        }
        else if ( perm instanceof PropertyPermission ) {
            return;
        }
        else if ( perm instanceof NetPermission && perm.getName().equals("specifyStreamHandler") ) {
            return;
        }
        else if ( perm instanceof FilePermission && perm.getActions().equals("read") ) {
            return;
        }
        else if ( perm instanceof SerializablePermission ) {
            return;
        }

        super.checkPermission(perm);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
     */
    @Override
    public void checkPropertyAccess ( String key ) {}


    /**
     * @param perm
     */
    private static boolean checkRuntimePermission ( RuntimePermission perm ) {

        if ( perm.getName().startsWith("accessClassInPackage.") ) {
            return true;
        }

        switch ( perm.getName() ) {
        case "setSecurityManager":
            return true;
        case "accessDeclaredMembers":
            return true;
        case "reflectionFactoryAccess":
            return true;
        case "createClassLoader":
            return true;
        case "getClassLoader":
            return true;
        case "setContextClassLoader":
            return true;
        case "shutdownHooks":
            return true;
        case "loadLibrary.net":
            return true;
        case "getProtectionDomain":
            return true;
        case "accessSystemModules":
            return true;
        }

        return false;
    }

}
