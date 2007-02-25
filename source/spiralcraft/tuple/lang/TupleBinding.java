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

import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.AbstractBinding;

import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Scheme;


/**
 * A spiralcraft.lang binding for Tuples, which uses the Tuple's Scheme
 *   as the type model for binding expressions.
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class TupleBinding
  extends AbstractBinding
{
  private Tuple _tuple;
  
  public TupleBinding(Scheme scheme)
    throws BindException
  { super(SchemePrism.getInstance(scheme),false);
  }
  
  public TupleBinding(Scheme scheme,Tuple data)
    throws BindException
  { 
    super(SchemePrism.getInstance(scheme),true);
    _tuple=data;
  }

  public Scheme getScheme()
  { return ((SchemePrism) getPrism()).getScheme();
  }

  protected Object retrieve()
  { return _tuple;
  }
  
  protected boolean store(Object val)
  { 
    _tuple=(Tuple) val;
    return true;
  }

}

