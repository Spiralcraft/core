/**
 * Construct an Array from a source that is Iterable
 */
package spiralcraft.lang.spi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.ArrayReflector;

public class ArrayConstructorChannel<C,T>
  extends AbstractChannel<T[]>
{
  private Reflector<T> componentReflector;
  private Channel<C> source;
  private IterationDecorator<C,T> decorator;
  
  @SuppressWarnings("unchecked") // Decorator call
  public ArrayConstructorChannel
    (Reflector<T> componentReflector
    ,Channel<C> source)
    throws BindException
  { 
    super(ArrayReflector.getInstance(componentReflector));
    this.componentReflector=componentReflector;
    this.source=source;
    this.decorator
      =source.<IterationDecorator>decorate(IterationDecorator.class);
    if (decorator==null)
    { throw new IllegalArgumentException("Iteration not supported for "+source);
    }
    
    // Check type- Void.class means that the source contains nothing or a
    //   list of nulls
    if (decorator.getComponentReflector()!=null
        && decorator.getComponentReflector().getContentType()!=Void.class)
    {
      if (!componentReflector.isAssignableFrom
          (decorator.getComponentReflector())
         )
      { 
        throw new BindException
          ("Incompatible types: "+decorator.getComponentReflector().getTypeURI()
            +" cannot be stored in an array of type "+componentReflector.getTypeURI()
          );
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T[] retrieve()
  {
    List<T> list=new ArrayList<T>();
    
    Iterator<T> it=decorator.iterator();
    if (it==null)
    { return null;
    }
    
    while (it.hasNext())
    { list.add(it.next());
    }
    
    T[] array=(T[]) Array.newInstance
      (componentReflector.getContentType(), list.size());
    
    try
    { return list.toArray(array);
    }
    catch (ArrayStoreException x)
    { 
      throw new IllegalArgumentException
        ("Array "+array
        +" could not store values "+list+" returned from "+componentReflector
        ,x
        );
    }

  }

  @Override
  protected boolean store(
    T[] val)
    throws AccessException
  { return false;
  }

  @Override
  public boolean isWritable()
  { return false;
  }
  
  @Override
  public boolean isConstant()
  { return source.isConstant();
  }
  
}
