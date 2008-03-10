package spiralcraft.data.query;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.session.BufferChannel;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.AbstractChannel;

/**
 * Provides access to the results of a query in the form of an Aggregate
 * 
 * @author mike
 *
 */
@SuppressWarnings("unchecked")
public class QueryChannel
  extends AbstractChannel<Aggregate>
{
  
  private BoundQuery<Query,Tuple> query;
  
  public QueryChannel(BoundQuery<Query,Tuple> query)
    throws BindException
  { 
    super
      ( 
        DataReflector.<Aggregate>getInstance
          (Type.getAggregateType(query.getType())
          )
      );
    this.query=query;
  }
  
  @Override
  protected Aggregate<?> retrieve()
  {
    try
    { 
      
      CursorAggregate aggregate=new CursorAggregate(query.execute());
      
      return aggregate;
      
    }
    catch (DataException x)
    { throw new AccessException("Error performing query",x);
    }
  }

  /**
   * Convenience method to buffer 
   * 
   * @param focus
   * @return
   * @throws BindException
   * @throws DataException
   */
  public BufferChannel buffer(Focus<?> focus)
    throws BindException,DataException
  { 
    return new BufferChannel
      (Type.getBufferType(query.getType()), this,focus);
  }  
  
  @Override
  public boolean isWritable()
  { return false;
  }
  
  @Override
  protected boolean store(
    Aggregate val)
    throws AccessException
  { return false;
  }

}
