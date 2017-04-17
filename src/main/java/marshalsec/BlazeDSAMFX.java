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

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AbstractAmfInput;
import flex.messaging.io.amf.AbstractAmfOutput;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageHeader;
import flex.messaging.io.amfx.AmfxMessageDeserializer;
import flex.messaging.io.amfx.AmfxMessageSerializer;


/**
 * @author mbechler
 *
 */
public class BlazeDSAMFX extends BlazeDSExternalizableBase {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public byte[] marshal ( Object o ) throws Exception {
        SerializationContext sc = new SerializationContext();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AmfxMessageSerializer out = new AmfxMessageSerializer();
        out.initialize(sc, bos, null);
        ActionMessage m = new ActionMessage();
        m.addHeader(new MessageHeader("foo", false, o));
        out.writeMessage(m);
        return bos.toByteArray();
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal ( byte[] data ) throws Exception {
        SerializationContext sc = new SerializationContext();
        AmfxMessageDeserializer amfxMessageDeserializer = new AmfxMessageDeserializer();
        amfxMessageDeserializer.initialize(sc, new ByteArrayInputStream(data), null);
        ActionMessage m = new ActionMessage();
        amfxMessageDeserializer.readMessage(m, null);
        return m.getHeader(0).getData();
    }


    @Override
    protected AbstractAmfOutput createOutput ( SerializationContext sc ) {
        return null;
    }


    @Override
    protected AbstractAmfInput createInput ( SerializationContext sc ) {
        return null;
    }


    public static void main ( String[] args ) {
        new BlazeDSAMFX().run(args);
    }

}
