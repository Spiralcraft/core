//
//Copyright (c) 2012 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app.kit;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;

/**
 * A State base type which allows a Component to be driven by or synchronized
 *   to Tuple data.
 * 
 * @author mike
 *
 */
public class TupleState
  extends ValueState<Tuple>
{
  public TupleState(int numChildren,String stateId)
  { super(numChildren,stateId);
  }


  @SuppressWarnings("unchecked")
  public <T> T get(String prop)
  { 
    Tuple tuple=getValue();
    if (tuple==null)
    { return null;
    }
    
    try
    { return (T) tuple.get(prop);
    }
    catch (DataException x)
    { throw new RuntimeException("Error reading "+prop,x);
    }
  }
  
}
