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
import java.util.Map;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.Reflector;
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
  private StringConverter<Object> unitConverter;
  private Reflector<Object> unitReflector;
  
  private String name;
  
  private boolean assignment;
  
  private IterationDecorator<T,Object> decorator;
  private CollectionDecorator<T,Object> collectionDecorator;
  
  @SuppressWarnings("rawtypes")
  private Map<Class,StringConverter> converterMap;
  
    
  /**
   * Create a DictionaryBinding
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
  
  /**
   * <p>Create a DictionaryBinding with the specified name and target
   * </p>
   * 
   * @param name
   * @param target
   */
  public DictionaryBinding(String name,Expression<T> target)
  { 
    this.setName(name);
    this.setTarget(target);
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
   * Specify a Map to use during the binding process to resolve the
   *   appropriate non-default converter for the target data type.
   * 
   * @param converterMap
   */
  @SuppressWarnings("rawtypes")
  public void setConverterMap(Map<Class,StringConverter> converterMap)
  { this.converterMap=converterMap;
  }
  
  /**
   * <p>Provide a StringConverter to translate the bound type to and from
   *   a String.
   * </p>
   *    
   * @param converter
   */
  public void setConverter(StringConverter<T> converter)
  { this.converter=converter;
  }
  
  /**
   * <p>Provide a StringConverter to translate individual units of 
   *   multi-valued elements to and from Strings
   * </p>
   * 
   * @param unitConverter
   */
  public void setUnitConverter(StringConverter<Object> unitConverter)
  { this.unitConverter=unitConverter;
  }
  
  public Expression<T> getTarget()
  { return target;
  }

  public Channel<T> getTargetChannel()
  { return targetChannel;
  }
  
  public Reflector<Object> getUnitReflector()
  { return unitReflector;
  }
  
  public String getName()
  { return name;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void bind(Focus<?> focus)
    throws BindException
  {
    if (target==null)
    { throw new BindException("Target expression is null");
    }
    
    if (assignment)
    { 
      if (converter!=null || unitConverter!=null)
      { 
        throw new BindException
          ("In binding for "+name+": Cannot set "
          +" both a target assignment and a converter"
          );
      }
      
      targetChannel
        =focus.bind
          (Expression.<T>create
            ( ((AssignmentNode<T,T>)target.getRootNode()).getTarget())
          );

      
      converter
        =new ExpressionStringConverter
          (focus
          ,Expression.<T>create
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
      
      
      decorator
        =targetChannel.<IterationDecorator>decorate(IterationDecorator.class);
      
      collectionDecorator
        =targetChannel.<CollectionDecorator>decorate(CollectionDecorator.class);
      
      if (decorator!=null)
      {
        if (unitConverter==null)
        { unitConverter=createUnitConverter();
        }
        if (unitConverter==null)
        {
          unitConverter=
            decorator.getComponentReflector().getStringConverter();
        }
        if (unitConverter==null)
        { 
          unitConverter
            =StringConverter.getInstance
              (decorator.getComponentReflector().getContentType());
        }
        if (unitConverter==null)
        { decorator=null;
        }
        else
        { unitReflector=decorator.getComponentReflector();
        }
      }
        
      
      if (converter==null && unitConverter==null)
      {
        throw new BindException
          ("Could not obtain a StringConverter for "
            +targetChannel.getContentType()+": Reflector is "
            +targetChannel.getReflector()
          );
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private StringConverter<T> createConverter()
  { 
    StringConverter<T> converter=null;
    if (converterMap!=null)
    { 
      converter
        =converterMap.get(targetChannel.getReflector().getContentType());
    }
    if (converter==null)
    { converter=targetChannel.getReflector().getStringConverter();    
    }
    return converter;
  }
  
  @SuppressWarnings("unchecked")
  private StringConverter<Object> createUnitConverter()
  { 
    StringConverter<Object> converter=null;
    if (converterMap!=null)
    {
      converter
        =converterMap.get(decorator.getComponentReflector().getContentType());
    }
    if (converter==null)
    { converter=decorator.getComponentReflector().getStringConverter();
    }
    return converter;
  }
  
  public boolean isList()
  { return decorator!=null;
  }
  
  public boolean isWritableList()
  { return collectionDecorator!=null;
  }
  
  
  
  public void set(String value)
  { 
   
    try
    { 
      if (converter!=null)
      { targetChannel.set(converter.fromString(value));
      }
      else
      { arraySet(new String[] {value});
      }
    }
    catch (IllegalArgumentException x)
    { throw new AccessException("Error writing '"+value+"'",x);
    }
  }
  
  public String get()
  { 
    if (targetChannel==null)
    { throw new IllegalStateException("Not bound");
    }
    
    if (converter!=null)
    { return converter.toString(targetChannel.get());
    }
    else 
    { 
      String[] values=arrayGet();
      if (values!=null && values.length>0)
      { return values[0];
      }
      else
      { return null;
      }
    }
  }
  
  /**
   * Store a set of values for a multi-valued entry
   * 
   * @param values
   */
  public void arraySet(String[] values)
  { 
    if (collectionDecorator==null)
    { 
      if (values==null)
      { set(null);
      }
      else if (values.length>1)
      {
        throw new IllegalArgumentException
          ("Target does not accept multiple values");
      }
      else if (values.length==1)
      { set(values[0]);
      }
      else
      { set(null);
      }
    }
    else
    { 
      T collection=collectionDecorator.newCollection();
      for (String value:values)
      { 
        collection
          =collectionDecorator.add(collection, unitConverter.fromString(value));
      }
      targetChannel.set(collection);
    }
  }
  
  /**
   * Retrieve values, accounting for multi-valued entries
   * 
   * @return
   */
  public String[] arrayGet()
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
        { values.add(unitConverter.toString(val));
        }
      }
      return values.toArray(new String[values.size()]);
    }
  }
  
}
