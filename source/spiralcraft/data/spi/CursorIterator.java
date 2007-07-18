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
    { this.hasNext=cursor.dataNext();
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error iterating through cursor:"+x,x);
    }
    
  }
  
  public boolean hasNext()
  { return hasNext;
  }
  
  public T next()
  {
    if (!hasNext)
    { return null;
    }
    
    try
    { 
      T ret=cursor.dataGetTuple();
      hasNext=cursor.dataNext();
      return ret;
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error iterating through cursor:"+x,x);
    }
  }
  
  public void remove()
  { 
    throw new UnsupportedOperationException
      ("CursorIterator does not support removal");
  }

  
}
