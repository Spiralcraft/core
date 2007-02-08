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

import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Buffer;

import spiralcraft.lang.optics.LenseBinding;
import spiralcraft.lang.optics.Binding;

/**
 * A binding to a value of a field
 */
public class FieldBinding
  extends LenseBinding
{
  private final FieldLense _lense;
  
  public FieldBinding(Binding source,FieldLense lense)
  { 
    super(source,lense,null);
    _lense=lense;
  }

  /**
   * Field bindings are never static, since the data in a Tuple can
   *   change even if the Tuple does not.
   */
  public boolean isStatic()
  { return false;
  }
  
  public boolean set(Object val)
  {
    
    Tuple tuple=(Tuple) getSourceValue();
    if (tuple!=null && (tuple instanceof Buffer))
    { 
      ((Buffer) tuple).set(_lense.getField().getIndex(),val);
      return true;
    }
    return false;
  }
}
