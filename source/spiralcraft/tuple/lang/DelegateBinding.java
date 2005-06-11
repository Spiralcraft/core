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

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Decorator;

import spiralcraft.lang.optics.AbstractBinding;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.Binding;

/**
 * Abstract class to help implement Bindings that expose a specified Java type 
 *   using metadata driven underlying object not directly associated with the
 *   specified type.
 *
 * XXX Evaluate utility of this class- only used to implement TupleBinding for
 *     now.
 */ 
public abstract class DelegateBinding
  extends AbstractBinding
{
  public DelegateBinding(Class clazz)
    throws BindException
  { super(new DelegatePrism(clazz),true);
  }
  
  public abstract Binding getBinding();

}

class DelegatePrism
  implements Prism
{
  private final Class _class;
  
  public DelegatePrism(Class clazz)
  { _class=clazz;
  }
  
  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  { 
    Binding binding=((DelegateBinding) source).getBinding();
    return binding.getPrism().resolve(binding,focus,name,params);
  }
  
  public Decorator decorate(Binding source,Class decoratorInterface)
  { 
    try
    {
      Binding binding=((DelegateBinding) source).getBinding();
      return binding.getPrism().decorate(binding,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error Decorating",x);
    }
    
  }
  
  public Class getContentType()
  { return _class;
  }
}
