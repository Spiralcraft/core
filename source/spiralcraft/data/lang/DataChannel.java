//
// Copyright (c) 1998,2007 Michael Toth
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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Focus;

import spiralcraft.lang.spi.AbstractChannel;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.BufferChannel;


/**
 * A spiralcraft.lang channel for Data, which uses the a Type as the 
 *   model for binding expressions.
 */
public class DataChannel<T extends DataComposite>
  extends AbstractChannel<T>
{
 
  private Channel<T> source;
  
  public DataChannel(Type<?> type,Channel<T> source,boolean isStatic)
    throws BindException
  { 
    super(DataReflector.<T>getInstance(type),isStatic);
    this.source=source;
  }

  @Override
  protected T retrieve()
  { return source.get();
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { return source.set(val);
  }
  
  @Override
  public boolean isWritable()
  { return source.isWritable();
  }
  
  
  /**
   * Convenience method to buffer 
   * 
   * @param focus
   * @return
   * @throws BindException
   * @throws DataException
   */
  @SuppressWarnings("unchecked")
  public BufferChannel buffer(Focus<?> focus)
    throws BindException,DataException
  { 
    return new BufferChannel
      (Type.<Buffer>getBufferType
        ((Type<Buffer>)((DataReflector<T>) getReflector()).getType())
      ,this
      ,focus
      );
  } 

}

