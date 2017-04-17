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


import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public interface C3P0RefDataSource extends Gadget {

    @Primary
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    default Object makeRefDataSource ( UtilFactory uf, String[] args ) throws Exception {
        Object obj = Reflections.createWithoutConstructor(Class.forName("com.mchange.v2.c3p0.JndiRefForwardingDataSource"));
        // requires ordering
        Reflections.setFieldValue(obj, "jndiName", args[ 0 ]);
        Reflections.setFieldValue(obj, "loginTimeout", 0);
        return obj;
    }

}
