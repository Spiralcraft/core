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
package spiralcraft.lang.util;

import java.util.ArrayList;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.parser.AssignmentNode;

import spiralcraft.util.string.StringConverter;

/**
 * <p>Binds a textual value mapped to a name (eg. as
 *   in a String dictionary) to a spiralcraft.lang expression of an arbitrary
 *   type.
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
  
  private boolean assignment;
  
  private IterationDecorator<T,Object> decorator;
    
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
      setName(shortHand.trim());
      setTarget(Expression.<T>parse(shortHand));
    }
    else
    {
      setName(shortHand.substring(0,eqPos).trim());
      setTarget(Expression.<T>parse(shortHand.substring(eqPos+1)));
    }
  }
  
  public void setTarget(Expression<T> expression)
  { 
    this.target=expression;
    if (this.target.getRootNode() instanceof AssignmentNode<?,?>)
    { assignment=true;
    }
  }

  public void setName(String name)
  { this.name=name;
  }

  /**
   * <p>Provide a StringConverter to translate the bound type to and from
   *   a String
   * </p>
   *    
   * @param converter
   */
  public void setConverter(StringConverter<T> converter)
  { this.converter=converter;
  }
  
  public Expression<T> getTarget()
  { return target;
  }

  public Channel<T> getTargetChannel()
  { return targetChannel;
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
    
    if (assignment)
    { 
      if (converter!=null)
      { 
        throw new BindException
          ("In binding for "+name+": Cannot set "
          +" both a target assignment and a converter"
          );
      }
      
      targetChannel
        =focus.bind
          (new Expression
            ( ((AssignmentNode<T,T>)target.getRootNode()).getTarget())
          );

      
      converter
        =new ExpressionStringConverter
          (focus
          ,new Expression
            ( ((AssignmentNode<T,T>) target.getRootNode()).getSource()
            )
          ,targetChannel.getReflector()
          );
    }
    else
    {
      targetChannel=focus.bind(target);
    
      if (converter==null)
      { converter=createConverter();
      }
      if (converter==null)
      { converter=StringConverter.getInstance(targetChannel.getContentType());
      }
      if (converter==null)
      { 
        decorator
          =targetChannel.<IterationDecorator>decorate(IterationDecorator.class);
        if (decorator!=null)
        {
        }
        converter=(StringConverter<T>) 
          decorator.getComponentReflector().getStringConverter();
        
      }
      if (converter==null)
      {
        throw new BindException
          ("Could not obtain a StringConverter for "
            +targetChannel.getContentType()+": Reflector is "
            +targetChannel.getReflector()
          );
      }
    }
  }
  
  protected StringConverter<T> createConverter()
  { return targetChannel.getReflector().getStringConverter();    
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
  { 
    if (decorator==null)
    { return converter.toString(targetChannel.get());
    }
    else
    { 
      String[] values=getValues();
      if (values!=null && values.length>0)
      { return values[0];
      }
      else
      { return null;
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  public String[] getValues()
  {
    if (decorator==null)
    { 
      final String str=get();
      if (str!=null)
      { return new String[] {str};
      }
      else
      { return null;
      }
    }
    else
    {
      ArrayList<String> values=new ArrayList<String>();
      for (Object val : decorator)
      { 
        if (val!=null)
        { values.add(converter.toString((T) val));
        }
      }
      return values.toArray(new String[values.size()]);
    }
  }
  
}
