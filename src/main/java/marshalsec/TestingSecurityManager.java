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


import java.net.URL;
import java.security.Permission;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * @author mbechler
 *
 */
public class TestingSecurityManager extends SecurityManager {

    private String executed;
    private Set<URL> remoteCodebases = new HashSet<>();


    /**
     * {@inheritDoc}
     *
     * @see java.lang.SecurityManager#checkExec(java.lang.String)
     */
    @Override
    public void checkExec ( String cmd ) {
        this.executed = cmd;
        throw new java.lang.SecurityException("Not calling executable " + cmd);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
     */
    @Override
    public void checkPermission ( Permission perm ) {

        if ( perm instanceof RuntimePermission ) {
            return;
        }

        Set<URL> cbs = new HashSet<>();
        for ( Class<?> cl : getClassContext() ) {
            if ( cl.getProtectionDomain() != null && cl.getProtectionDomain().getCodeSource() != null
                    && cl.getProtectionDomain().getCodeSource().getLocation() != null
                    && !"file".equals(cl.getProtectionDomain().getCodeSource().getLocation().getProtocol()) ) {
                cbs.add(cl.getProtectionDomain().getCodeSource().getLocation());
            }
        }

        this.remoteCodebases.addAll(cbs);
    }


    public void assertRCE () throws Exception {

        if ( this.executed != null ) {
            System.err.println("Had execution of " + this.executed);
            return;
        }

        if ( !this.remoteCodebases.isEmpty() ) {
            System.err.println("Had execution from " + this.remoteCodebases);
            return;
        }

        throw new Exception("Did not trigger RCE");
    }

}
