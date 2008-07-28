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
package spiralcraft.data.spi;

import spiralcraft.data.DataException;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Tuple;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.util.KeyFunction;

public class DataKeyFunction<T>
  implements KeyFunction<KeyTuple,T>
{
  
  private ThreadLocalChannel<T> valueChannel;
  private Channel<? extends Tuple> projectionChannel;
  
  public DataKeyFunction(Projection projection)
    throws BindException
  { 
    Focus<T> focus=new SimpleFocus<T>(valueChannel);
    projectionChannel=projection.bind(focus);
  }
  
  @Override
  public KeyTuple key(T value)
  {
    valueChannel.push(value);
    try
    { return new KeyTuple(projectionChannel.get()); 
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error generating retrieving key",x);
    }
    finally
    { valueChannel.pop();
    }
  }


}
