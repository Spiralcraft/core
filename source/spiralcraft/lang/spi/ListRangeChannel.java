package spiralcraft.lang.spi;



import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Range;

public class ListRangeChannel<C,X>
  extends SourcedChannel<C,C>
{

  private final Channel<Range> range;
  private final ListDecorator<C,X> sourceDecorator;
  
  /**
   * 
   * @param source A Channel which provides the source list
   * @param componentReflector The component type of the source list
   * @param selector The selector which evaluates the filter expression
   *  
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public ListRangeChannel
    (Channel<C> source
    ,Channel<Range> range
    )
    throws BindException
  { 
    super(source.getReflector(),source);
    this.range=range;
    this.sourceDecorator
      =source.<ListDecorator>decorate(ListDecorator.class);
   
  }
    
  @Override
  protected C retrieve()
  {
    C sourceList=source.get();
    if (sourceList==null)
    { return null;
    }


    Range range=this.range.get();
    
    C targetList=sourceDecorator.newCollection();
    
    int start=range.getStart().intValue();
    
    int end
      =range.getEnd()==null
      ?sourceDecorator.size(sourceList)
      :range.isInclusive()
        ?range.getEnd().intValue()+1
        :range.getEnd().intValue();
    
    end=Math.min(end,sourceDecorator.size(sourceList));
    
    for (int i=start;i<end;i++)
    { 
      targetList
        =sourceDecorator.add(targetList,sourceDecorator.get(sourceList,i));
    }
    
    return targetList;
  }

  @Override
  protected boolean store(
    C val)
    throws AccessException
  { return false;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }

}
