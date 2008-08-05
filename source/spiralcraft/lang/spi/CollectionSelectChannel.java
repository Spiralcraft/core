package spiralcraft.lang.spi;

import java.util.ArrayList;
import java.util.Collection;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;

public class CollectionSelectChannel<X>
  extends AbstractChannel<Collection<X>>
{

  private final Channel<Collection<X>> source;
  private final ThreadLocalChannel<X> componentChannel;
  private final Channel<Boolean> selector;
  
  /**
   * 
   * @param source A Channel which provides the source array
   * @param componentChannel The channel against which the selector is bound
   * @param selector The selector which evaluates the filter expression
   *  
   */
  public CollectionSelectChannel
    (Channel<Collection<X>> source
    ,ThreadLocalChannel<X> componentChannel
    ,Channel<Boolean> selector
    )
  { 
    super(source.getReflector());
    this.source=source;
    this.componentChannel=componentChannel;
    this.selector=selector;   
  }
    
    
  @Override
  protected Collection<X> retrieve()
  {
    Collection<X> collection=source.get();
    ArrayList<X> list=new ArrayList<X>();
    componentChannel.push(null);
    try
    {
      for (X item: collection)
      {
        componentChannel.set(item);
        if (selector.get())
        { list.add(item);
        }
      }
    }
    finally
    { componentChannel.pop();
    }
    
    return list;
  }

  @Override
  protected boolean store(
    Collection<X> val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
