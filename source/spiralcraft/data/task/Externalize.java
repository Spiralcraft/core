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
package spiralcraft.data.task;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.TranslatorChannel;


/**
 * <p>Turns a native Java object into a DataComposite
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class Externalize<D,T>
  implements ChannelFactory<D,T>
{

  public static final <D,T> Channel<D> apply(Channel<T> source,Focus<?> focus)
    throws BindException
  { return new Externalize<D,T>().bindChannel(source,focus,null);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<D> bindChannel(
    final Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    final Type<T> dataType;
    
    if (source.getReflector() instanceof DataReflector)
    { dataType=(Type<T>) ((DataReflector<?>) source.getReflector()).getType();
    }
    else
    { 
      try
      { dataType=ReflectionType.canonicalType(source.getContentType());
      }
      catch (DataException x)
      { 
        throw new BindException
          ("Error resolve data Type for "+source.getContentType(),x);
      }
    }
    if (dataType==null)
    { 
      throw new BindException
        ("Error resolve data Type for "+source.getContentType());
    }
    try
    { return new TranslatorChannel(source,dataType.getExternalizer(),null);  
    }
    catch (DataException x)
    { throw new BindException("Error resolving externalizer",x);
    }
  }
}
