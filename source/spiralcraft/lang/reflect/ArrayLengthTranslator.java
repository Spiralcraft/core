package spiralcraft.lang.reflect;

import java.lang.reflect.Array;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;

class ArrayLengthTranslator<S>
  implements Translator<Integer,S>
{
  private Reflector<Integer> _reflector;
  
  public ArrayLengthTranslator()
  { 
    try
    { _reflector=BeanReflector.<Integer>getInstance(Integer.class);
    }
    catch (BindException x)
    { x.printStackTrace();
    }
  }
  
  @Override
  public Integer translateForGet(S source,Channel<?>[] params)
  { return Array.getLength(source);
  }
  
  @Override
  public S translateForSet(Integer length,Channel<?>[] params)
  { throw new UnsupportedOperationException("Cannot set array length");
  }
  
  public Reflector<Integer> getReflector()
  { return _reflector;
  }
  
}
