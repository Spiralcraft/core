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
      =source.decorate(IterationDecorator.class);
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
    
    return list.toArray(array);

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
