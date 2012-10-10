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
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ReverseTranslator;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;


/**
 * <p>Turns a DataComposite into a Java object
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class Internalize<T,D>
  implements ChannelFactory<T,D>
{

  public static final <T,D> Channel<T> apply(Channel<D> source,Focus<?> focus)
    throws BindException
  { return new Internalize<T,D>().bindChannel(source,focus,null);
  }
  
  private Type<T> type;
  
  public void setType(Type<T> type)
  { this.type=type;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Channel<T> bindChannel(
    final Channel<D> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    Type<T> dataType=type;
    
    if (dataType==null && source.getReflector() instanceof DataReflector)
    { dataType=(Type<T>) ((DataReflector<?>) source.getReflector()).getType();
    }
    
    if (dataType==null)
    {
      throw new BindException
        ("Not enough type information to internalize source: "
        +source.getReflector()
        );
    }
    
    try
    {     
      return new TranslatorChannel
        (source
        ,new ReverseTranslator<T,D>
          (BeanReflector.<T>getInstance(dataType.getNativeClass())
          ,(Translator<D,T>) dataType.getExternalizer()
          )
        ,null
        );
    }
    catch (DataException x)
    { throw new BindException("Error resolving externalizer",x);
    }
    
  }
}
