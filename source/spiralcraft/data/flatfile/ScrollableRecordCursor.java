//
// Copyright (c) 2009,2009 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.flatfile;

import java.io.IOException;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.io.record.ScrollableRecordIterator;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

public class ScrollableRecordCursor
  extends RecordCursor
  implements ScrollableCursor<Tuple>
{
  private static final ClassLog log
    =ClassLog.getInstance(ScrollableRecordCursor.class);  
  
  private ScrollableRecordIterator iterator;

  public ScrollableRecordCursor
    (ScrollableRecordIterator iterator
    ,RecordFormat format
    )
  { 
    super(iterator,format);
    this.iterator=iterator;
  }
  
  @Override
  public void moveAfterLast()
    throws DataException
  {
    try
    { 
      iterator.bottom();
      update();
    }
    catch (IOException x)
    { throw new DataException("Error moving cursor",x);
    }
    
  }

  @Override
  public void moveBeforeFirst()
    throws DataException
  {
    try
    { 
      iterator.top();
      update();
    }
    catch (IOException x)
    { throw new DataException("Error moving cursor",x);
    }    
  }

  @Override
  public boolean moveFirst()
    throws DataException
  {
    try
    { return iterator.seek(0);
    }
    catch (IOException x)
    { throw new DataException("Error moving cursor",x);
    }
    finally
    { update();
    }
    
  }

  @Override
  public boolean moveLast()
    throws DataException
  {
    try
    { 
      iterator.bottom();
      return iterator.previous();
    }
    catch (IOException x)
    { throw new DataException("Error moving cursor",x);
    }
    finally
    { update();
    }
  }

  @Override
  public boolean previous()
    throws DataException
  {
    try
    {
      while (true)
      {
        boolean ret=false;
        try
        {
          ret=iterator.previous();
          update();
          return ret;
        }
        catch (ParseException x)
        {
          if (errorTolerant)
          { 
            log.log(Level.INFO,"Skipping unreadable record #"
              +iterator.getRecordPointer(),x);
            if (!ret)
            { return false;
            }
          }
          else
          { throw x;
          }
        }
      }
    }
    catch (IOException x)
    { throw new DataException("Error moving cursor",x);
    }
      
  }

}
