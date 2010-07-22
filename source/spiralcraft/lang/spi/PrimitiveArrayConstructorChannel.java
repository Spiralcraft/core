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

public class PrimitiveArrayConstructorChannel<C,T>
  extends SourcedChannel<C,Object>
{
  private Reflector<T> componentReflector;
  private IterationDecorator<C,T> decorator;
  
  @SuppressWarnings("unchecked")
  public PrimitiveArrayConstructorChannel
    (Reflector componentReflector
    ,Channel<C> source
    ,IterationDecorator<C,T> sourceIterable
    )
    throws BindException
  { 
    super((Reflector<Object>) ArrayReflector.getInstance(componentReflector)
         ,source
         );
    this.componentReflector=componentReflector;
    this.decorator
      =sourceIterable;
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

  @Override
  protected Object retrieve()
  {
    List<T> list=new ArrayList<T>();
    
    Iterator<T> it=decorator.iterator();
    if (it==null)
    { return null;
    }
    
    while (it.hasNext())
    { list.add(it.next());
    }
    
    Object array=Array.newInstance
      (componentReflector.getContentType(), list.size());
    
    try
    { 
      int i=0;
      for (T t:list)
      { Array.set(array,i++,t);
      }
    }
    catch (ArrayStoreException x)
    { 
      throw new IllegalArgumentException
        ("Array "+array
        +" could not store values "+list+" returned from "+componentReflector
        ,x
        );
    }
    return array;

  }

  @Override
  protected boolean store(
    Object val)
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
