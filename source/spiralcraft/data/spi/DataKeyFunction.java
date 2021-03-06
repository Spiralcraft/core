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
import spiralcraft.data.Type;
import spiralcraft.data.lang.TupleReflector;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.util.KeyFunction;

public class DataKeyFunction<K extends KeyTuple,T>
  implements KeyFunction<K,T>
{
  
  private final ThreadLocalChannel<T> valueChannel;
  private final Channel<? extends Tuple> projectionChannel;
  
  @SuppressWarnings("unchecked")
  public DataKeyFunction(Projection<T> projection)
    throws BindException
  { 
    this((Type<T>) projection.getSource().getType(),projection);
  }

  private DataKeyFunction
    (Reflector<T> valueReflector,Projection<T> projection)
    throws BindException
  {
    valueChannel=new ThreadLocalChannel<T>(valueReflector);
    Focus<T> focus=new SimpleFocus<T>(valueChannel);
    
    // Indexes are a function of the target type and cannot be contextual
    valueChannel.setContext(new SimpleFocus<Void>(null));
    
    projectionChannel=projection.bindChannel(valueChannel,focus,null);

  }
  
  private DataKeyFunction(Type<T> masterType,Projection<T> projection)
    throws BindException
  { this(TupleReflector.<T>getInstance(masterType),projection);
  }
  
  @Override
  public K key(T value)
  {
    valueChannel.push(value);
    try
    { return createKeyTuple(projectionChannel.get());
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error generating retrieving key",x);
    }
    finally
    { valueChannel.pop();
    }
  }
  
  @SuppressWarnings("unchecked")
  protected K createKeyTuple(Tuple projectionValue)
    throws DataException
  { return (K) new KeyTuple(projectionChannel.get());
  }


}
