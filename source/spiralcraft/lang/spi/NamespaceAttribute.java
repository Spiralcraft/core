package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

class NamespaceAttribute<T>
  implements Translator<T,Namespace>
{
  private final Reflector<T> type;
  private final int index;
  
  public NamespaceAttribute(Reflector<T> type,int index)
  {
    this.type=type;
    this.index=index;
  }
  
  public Reflector<T> getReflector()
  { return type;
  }

  @SuppressWarnings("unchecked") // Heterogeneous collection
  public T translateForGet(Namespace source, Channel<?>[] modifiers)
  { return (T) source.getChannel(index).get();
  }

  
  public Namespace translateForSet(T source, Channel<?>[] modifiers)
  { throw new UnsupportedOperationException("Operation is not reversible");
  }
  
  /**
   * Namespaces are mutable
   */
  public boolean isFunction()
  { return false;
  }
  
  int getIndex()
  { return index;
  }
  
}
