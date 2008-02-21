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

import spiralcraft.lang.spi.AbstractChannel;

import spiralcraft.data.Type;


/**
 * A spiralcraft.lang binding for Data, which uses the a Type as the 
 *   model for binding expressions.
 */
@SuppressWarnings("unchecked") // Haven't genericized the data package yet
public class DataBinding<T>
  extends AbstractChannel<T>
{
 
  private Channel<T> source;
  
  public DataBinding(Type type,Channel source,boolean isStatic)
    throws BindException
  { 
    super(DataReflector.<T>getInstance(type),isStatic);
    this.source=source;
  }

  @Override
  protected T retrieve()
  {
    // TODO Auto-generated method stub
    return source.get();
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  {
    // TODO Auto-generated method stub
    return source.set(val);
  }


}

