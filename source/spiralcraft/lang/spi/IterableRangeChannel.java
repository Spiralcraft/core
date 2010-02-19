package spiralcraft.lang.spi;



import java.util.ArrayList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Range;

public class IterableRangeChannel<X>
  extends AbstractChannel<Iterable<X>>
{

  private final Channel<Iterable<X>> source;
  private final Channel<Range> range;
  
  /**
   * 
   * @param source A Channel which provides the source Iterable
   * @param componentReflector The component type of the source list
   * @param range A Channel which provides the range values
   *  
   */
  public IterableRangeChannel
    (Channel<Iterable<X>> source
    ,Channel<Range> range
    )
    throws BindException
  { 
    super(source.getReflector());
    this.source=source;
    this.range=range;
   
  }
    
  @Override
  protected Iterable<X> retrieve()
  {
    Iterable<X> sourceList=source.get();
    if (sourceList==null)
    { return null;
    }


    Range range=this.range.get();
    
    ArrayList<X> targetList=new ArrayList<X>();
    
    int start=range.getStart().intValue();
    
    int end
      =range.getEnd()==null
      ?-1
      :(range.isInclusive()
        ?range.getEnd().intValue()+1
        :range.getEnd().intValue()
        )
      ;
    
    int pos=0;
    for (X val : sourceList)
    { 
      if (pos>=start
          && (end<0 || pos<end)
          )
      { targetList.add(val);
      }
    }
    
    return targetList;
  }

  @Override
  protected boolean store(
    Iterable<X> val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
