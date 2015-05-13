package spiralcraft.lang.functions;

import java.util.Comparator;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;

@SuppressWarnings("unchecked")

/**
 * Defines an ordering over a function of a collection item
 */
public class Ordering<T,I>
{
  @SuppressWarnings("rawtypes")
  private static final DefaultComparator defaultComparator
    =new DefaultComparator();
  private static final NumberComparator numberComparator
    =new NumberComparator();
  
  private Ordering<T,?> subOrdering;
  private Expression<I> x=Expression.<I>create(".");
  private boolean reverse;
  
  
  private Comparator<I> comparator;
  
  public Ordering()
  {
  }
  
  public Ordering(boolean reverse)
  { this.reverse=reverse;
  }

  public Ordering(Expression<I> x,boolean reverse)
  { 
    this.x=x;
    this.reverse=reverse;
  }
  
  public Ordering(Expression<I> x,boolean reverse,Ordering<T,?> subOrdering)
  { 
    this.x=x;
    this.reverse=reverse;
    this.subOrdering=subOrdering;
  }
  
  public void setComparator(Comparator<I> comparator)
  { this.comparator=comparator;
  }
  
  public OrderingComparator bind
    (Focus<T> item1Focus
    ,Focus<T> item2Focus
    )
    throws BindException
  { return new OrderingComparator(item1Focus,item2Focus);
  }
  
  @SuppressWarnings("rawtypes")
  private Comparator resolveComparator(Reflector<I> item1,Reflector<I> item2)
  {
    if (item1.getContentType()==Number.class 
        || item2.getContentType()==Number.class
        )
    { return numberComparator;
    }
    return defaultComparator;
  }
  
  class OrderingComparator
  {
    private final Comparator<I> actualComparator;
    private final Ordering<T,?>.OrderingComparator subComparator;
    private final Channel<I> item1;
    private final Channel<I> item2;
    
    public OrderingComparator(Focus<T> item1Focus,Focus<T> item2Focus)
      throws BindException
    { 
      this.item1=item1Focus.<I>bind(x);
      this.item2=item2Focus.<I>bind(x);
      
      actualComparator
        =comparator!=null
          ?comparator
          :resolveComparator(item1.getReflector(),item2.getReflector());
      
      if (subOrdering!=null)
      { this.subComparator=subOrdering.bind(item1Focus,item2Focus);
      }
      else
      { this.subComparator=null;
      }
    }
    
    public int compare()
    {
      int ret=actualComparator.compare(item1.get(),item2.get());
      
      if (ret==0)
      { 
        if (subComparator!=null)
        { return subComparator.compare();
        }
        else
        { return 0;
        }
      }
      else
      { return reverse?-ret:ret;
      }
    }
    
    
  }
  

}

class NumberComparator
  implements Comparator<Number>
{
  @Override
  public final int compare(Number o1,Number o2)
  { 
    if (o1==null)
    { return o2==null?0:-1;
    }
    else if (o2==null)
    { return 1;
    }      
    else if (o1!=o2)
    { return Double.compare(o1.doubleValue(),o2.doubleValue());
    }
    else
    { return 0;
    }
  }
}

class DefaultComparator<X extends Comparable<X>>
  implements Comparator<X>
{
  
  @Override
  public final int compare(
    X o1,
    X o2)
  { 
    if (o1==null)
    { return o2==null?0:-1;
    }
    else if (o2==null)
    { return 1;
    }
    return o1.compareTo(o2);
  }
}
