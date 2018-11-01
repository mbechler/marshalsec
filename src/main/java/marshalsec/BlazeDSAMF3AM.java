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

import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfMessageDeserializer;
import flex.messaging.io.amf.AmfMessageSerializer;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.amf.MessageHeader;


/**
 * AMF3 serialization, payload wrapped in ActionMessage
 * 
 * @author mbechler
 *
 */
public class BlazeDSAMF3AM extends BlazeDSAMF3 {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.BlazeDSBase#marshal(java.lang.Object)
     */
    @Override
    public byte[] marshal ( Object o ) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SerializationContext sc = new SerializationContext();
        AmfMessageSerializer serializer = new AmfMessageSerializer();
        serializer.initialize(sc, bos, new AmfTrace());
        ActionMessage am = new ActionMessage(MessageIOConstants.AMF3);
        am.addHeader(new MessageHeader("payl", false, o));
        serializer.writeMessage(am);
        return bos.toByteArray();
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.BlazeDSBase#unmarshal(byte[])
     */
    @Override
    public Object unmarshal ( byte[] data ) throws Exception {
        SerializationContext sc = new SerializationContext();
        AmfMessageDeserializer deserializer = new AmfMessageDeserializer();
        deserializer.initialize(sc, new ByteArrayInputStream(data), new AmfTrace());
        ActionMessage am = new ActionMessage(MessageIOConstants.AMF3);
        ActionContext ac = new ActionContext();
        deserializer.readMessage(am, ac);
        return am.getHeader(0);
    }


    public static void main ( String[] args ) {
        new BlazeDSAMF3AM().run(args);
    }
}
