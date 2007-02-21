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
package spiralcraft.data.lang;

import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.AbstractBinding;

import spiralcraft.data.Tuple;
import spiralcraft.data.Scheme;

/**
 * A spiralcraft.lang binding for Tuples, which uses the Tuple's Scheme
 *   as the type model for binding expressions.
 */
public class TupleBinding
  extends AbstractBinding
{
  private Tuple tuple;
  
  public TupleBinding(Scheme scheme)
    throws BindException
  { super(SchemePrism.getInstance(scheme),false);
  }
  
  public TupleBinding(Scheme scheme,Tuple data)
    throws BindException
  { 
    super(SchemePrism.getInstance(scheme),true);
    tuple=data;
  }

  public Scheme getScheme()
  { return ((SchemePrism) getPrism()).getScheme();
  }

  protected Object retrieve()
  { return tuple;
  }
  
  protected boolean store(Object val)
  { 
    tuple=(Tuple) val;
    return true;
  }

}

