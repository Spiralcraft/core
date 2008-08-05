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
import spiralcraft.lang.ParseException;
import spiralcraft.util.StringConverter;

/**
 * <p>Directs that the value of an XML attribute in the incoming data stream be
 *   assigned to an element in the data model.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class AttributeBinding<T>
{
  private Expression<T> target;
  private Channel<T> targetChannel;
  private StringConverter<T> converter;
  
  private String attribute;
    
  /**
   * Create an AttributeBinding
   */
  public AttributeBinding()
  {
  }
  
  /**
   * <p>Create an AttributeBinding using the shorthand method. The syntax
   *   of the shorthand string is:
   * </p>
   * 
   *   <code><i>attributeName</i> ( "=" <i>targetExpression</i> ) </code>
   *  
   * <p>If the targetExpression is excluded, it will be set to the same as
   *   the attribute name, which relies on an identically named field in
   *   the Type associated with the current FrameHandler.
   * </p> 
   * 
   * @param shortHand
   */
  public AttributeBinding(String shortHand)
    throws ParseException
  {
    int eqPos=shortHand.indexOf("=");
    if (eqPos<0)
    { 
      setAttribute(shortHand);
      setTarget(Expression.<T>parse(shortHand));
    }
    else
    {
      setAttribute(shortHand.substring(0,eqPos));
      setTarget(Expression.<T>parse(shortHand.substring(eqPos+1)));
    }
  }
  
  public void setTarget(Expression<T> expression)
  { this.target=expression;
  }

  public void setAttribute(String attribute)
  { this.attribute=attribute;
  }
  
  public Expression<?> getTarget()
  { return target;
  }

  public String getAttribute()
  { return attribute;
  }
  
  @SuppressWarnings("unchecked")
  public void bind(Focus<?> focus)
    throws BindException
  {
    if (target==null)
    { throw new BindException("Target expression is null");
    }
    
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
