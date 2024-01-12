package spiralcraft.data.core;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.Channel;

/**
 * Represents a value derived from an expression that can be used as an
 *   argument for a type parameter.
 *   
 * @author mike
 *
 * @param <T>
 */
public class DerivedValue<T>
{
  private final Expression<T> x;
  private T value;
  boolean bound=false;

  public DerivedValue(String xs)
    throws ParseException
  {
    x=Expression.parse(xs);
  }  
  
  public DerivedValue(Expression<T> x)
  { this.x=x;
  }

  public synchronized void ensureBound(Focus<?> typeFocus)
    throws BindException
  { 
    if (!bound)
    {
      Channel<T> c=typeFocus.bind(x);
      value=c.get();
    }
  }
  
  public boolean isBound()
  { return bound;
  }
  
  public T get()
  { return value;
  }

  public String toString()
  { return x.getText();
  }
}
