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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;

import marshalsec.gadgets.CommonsBeanutils;
import marshalsec.gadgets.JDKUtil;
import marshalsec.gadgets.XBean;


/**
 * @author mbechler
 *
 */
public class Java extends MarshallerBase<byte[]> implements CommonsBeanutils, XBean {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public byte[] marshal ( Object o ) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( ObjectOutputStream oos = new ObjectOutputStream(bos) ) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal ( byte[] data ) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try ( ObjectInputStream ois = new ObjectInputStream(bis) ) {
            return ois.readObject();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.UtilFactory#makeComparatorTrigger(java.lang.Object, java.util.Comparator)
     */
    @Override
    public Object makeComparatorTrigger ( Object tgt, Comparator<?> cmp ) throws Exception {
        return JDKUtil.makePriorityQueue(tgt, cmp);
    }


    public static void main ( String[] args ) {
        new Java().run(args);
    }
}
