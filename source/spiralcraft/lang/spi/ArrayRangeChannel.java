package spiralcraft.lang.spi;

import java.lang.reflect.Array;
import java.util.LinkedList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Range;
import spiralcraft.lang.Reflector;

public class ArrayRangeChannel<X>
  extends SourcedChannel<X[],X[]>
{

  private final Channel<Range> range;
  private final Reflector<X> componentReflector;
  
  /**
   * 
   * @param source A Channel which provides the source array
   * @param componentChannel The channel against which the selector is bound
   * @param selector The selector which evaluates the filter expression
   *  
   */
  public ArrayRangeChannel
    (Channel<X[]> source
    ,Reflector<X> componentReflector
    ,Channel<Range> range
    )
  { 
    super(source.getReflector(),source);
    this.componentReflector=componentReflector;
    this.range=range;
   
  }
    
    
  @SuppressWarnings("unchecked") // Array instantiation
  @Override
  protected X[] retrieve()
  {
    X[] array=source.get();
    if (array==null)
    { return null;
    }

    LinkedList<X> list=new LinkedList<X>();
    Range range=this.range.get();
    
    Number start=range.getStart();
    
    if (start==null)
    { return (X[]) Array.newInstance(componentReflector.getContentType(),0);
    }
    
    int end
      =range.getEnd()==null
      ?array.length
      :range.isInclusive()
        ?range.getEnd().intValue()+1
        :range.getEnd().intValue();
    
    end=Math.min(end,array.length);
    
    for (int i=start.intValue();i<end;i++)
    { list.add(array[i]);
    }

    
    X[] ret=(X[]) Array.newInstance
      (componentReflector.getContentType()
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
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
