//
// Copyright (c) 2013 Michael Toth
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
package spiralcraft.data.access.cache;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.access.kit.SerialCursorAdapter;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

public class SerialCacheCursor
  extends SerialCursorAdapter<Tuple>
{

  private final EntityCache cache;
  
  public SerialCacheCursor(SerialCursor<?> source,EntityCache cache)
  { 
    super(source);
    this.cache=cache;
  }


  @Override
  public Tuple getTuple()
    throws DataException
  {
    // Canonicalize here
    return cache.cache(source.getTuple());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Channel<Tuple> bind()
    throws BindException
  { return (Channel<Tuple>) source.bind();
  }

  
}
