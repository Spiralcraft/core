package spiralcraft.data.xml;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Scan;
import spiralcraft.data.spi.ListCursor;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;

class BoundScan
  extends BoundQuery<Scan,Tuple>
{
    
  private final boolean debugTrace;
  private final XmlQueryable source;
    
  public BoundScan(Scan query,Focus<?> paramFocus,XmlQueryable source)
  { 
    super(query,paramFocus);
    this.source=source;
    debugTrace=debugLevel.canLog(Level.TRACE);
  }
  
  @Override
  public SerialCursor<Tuple> doExecute() throws DataException
  { 
    if (debugTrace)
    { log.trace(toString()+": Executing BoundScan of "+getType().getURI());
    }
    return source.getCursor();
  } 
  
  class BoundScanScrollableCursor
    extends BoundQueryScrollableCursor
  {
    private final ScrollableCursor<Tuple> cursor;
   
    @Override
    public Identifier getRelationId()
    { return null;
    }
    
    public BoundScanScrollableCursor(Aggregate<Tuple> aggregate)
    { 

      cursor=new ListCursor<Tuple>(aggregate);
    }
    
    @Override
    public boolean next()
      throws DataException
    {
      if (cursor.next())
      { 
        dataAvailable(cursor.getTuple());
        return true;
      }
      else
      { return false;
      }
    }

    @Override
    public void moveAfterLast()
      throws DataException
    { cursor.moveAfterLast();
    }

    @Override
    public void moveBeforeFirst()
      throws DataException
    { cursor.moveBeforeFirst();
    }

    @Override
    public boolean moveFirst()
      throws DataException
    { 
      if (cursor.moveFirst())
      { 
        dataAvailable(cursor.getTuple());
        return true;
      }
      else
      { return false;
      }
    }

    @Override
    public boolean moveLast()
      throws DataException
    { 
      if (cursor.moveLast())
      { 
        dataAvailable(cursor.getTuple());
        return true;
      }
      else
      { return false;
      }
    }

    @Override
    public boolean previous()
      throws DataException
    { 
      if (cursor.previous())
      { 
        dataAvailable(cursor.getTuple());
        return true;
      }
      else
      { return false;
      }
      
    }
    
    @Override
    public void close()
      throws DataException
    { cursor.close();
    }
  }
}
