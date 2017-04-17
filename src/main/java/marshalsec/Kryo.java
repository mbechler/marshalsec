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


import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import marshalsec.gadgets.CommonsBeanutils;
import marshalsec.gadgets.SpringAbstractBeanFactoryPointcutAdvisor;


/**
 * 
 * Not applicable:
 * - Most, as public default constructor is required, see {@link KryoAltStrategy}.
 * 
 * @author mbechler
 *
 */
public class Kryo extends MarshallerBase<byte[]> implements SpringAbstractBeanFactoryPointcutAdvisor, CommonsBeanutils {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public byte[] marshal ( Object o ) throws Exception {
        com.esotericsoftware.kryo.Kryo k = makeKryo();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( Output output = new Output(bos) ) {
            k.writeClassAndObject(output, o);
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
        com.esotericsoftware.kryo.Kryo k = makeKryo();
        try ( Input in = new Input(data) ) {
            return k.readClassAndObject(in);
        }
    }


    protected com.esotericsoftware.kryo.Kryo makeKryo () {
        return new com.esotericsoftware.kryo.Kryo();
    }


    public static void main ( String[] args ) {
        new Kryo().run(args);
    }
}
