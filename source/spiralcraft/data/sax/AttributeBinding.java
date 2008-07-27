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
package spiralcraft.data.sax;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.util.StringConverter;

public class AttributeBinding<T>
{
  private Expression<T> target;
  private Channel<T> targetChannel;
  private StringConverter<T> converter;
  
  private String attribute;
    
  public void setTarget(Expression<T> expression)
  { this.target=expression;
  }

  public void setAttribute(String attribute)
  { this.attribute=attribute;
  }
  
  public Expression<?> getExpression()
  { return target;
  }

  public String getAttribute()
  { return attribute;
  }
  
  @SuppressWarnings("unchecked")
  public void bind(Focus<?> focus)
    throws BindException
  {
    targetChannel=focus.bind(target);
    
    if (converter==null)
    { 
      converter
        =(StringConverter<T>) StringConverter.getInstance
          (targetChannel.getContentType());
    }
  }
  
  public void set(String value)
  { targetChannel.set(converter.fromString(value));
  }
  
}
