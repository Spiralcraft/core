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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;
import spiralcraft.util.string.StringConverter;

/**
 * <p>Associates a loosely bound name, such as an XML attribute name or 
 *   a variable in a URLEncoded query string with an Expression bound to
 *   a data model.
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
   *   the Type associated with the bound scope. 
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
      setAttribute(shortHand.trim());
      setTarget(Expression.<T>parse(shortHand));
    }
    else
    {
      setAttribute(shortHand.substring(0,eqPos).trim());
      setTarget(Expression.<T>parse(shortHand.substring(eqPos+1)));
    }
  }
  
  public void setTarget(Expression<T> expression)
  { this.target=expression;
  }

  public void setAttribute(String attribute)
  { this.attribute=attribute;
  }
  
  
  /**
   * <p>The expression referencing the Channel to be updated.
   * </p>
   * 
   * @return The attribute name
   */
  public Expression<?> getTarget()
  { return target;
  }
  
  /**
   * <p>The name of the XML attribute
   * </p>
   * 
   * @return The attribute name
   */
  public String getAttribute()
  { return attribute;
  }
  
  /**
   * 
   * <p>Specify the StringConverter that manages this attribute's value
   * </p>
   * 
   * @param converter The StringConverter that will be used to convert
   *   the attribute value text to the target's type, if the default
   *   converter should be overridden. 
   */
  public void setConverter(StringConverter<T> converter)
  { this.converter=converter;
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
  { 
    try
    { targetChannel.set(converter.fromString(value));
    }
    catch (IllegalArgumentException x)
    { throw new AccessException("Error reading '"+value+"'",x);
    }
  }
  
  public String get()
  { return converter.toString(targetChannel.get());
  }
  
}
