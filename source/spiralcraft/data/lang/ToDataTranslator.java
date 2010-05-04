//
// Copyright (c) 2010 Michael Toth
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

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;

/**
 * <p>A Translator which calls the toData() and fromData() methods on 
 *   a dataType to externalize/internalize an object
 * </p>
 *   
 * 
 * @author mike
 *
 * @param <T>
 */
public class ToDataTranslator<T>
  implements Translator<DataComposite, T>
{

  
  private final Reflector<DataComposite> reflector;
  private final Type<T> type;
  
  public ToDataTranslator(Type<T> type)
    throws DataException
  { 
    this.type=type;
    try
    { 
      if (type.isAggregate())
      {
        reflector=AggregateReflector.getInstance(type);
      }
      else
      { reflector=TupleReflector.getInstance(type);
      }
      
      if (reflector==null)
      {
        throw new DataException
          ("Could not resolve reflector for DataComposite aspect of"
          +" type "+type.getURI()+"("+type.getNativeClass()+")"
          );
      }
    }
    catch (BindException x)
    { throw new DataException("Error resolving reflector for "+type.getURI(),x);
    }
  }
  
  @Override
  public Reflector<DataComposite> getReflector()
  { return reflector;
  }

  @Override
  public DataComposite translateForGet(
    T source,
    Channel<?>[] modifiers)
  { 
    try
    { return type.toData(source);
    }
    catch (DataException x)
    { throw new AccessException("Error externalizing "+source);
    }
  }

  @Override
  public T translateForSet(
    DataComposite source,
    Channel<?>[] modifiers)
  { 
    try
    { return type.fromData(source,null);
    }
    catch (DataException x)
    { throw new AccessException("Error internalizing "+source);
    }
  } 
  
  @Override
  public String toString()
  { return super.toString()+"("+reflector.toString()+")";
  }
}
