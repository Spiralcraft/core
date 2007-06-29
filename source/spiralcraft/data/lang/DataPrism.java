//
// Copyright (c) 1998,2005 Michael Toth
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

import java.util.HashMap;

import spiralcraft.lang.BindException;
import spiralcraft.lang.optics.Prism;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Type;

/**
 * Abstract base class maps a DataComposite into the spiralcraft.lang namespace
 *
 * @author mike
 *
 * @param <T> The type of DataComposite we are mapping
 */
public abstract class DataPrism<T extends DataComposite>
  implements Prism<T>
{
  // XXX Use weak map
  private static final HashMap<Type,DataPrism> SINGLETONS
    =new HashMap<Type,DataPrism>();
  
  protected final Type type;
  
  @SuppressWarnings("unchecked") // We only create Prism with erased type
  public synchronized static final 
    <T extends DataComposite> DataPrism<T> getInstance(Type type)
    throws BindException
  { 
    DataPrism prism=SINGLETONS.get(type);
    if (prism==null)
    {
      if (type.isAggregate())
      { prism=new AggregatePrism(type,Aggregate.class);
      }
      else
      { prism=new TuplePrism(type,Tuple.class);
      }
      SINGLETONS.put(type,prism);
    }
    return prism;
  }
  
  public DataPrism(Type type)
  { this.type=type;
  }
  
  public Type getType()
  { return type;
  }
}
