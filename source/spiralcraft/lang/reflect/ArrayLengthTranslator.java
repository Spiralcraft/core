package spiralcraft.lang.reflect;

import java.lang.reflect.Array;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;

class ArrayLengthTranslator<S>
  implements Translator<Integer,S>
{
  private Reflector<Integer> _reflector
    =BeanReflector.<Integer>getInstance(Integer.class);
    
  @Override
  public Integer translateForGet(S source,Channel<?>[] params)
  { return Array.getLength(source);
  }
  
  @Override
  public S translateForSet(Integer length,Channel<?>[] params)
  { throw new UnsupportedOperationException("Cannot set array length");
  }
  
  /**
   * An array's length can never change, therefore array length is a
   *   function on the domain of arrays.
   */
  public boolean isFunction()
  { return true;
  }
  
  public Reflector<Integer> getReflector()
  { return _reflector;
  }
  
}
