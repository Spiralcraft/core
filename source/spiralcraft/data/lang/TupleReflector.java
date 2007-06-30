//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.data.lang;

import spiralcraft.lang.spi.Binding;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Field;
import spiralcraft.data.Type;
import spiralcraft.data.Scheme;

import java.util.HashMap;

/**
 * Maps a Scheme into the spiralcraft.lang binding mechanism
 *
 * This allows object models of Tuples (defined by Schemes) to be
 *   fully utilized by the language facilities.
 */
public class TupleReflector<T extends Tuple>
  extends DataReflector<T>
{
  private final FieldSet fieldSet;
  
  private final HashMap<String,FieldTranslator> fieldTranslators
    =new HashMap<String,FieldTranslator>();
  
  private final Class<T> contentType;

  @SuppressWarnings("unchecked") // We only create Reflector with erased type
  public synchronized static final 
    <T extends Tuple> TupleReflector<T> getInstance(FieldSet fieldSet)
    throws BindException
  { 
    if (fieldSet instanceof Scheme)
    { return (TupleReflector) DataReflector.getInstance( ((Scheme) fieldSet).getType());
    }
    else
    { return new TupleReflector(fieldSet,Tuple.class);
    }
  }
  

  TupleReflector(Type type,Class<T> contentType)
    throws BindException
  { 
    super(type);
    this.fieldSet=type.getScheme();
    this.contentType=contentType;
    for (Field field : fieldSet.fieldIterable())
    { fieldTranslators.put(field.getName(),new FieldTranslator(field));
    }
  }
  
  TupleReflector(FieldSet fieldSet,Class<T> contentType)
    throws BindException
  { 
    super(null);
    this.fieldSet=fieldSet;
    this.contentType=contentType;
    for (Field field : fieldSet.fieldIterable())
    { fieldTranslators.put(field.getName(),new FieldTranslator(field));
    }
  }

  public FieldSet getFieldSet()
  { return fieldSet;
  }

  /**
   * Resolve a Binding that provides access to a member of a Tuple given a 
   *   source that provides Tuples.
   */
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  public synchronized Binding resolve
    (Binding source
    ,Focus focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  {
    FieldTranslator translator=fieldTranslators.get(name);
    
    if (translator!=null)
    {
      Binding binding=source.getCache().get(translator);
      if (binding==null)
      { 
        binding=new FieldBinding(source,translator);
        source.getCache().put(translator,binding);
      }
      return binding;      
    }
    
    return null;
  }

  public Decorator<T> decorate(Binding binding,Class decoratorInterface)
  { 
    // This depends on a system for registering and mapping decorators
    //   to Tuple constructs.
    return null;
  }
  
  public Class<T> getContentType()
  { return contentType;
  }
  
}
