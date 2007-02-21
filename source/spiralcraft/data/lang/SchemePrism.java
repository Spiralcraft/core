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

import spiralcraft.data.Scheme;
import spiralcraft.data.Tuple;
import spiralcraft.data.Field;

import java.util.HashMap;

/**
 * Maps a Scheme into the spiralcraft.lang binding mechanism
 *
 * This allows object models of Tuples (defined by Schemes) to be
 *   fully utilized by the language facilities.
 */
public class SchemePrism
  implements Prism
{
  private static final HashMap<Scheme,SchemePrism> SINGLETONS
    =new HashMap<Scheme,SchemePrism>();
  
  private final Scheme scheme;
  private final HashMap<String,FieldLense> fieldLenses
    =new HashMap<String,FieldLense>();

  public synchronized static final SchemePrism getInstance(Scheme scheme)
    throws BindException
  { 
    SchemePrism prism=SINGLETONS.get(scheme);
    if (prism==null)
    {
      prism=new SchemePrism(scheme);
      SINGLETONS.put(scheme,prism);
    }
    return prism;
  }
  
  SchemePrism(Scheme scheme)
    throws BindException
  { 
    this.scheme=scheme;
    for (Field field : scheme.fieldIterable())
    { fieldLenses.put(field.getName(),new FieldLense(field));
    }
  }

  public Scheme getScheme()
  { return scheme;
  }

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

  public Decorator decorate(Binding binding,Class decoratorInterface)
  { 
    // This depends on a system for registering and mapping decorators
    //   to Tuple constructs.
    return null;
  }
  
  public Class<?> getContentType()
  { return Tuple.class;
  }
  
}
