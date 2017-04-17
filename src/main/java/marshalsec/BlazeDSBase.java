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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.jndi.support.SimpleJndiBeanFactory;

import com.mchange.v2.c3p0.WrapperConnectionPoolDataSource;

import flex.messaging.io.BeanProxy;
import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.AbstractAmfInput;
import flex.messaging.io.amf.AbstractAmfOutput;
import marshalsec.gadgets.Args;
import marshalsec.gadgets.C3P0WrapperConnPool;
import marshalsec.gadgets.SpringPropertyPathFactory;
import marshalsec.util.Reflections;


/**
 * 
 * Not applicable:
 * - C3P0RefDataSource as a public constructor is required
 * - JdbcRowSet as there is custom conversion for RowSet sub-types
 * 
 * @author mbechler
 *
 */
public abstract class BlazeDSBase extends MarshallerBase<byte[]> implements C3P0WrapperConnPool, SpringPropertyPathFactory {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public byte[] marshal ( Object o ) throws Exception {
        SerializationContext sc = new SerializationContext();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try ( AbstractAmfOutput out = createOutput(sc) ) {
            out.setOutputStream(bos);
            out.writeObject(o);
            return bos.toByteArray();
        }
    }


    protected abstract AbstractAmfOutput createOutput ( SerializationContext sc );


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal ( byte[] data ) throws Exception {
        SerializationContext sc = new SerializationContext();
        try ( AbstractAmfInput in = createInput(sc) ) {
            in.setInputStream(new ByteArrayInputStream(data));
            return in.readObject();
        }
    }


    @Override
    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    public Object makeWrapperConnPool ( UtilFactory uf, String[] args ) throws Exception {
        return new PropertyInjectingProxy(
            Reflections.createWithoutConstructor(WrapperConnectionPoolDataSource.class),
            Collections.singletonMap("userOverridesAsString", C3P0WrapperConnPool.makeC3P0UserOverridesString(args[ 0 ], args[ 1 ])));
    }


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
            Collections.singletonMap("shareableResources", Arrays.asList(jndiUrl) // this would actually be an array,
                                                                                  // but AMFX has some trouble with
                                                                                  // non-readable array properties
        ));

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("targetBeanName", jndiUrl);
        values.put("propertyPath", "foo");
        values.put("beanFactory", bfproxy);
        return new PropertyInjectingProxy(new PropertyPathFactoryBean(), values);
    }


    protected abstract AbstractAmfInput createInput ( SerializationContext sc );

    /**
     * 
     * Bean proxy to support partial marshalling as well as ordering of properties and setting write-only properties
     * 
     * @author mbechler
     *
     */
    public final class PropertyInjectingProxy extends BeanProxy {

        private static final long serialVersionUID = 4559272383186706846L;
        private Map<String, Object> values;


        public PropertyInjectingProxy ( Object defaultInstance, Map<String, Object> v ) {
            super(defaultInstance);
            this.values = v;
        }


        @Override
        public List getPropertyNames ( Object instance ) {
            List<String> l = super.getPropertyNames(instance);
            l.addAll(this.values.keySet());
            return new ArrayList<>(this.values.keySet());
        }


        @Override
        public boolean isWriteOnly ( Object instance, String propertyName ) {
            if ( this.values.containsKey(propertyName) ) {
                return false;
            }
            return super.isWriteOnly(instance, propertyName);
        }


        @Override
        public Object getValue ( Object instance, String propertyName ) {
            if ( this.values.containsKey(propertyName) ) {
                return this.values.get(propertyName);
            }
            return super.getValue(instance, propertyName);
        }
    }
}
