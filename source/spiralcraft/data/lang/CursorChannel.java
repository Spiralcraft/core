//
// Copyright (c) 2009  Michael Toth
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
package spiralcraft.data.lang;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeMismatchException;
import spiralcraft.data.access.Cursor;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.SourcedChannel;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CursorChannel<T>
  extends SourcedChannel<Cursor,T>
{
    
  protected final Type<T> type;
    
  public CursorChannel(Type<T> type,Channel<Cursor> cursorChannel)
    throws BindException
  { 
    super(DataReflector.<T>getInstance(type),cursorChannel);
    this.type=type;
  }
    
  @Override
  public boolean isWritable()
  { return false;
  }

  public void setCursor(Cursor cursor)
    throws DataException
  { 
    if (type.isAssignableFrom(cursor.getFieldSet().getType()))
    { source.set(cursor);
    }
    else
    { throw new TypeMismatchException
        ("Incompatible cursor",type,cursor.getFieldSet().getType());
    }
  }
    
  public Cursor getCursor()
  { return source.get();
  }
      
  @Override
  protected T retrieve()
    throws AccessException
  {
    try
    {
      Cursor cursor=source.get();
      if (cursor!=null)
      { return (T) cursor.getTuple();
      }
      else
      { return null;
      }
    }
    catch (DataException x)
    { 
      throw new AccessException(x.toString(),x);
    }
    catch (RuntimeException x)
    { 
      x.printStackTrace();
      throw x;
    }
  }

  @Override
  protected boolean store(T val)
    throws AccessException
  { 
      
    throw new AccessException
      ("Can't store anything in a cursor."
      );
    
  }
    
   
}
