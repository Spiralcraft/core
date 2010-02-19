package spiralcraft.lang.reflect;

import java.util.Iterator;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;

class IterableLengthTranslator<S extends Iterable<?>>
  implements Translator<Integer,S>
{
  private Reflector<Integer> _reflector
    =BeanReflector.<Integer>getInstance(Integer.class);
    
  @Override
  public Integer translateForGet(S source,Channel<?>[] params)
  {
    int i=0;
    Iterator<?> it=source.iterator();
    while (it.hasNext())
    { 
      it.next();
      i++;
    }
    return i;
  }
  
  @Override
  public S translateForSet(Integer length,Channel<?>[] params)
  { throw new UnsupportedOperationException("Cannot set Iterable length");
  }
  
  public Reflector<Integer> getReflector()
  { return _reflector;
  }
  
}
