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


import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;

/**
 * A TupleBinding where the contained Tuple never changes
 */
public class StaticTupleBinding<T extends Tuple>
  extends TupleBinding<T>
{
  private T tuple;
  
  public StaticTupleBinding(FieldSet fieldSet,T data)
    throws BindException
  { 
    super(fieldSet,true);
    tuple=data;
  }

  public FieldSet getFieldSet()
  { return ((TupleReflector) getReflector()).getFieldSet();
  }

  protected T retrieve()
  { return tuple;
  }
  
  protected boolean store(Tuple val)
  { throw new UnsupportedOperationException("Can't replace tuple");
  }

}

