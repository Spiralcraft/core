//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.lang;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;
import spiralcraft.util.StringConverter;

/**
 * <p>Associates a loosely bound name, such as used to reference 
 *   objects in a map, with an Expression bound to
 *   a data model.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class DictionaryBinding<T>
{
  private Expression<T> target;
  private Channel<T> targetChannel;
  private StringConverter<T> converter;
  
  private String name;
    
  /**
   * Create an AttributeBinding
   */
  public DictionaryBinding()
  {
  }
  
  /**
   * <p>Create an DictionaryBinding using the shorthand method. The syntax
   *   of the shorthand string is:
   * </p>
   * 
   *   <code><i>name</i> ( "=" <i>targetExpression</i> ) </code>
   *  
   * <p>If the targetExpression is excluded, it will be set to the same as
   *   the name, which relies on an identically named field in
   *   the Type associated with the bound scope. 
   * </p> 
   * 
   * @param shortHand
   */
  public DictionaryBinding(String shortHand)
    throws ParseException
  {
    int eqPos=shortHand.indexOf("=");
    if (eqPos<0)
    { 
      setName(shortHand);
      setTarget(Expression.<T>parse(shortHand));
    }
    else
    {
      setName(shortHand.substring(0,eqPos));
      setTarget(Expression.<T>parse(shortHand.substring(eqPos+1)));
    }
  }
  
  public void setTarget(Expression<T> expression)
  { this.target=expression;
  }

  public void setName(String name)
  { this.name=name;
  }
  
  public Expression<?> getTarget()
  { return target;
  }

  public String getName()
  { return name;
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
  
  public String get()
  { return converter.toString(targetChannel.get());
  }
  
}
