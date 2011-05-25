//
// Copyright (c) 1998,2010 Michael Toth
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
package spiralcraft.lang.util;

import java.io.IOException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.text.Renderer;
import spiralcraft.util.string.StringConverter;

/**
 * <p>Renders the output of an Expression, using a StringConverter to
 *   turn the result into text
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class ExpressionRenderer<T>
  implements Renderer,Contextual
{

  private StringConverter<T> converter;
  private Expression<T> x;
  private Channel<T> channel;
  
  public ExpressionRenderer()
  {
  }
  
  public ExpressionRenderer(Expression<T> x)
  { this.x=x;
  }
  
  public void setX(Expression<T> x)
  { this.x=x;
  }
  
  public void setConverter(StringConverter<T> converter)
  { this.converter=converter;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    if (x!=null)
    { channel=focusChain.bind(x);
    }
    else
    { channel=(Channel<T>) focusChain.getSubject();
    }
    if (converter==null)
    { converter=channel.getReflector().getStringConverter();
    }
    return focusChain;
  }

  @Override
  public void render(Appendable out)
    throws IOException
  { 
    if (converter!=null)
    { out.append(converter.toString(channel.get()));
    }
    else
    {
      T val=channel.get();
      if (val!=null)
      { out.append(val.toString());
      }
    }
  }

}
