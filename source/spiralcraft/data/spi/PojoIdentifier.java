package spiralcraft.data.spi;

import java.lang.ref.WeakReference;

import spiralcraft.data.Identifier;
import spiralcraft.data.Type;

/**
 * Identifies by Java identity, using System.identityHashCode
 *   to provide a stable HashCode
 * 
 * @author mike
 *
 */
public class PojoIdentifier<T>
  implements Identifier
{
  
  private WeakReference<T> instance;
  private int hashCode;
  
  public boolean instanceIs(T other)
  { return other==instance.get();
  }

  public PojoIdentifier(T instance)
  { 
    this.instance=new WeakReference<T>(instance);
    hashCode=System.identityHashCode(instance);
  }
  
  @Override
  public Identifier copy()
  { return new PojoIdentifier<T>(instance.get());
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(
    Identifier identifier)
  {
    if (!(identifier instanceof PojoIdentifier))
    { return false;
    }
    else
    { return ((PojoIdentifier<T>) identifier).instanceIs(instance.get());
    }

  }

  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public Type<?> getIdentifiedType()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
