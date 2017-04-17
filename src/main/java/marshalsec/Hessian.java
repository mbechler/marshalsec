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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;


/**
 * @author mbechler
 *
 */
public class Hessian extends HessianBase {

    /**
     * {@inheritDoc}
     *
     * @see marshalsec.AbstractHessianBase#createOutput(java.io.ByteArrayOutputStream)
     */
    @Override
    protected AbstractHessianOutput createOutput ( ByteArrayOutputStream bos ) {
        return new HessianOutput(bos);
    }


    /**
     * {@inheritDoc}
     *
     * @see marshalsec.AbstractHessianBase#createInput(java.io.ByteArrayInputStream)
     */
    @Override
    protected AbstractHessianInput createInput ( ByteArrayInputStream bos ) {
        return new HessianInput(bos);
    }


    public static void main ( String[] args ) {
        new Hessian().run(args);
    }

}
