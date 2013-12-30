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
package spiralcraft.data.access.kit;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

public class SerialCursorAdapter<T extends Tuple>
  implements SerialCursor<T>
{

  protected SerialCursor<?> source;
  
  protected SerialCursorAdapter(SerialCursor<?> source)
  {  this.source=source;
  }

  @Override
  public Type<?> getResultType()
  { return source.getResultType();
  }

  @Override
  public Identifier getRelationId()
  { return source.getRelationId();
  }

  @Override
  public FieldSet getFieldSet()
  { return source.getFieldSet();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getTuple()
    throws DataException
  { return (T) source.getTuple();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Channel<T> bind()
    throws BindException
  { return (Channel<T>) source.bind();
  }

  @Override
  public void close()
    throws DataException
  { source.close();
  }

  @Override
  public boolean next()
    throws DataException
  { return source.next();
  }
  
}
