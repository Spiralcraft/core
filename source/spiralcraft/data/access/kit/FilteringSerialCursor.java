//
// Copyright (c) 2014 Michael Toth
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
package spiralcraft.data.access.kit;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.lang.util.ContextualFilter;

public class FilteringSerialCursor<T extends Tuple>
  extends SerialCursorAdapter<T>
{
  private final ContextualFilter<T> filter;
  
  public FilteringSerialCursor(SerialCursor<?> source,ContextualFilter<T> filter)
  { 
    super(source);
    this.filter=filter;
  }  
  
  @Override
  public boolean next()
    throws DataException
  {
    while (true)
    {
      if (!source.next())
      { return false;
      }
      if (filter.eval(getTuple()))
      { return true;
      }
    }
  }
  
  
}