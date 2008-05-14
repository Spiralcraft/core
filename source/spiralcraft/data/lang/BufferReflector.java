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

import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

public class BufferReflector<T extends Tuple>
  extends TupleReflector<T>
{

  BufferReflector(Type<?> type,Class<T> contentType)
    throws BindException
  { super(type,contentType);
  }
  
  /**
   * Resolve a Binding that provides access to a member of a Tuple given a 
   *   source that provides Tuples.
   */
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  public synchronized Channel resolve
    (final Channel source
    ,Focus focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  {  
    
    return super.resolve(source,focus,name,params);
  }
}
