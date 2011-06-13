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
package spiralcraft.lang.functions;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.util.string.StringConverter;

/**
 * A reversible function which converts objects to strings based on a
 *   StringConverter
 * 
 * @author mike
 *
 * @param <T>
 */
public class ToString<T>
  implements ChannelFactory<String,T>

{

  private StringConverter<T> converter;
  
  public ToString()
  {
  }
  
  public ToString(StringConverter<T> converter)
  { this.converter=converter;
  }
  
  @Override
  public Channel<String> bindChannel(
    Channel<T> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  {
    return new ToStringChannel(source);
  }
  
  public class ToStringChannel
    extends SourcedChannel<T,String>
  {

    private StringConverter<T> converter=ToString.this.converter;
    
    public ToStringChannel(Channel<T> source) 
       throws BindException
    { 
      super(source);
      if (converter==null)
      { converter=source.getReflector().getStringConverter();
      }
      if (converter==null)
      { 
        throw new BindException
          ("No StringConverter for "+source.getReflector().getTypeURI());
      }
    }
    
    @Override
    protected String retrieve()
    { return converter.toString(source.get());
    }

    @Override
    protected boolean store(
      String val)
      throws AccessException
    { return source.set(converter.fromString(val));
    }
  }

  
}
