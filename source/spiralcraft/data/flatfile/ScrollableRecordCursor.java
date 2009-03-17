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

public class ScrollableRecordCursor
  extends RecordCursor
  implements ScrollableCursor<Tuple>
{
  
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
  public void dataMoveAfterLast()
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
  public void dataMoveBeforeFirst()
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
  public boolean dataMoveFirst()
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
  public boolean dataMoveLast()
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
  public boolean dataPrevious()
    throws DataException
  {
    try
    { return iterator.previous();
    }
    catch (IOException x)
    { throw new DataException("Error moving cursor",x);
    }
    finally
    { update();
    }
  }

}
