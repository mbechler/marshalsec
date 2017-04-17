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


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.internal.Annotations;

import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public class MockProxies {

    public enum MockProxyType {
        GUICE, HIBERNATEVAL, JAVA
    }


    public static Object makeProxy ( MockProxyType t, String method, Object iter, Class<?>... types ) throws ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
        switch ( t ) {
        case GUICE:
            return makeProxyGuice(method, iter, types);
        case HIBERNATEVAL:
            return makeProxyHibernate(method, iter, types);
        case JAVA:
        default:
            return makeProxyJava(method, iter, types);
        }
    }


    private static Object makeProxyJava ( String method, Object iter, Class<?>... types ) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
        Map<String, Object> values = new HashMap<>();
        values.put(method, iter);
        return Proxy.newProxyInstance(MockProxies.class.getClassLoader(), types, JDKUtil.createMemoizedInvocationHandler(values));
    }


    private static Object makeProxyGuice ( String method, Object iter, Class<?>... types ) throws Exception {
        Method meth = Annotations.class.getDeclaredMethod("generateAnnotationImpl", Class.class); //$NON-NLS-1$
        meth.setAccessible(true);
        Object o = meth.invoke(null, Override.class);
        InvocationHandler inv = Proxy.getInvocationHandler(o);
        Map<String, Object> values = new HashMap<>();
        values.put(method, iter);
        Reflections.setFieldValue(inv, "val$members", values);
        return Proxy.newProxyInstance(MockProxies.class.getClassLoader(), types, inv);
    }


    private static Object makeProxyHibernate ( String method, Object iter, Class<?>... types ) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
        Class<?> invhcl = Class.forName("org.hibernate.validator.util.annotationfactory.AnnotationProxy");
        InvocationHandler invh = (InvocationHandler) Reflections.createWithoutConstructor(invhcl);
        Map<String, Object> values = new HashMap<>();
        values.put(method, iter);
        Reflections.setFieldValue(invh, "values", values);

        return Proxy.newProxyInstance(MockProxies.class.getClassLoader(), types, invh);
    }

}
