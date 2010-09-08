package spiralcraft.data.spi;

import java.util.Iterator;

import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.RuntimeDataException;

import spiralcraft.data.access.SerialCursor;

public class CursorIterator<T extends Tuple>
  implements Iterator<T>
{
  private final SerialCursor<T> cursor;
  private boolean hasNext;
  
  public CursorIterator(SerialCursor<T> cursor)
  { 
    this.cursor=cursor;
    try
    { this.hasNext=cursor.next();
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error iterating through cursor:"+x,x);
    }
    
  }
  
  @Override
  public boolean hasNext()
  { return hasNext;
  }
  
  @Override
  public T next()
  {
    if (!hasNext)
    { return null;
    }
    
    try
    { 
      T ret=cursor.getTuple();
      hasNext=cursor.next();
      return ret;
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error iterating through cursor:"+x,x);
    }
  }
  
  @Override
  public void remove()
  { 
    throw new UnsupportedOperationException
      ("CursorIterator does not support removal");
  }

  
}
