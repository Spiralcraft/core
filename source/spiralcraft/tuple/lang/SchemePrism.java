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
package spiralcraft.tuple.lang;

import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.Binding;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Field;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Maps a Scheme into the spiralcraft.lang binding mechanism
 *
 * This allows object models of Tuples (defined by Schemes) to be
 *   fully utilized by the language facilities.
 */
public class SchemePrism
  implements Prism
{
  private static final HashMap<Scheme,Prism> _SINGLETONS
    =new HashMap<Scheme,Prism>();
  
  private final Scheme _scheme;
  private final HashMap<String,Field> _fields
    =new HashMap<String,Field>();
  private HashMap<String,FieldLense> _fieldLenses;

  public synchronized static final SchemePrism getInstance(Scheme scheme)
  { 
    SchemePrism prism=(SchemePrism) _SINGLETONS.get(scheme);
    if (prism==null)
    {
      prism=new SchemePrism(scheme);
      _SINGLETONS.put(scheme,prism);
    }
    return prism;
  }
  
  SchemePrism(Scheme scheme)
  { 
    _scheme=scheme;
    Iterator it=_scheme.getFields().iterator();
    while (it.hasNext())
    {
      Field field=(Field) it.next();
      _fields.put(field.getName(),field);
    }
  }

  public Field findField(String name)
  { return (Field) _fields.get(name);
  }
  
  public Scheme getScheme()
  { return _scheme;
  }

  public synchronized Binding resolve
    (Binding source
    ,Focus focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  {
    FieldLense lense=null;
    if (_fieldLenses==null)
    { _fieldLenses=new HashMap<String,FieldLense>();
    }
    else
    { lense=(FieldLense) _fieldLenses.get(name);
    }
    
    if (lense==null)
    {
      Field field
        =findField(name);

      if (field!=null)
      { 
        lense=new FieldLense(field);
        _fieldLenses.put(name,lense);
      }
      
    }
    
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
