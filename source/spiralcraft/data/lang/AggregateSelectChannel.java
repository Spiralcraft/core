package spiralcraft.data.lang;


import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.spi.EditableArrayListAggregate;

public class AggregateSelectChannel<T extends Aggregate<X>,X>
  extends AbstractChannel<Aggregate<X>>
{

  private final Channel<T> source;
  private final ThreadLocalChannel<X> componentChannel;
  private final Channel<Boolean> selector;
  
  /**
   * 
   * @param source A Channel which provides the source array
   * @param componentChannel The channel against which the selector is bound
   * @param selector The selector which evaluates the filter expression
   *  
   */
  
  public AggregateSelectChannel
    (Channel<T> source
    ,ThreadLocalChannel<X> componentChannel
    ,Channel<Boolean> selector
    )
    throws BindException
  { 
    super(DataReflector.<Aggregate<X>> getInstance
        ( ((DataReflector<?>) source.getReflector()).getType()));
    this.source=source;
    this.componentChannel=componentChannel;
    this.selector=selector;   
  }
    
    
  @Override
  protected Aggregate<X> retrieve()
  {
    T aggregate=source.get();
    if (aggregate==null)
    { return null;
    }
    EditableAggregate<X> ret=new EditableArrayListAggregate<X>(aggregate.getType());
    componentChannel.push(null);
    try
    {
      for (X item: aggregate)
      {
        componentChannel.set(item);
        if (selector.get())
        { ret.add(item);
        }
      }
    }
    finally
    { componentChannel.pop();
    }
    
    return ret;
  }

  @Override
  protected boolean store(
    Aggregate<X> val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
