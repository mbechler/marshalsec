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


import java.io.StringWriter;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

import marshalsec.gadgets.C3P0WrapperConnPool;


/**
 * @author mbechler
 *
 */
public class YAMLBeans extends YAMLBase implements C3P0WrapperConnPool {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( Object o ) throws Exception {
        YamlConfig yc = new YamlConfig();
        StringWriter sw = new StringWriter();
        YamlWriter w = new YamlWriter(sw, yc);
        w.write(o);
        return sw.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal ( String data ) throws Exception {
        YamlConfig yc = new YamlConfig();
        YamlReader r = new YamlReader(data, yc);
        return r.read();
    }


    @Override
    protected boolean constructorArgumentsSupported () {
        return false;
    }


    @Override
    protected String constructorPrefix ( boolean inline ) {
        return "!";
    }


    public static void main ( String[] args ) {
        new YAMLBeans().run(args);
    }
}
