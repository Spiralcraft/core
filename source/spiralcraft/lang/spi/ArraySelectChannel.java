package spiralcraft.lang.spi;

import java.lang.reflect.Array;
import java.util.LinkedList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;

public class ArraySelectChannel<X>
  extends AbstractChannel<X[]>
{

  private final Channel<X[]> source;
  private final ThreadLocalChannel<X> componentChannel;
  private final Channel<Boolean> selector;
  
  /**
   * 
   * @param source A Channel which provides the source array
   * @param componentChannel The channel against which the selector is bound
   * @param selector The selector which evaluates the filter expression
   *  
   */
  public ArraySelectChannel
    (Channel<X[]> source
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
  protected X[] retrieve()
  {
    X[] array=source.get();
    LinkedList<X> list=new LinkedList<X>();
    componentChannel.push(null);
    try
    {
      for (int i=0;i<array.length;i++)
      {
        componentChannel.set(array[i]);
        if (selector.get())
        { list.add(array[i]);
        }
      }
    }
    finally
    { componentChannel.pop();
    }
    
    X[] ret=(X[]) Array.newInstance
      (componentChannel.getReflector().getContentType()
      ,list.size()
      );
    
    return list.toArray(ret);
  }

  @Override
  protected boolean store(
    X[] val)
    throws AccessException
  { return false;
  }
  
  public boolean isWritable()
  { return false;
  }

}
