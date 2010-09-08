package spiralcraft.lang.functions;

import java.util.Comparator;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

@SuppressWarnings("unchecked")

/**
 * Defines an ordering over a function of a collection item
 */
public class Ordering<T,I>
{
  private Ordering<T,?> subOrdering;
  private Expression<I> x=Expression.<I>create(".");
  private boolean reverse;
  
  
  @SuppressWarnings("rawtypes")
  private Comparator<I> comparator=new DefaultComparator();
  
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
  
  class OrderingComparator
  {
    private Ordering<T,?>.OrderingComparator subComparator;
    private Channel<I> item1;
    private Channel<I> item2;
    
    public OrderingComparator(Focus<T> item1Focus,Focus<T> item2Focus)
      throws BindException
    { 
      this.item1=item1Focus.<I>bind(x);
      this.item2=item2Focus.<I>bind(x);

      if (subOrdering!=null)
      { this.subComparator=subOrdering.bind(item1Focus,item2Focus);
      }
    }
    
    public int compare()
    {
      int ret=comparator.compare(item1.get(),item2.get());
      
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
  
  static class DefaultComparator<X extends Comparable<X>>
    implements Comparator<X>
  {
    
    @Override
    public int compare(
      X o1,
      X o2)
    { return o1.compareTo(o2);
    }
  }

}
