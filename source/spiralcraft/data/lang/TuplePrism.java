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

import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.Binding;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Field;

import java.util.HashMap;

/**
 * Maps a Scheme into the spiralcraft.lang binding mechanism
 *
 * This allows object models of Tuples (defined by Schemes) to be
 *   fully utilized by the language facilities.
 */
public class TuplePrism<T extends Tuple>
  implements Prism<T>
{
  private static final HashMap<FieldSet,TuplePrism> SINGLETONS
    =new HashMap<FieldSet,TuplePrism>();
  
  private final FieldSet fieldSet;
  
  private final HashMap<String,FieldLense> fieldLenses
    =new HashMap<String,FieldLense>();
  
  private final Class<T> contentType;

  @SuppressWarnings("unchecked") // We only create Prism with erased type
  public synchronized static final 
    <T extends Tuple> TuplePrism<T> getInstance(FieldSet fieldSet)
    throws BindException
  { 
    TuplePrism prism=SINGLETONS.get(fieldSet);
    if (prism==null)
    {
      prism=new TuplePrism<Tuple>(fieldSet,Tuple.class);
      SINGLETONS.put(fieldSet,prism);
    }
    return prism;
  }
  
  TuplePrism(FieldSet fieldSet,Class<T> contentType)
    throws BindException
  { 
    this.fieldSet=fieldSet;
    this.contentType=contentType;
    for (Field field : fieldSet.fieldIterable())
    { fieldLenses.put(field.getName(),new FieldLense(field));
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
    FieldLense lense=fieldLenses.get(name);
    
    if (lense!=null)
    {
      Binding binding=source.getCache().get(lense);
      if (binding==null)
      { 
        binding=new FieldBinding(source,lense);
        source.getCache().put(lense,binding);
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
