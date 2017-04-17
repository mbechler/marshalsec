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


import java.util.Comparator;

import marshalsec.gadgets.JDKUtil;
import marshalsec.gadgets.ToStringUtil;


/**
 * @author mbechler
 *
 */
public interface UtilFactory {

    default Object makeHashCodeTrigger ( Object o1 ) throws Exception {
        return JDKUtil.makeMap(o1, o1);
    }


    default Object makeEqualsTrigger ( Object tgt, Object sameHash ) throws Exception {
        return JDKUtil.makeMap(tgt, sameHash);
    }


    Object makeToStringTriggerUnstable ( Object obj ) throws Exception;


    default Object makeToStringTriggerStable ( Object obj ) throws Exception {
        return ToStringUtil.makeToStringTrigger(obj);
    }


    default Object makeIteratorTrigger ( Object it ) throws Exception {
        return JDKUtil.makeIteratorTriggerNative(this, it);
    }


    default Object makeComparatorTrigger ( Object tgt, Comparator<?> cmp ) throws Exception {
        return JDKUtil.makeTreeMap(tgt, cmp);
    }
}
