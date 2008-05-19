package spiralcraft.data.lang;


import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.spi.EditableArrayListAggregate;

public class AggregateSelectChannel<X>
  extends AbstractChannel<Aggregate<X>>
{

  private final Channel<Aggregate<X>> source;
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
    (Channel<Aggregate<X>> source
    ,ThreadLocalChannel<X> componentChannel
    ,Channel<Boolean> selector
    )
  { 
    super(source.getReflector());
    this.source=source;
    this.componentChannel=componentChannel;
    this.selector=selector;   
  }
    
    
  @SuppressWarnings("unchecked") // Array instantiation
  @Override
  protected Aggregate<X> retrieve()
  {
    Aggregate<X> aggregate=source.get();
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
  
  public boolean isWritable()
  { return false;
  }

}
