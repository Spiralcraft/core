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

import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

import spiralcraft.lang.optics.LenseBinding;
import spiralcraft.lang.optics.Binding;

/**
 * A binding to a value of a field
 */
public class FieldBinding
  extends LenseBinding
{
  private final FieldLense lense;
  
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  public FieldBinding(Binding source,FieldLense lense)
  { 
    super(source,lense,null);
    this.lense=lense;
  }

  /**
   * Field bindings are never static, since the data in a Tuple can
   *   change even if the Tuple does not.
   */
  public boolean isStatic()
  { 
    // XXX Find a way to incorporate Tuple immutability to give a more
    // XXX intelligent response here.
    return false;
  }
  
  public boolean set(Object val)
  {
    
    Tuple tuple=(Tuple) getSourceValue();
    if (tuple!=null && (tuple instanceof EditableTuple))
    { 
      try
      {
        ((EditableTuple) tuple).set(lense.getField().getIndex(),val);
        return true;
      }
      catch (DataException x)
      { 
        x.printStackTrace();
        return false;
      }
    }
    return false;
  }
}
