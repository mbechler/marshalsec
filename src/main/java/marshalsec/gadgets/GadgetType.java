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


/**
 * @author mbechler
 *
 */
public enum GadgetType {

    UnicastRef(UnicastRefGadget.class),
    UnicastRemoteObject(UnicastRemoteObjectGadget.class),
    Groovy(Groovy.class),
    SpringPropertyPathFactory(SpringPropertyPathFactory.class),
    SpringPartiallyComparableAdvisorHolder(SpringPartiallyComparableAdvisorHolder.class),
    SpringAbstractBeanFactoryPointcutAdvisor(SpringAbstractBeanFactoryPointcutAdvisor.class),
    Rome(Rome.class),
    XBean(XBean.class),
    Resin(Resin.class),
    CommonsConfiguration(CommonsConfiguration.class),
    LazySearchEnumeration(LazySearchEnumeration.class),
    BindingEnumeration(BindingEnumeration.class),
    ServiceLoader(ServiceLoader.class),
    ImageIO(ImageIO.class),
    CommonsBeanutils(CommonsBeanutils.class),
    C3P0WrapperConnPool(C3P0WrapperConnPool.class),
    C3P0RefDataSource(C3P0RefDataSource.class),
    JdbcRowSet(JdbcRowSet.class),
    ScriptEngine(ScriptEngine.class),
    Templates(Templates.class),
    ResourceGadget(ResourceGadget.class),

    //
    ;

    private Class<? extends Gadget> clazz;


    private GadgetType ( Class<? extends Gadget> clazz ) {
        this.clazz = clazz;
    }


    /**
     * @return the clazz
     */
    public Class<? extends Gadget> getClazz () {
        return this.clazz;
    }
}
