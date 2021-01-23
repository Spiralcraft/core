//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.data.core;


import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;


import spiralcraft.data.Tuple;



import spiralcraft.data.lang.CursorChannel;



public class CursorField<T>
  extends FieldImpl<T>
{
  
  
  { setTransient(true);
  }
  
  public CursorField()
  { 
  }
 
  @Override
  public boolean isDerived()
  { return true;
  }
  
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Channel<T> bindChannel
    (Channel<Tuple> source
    ,Focus<?> focus
    ,Expression<?>[] params
    )
    throws BindException
  { 
    return new CursorChannel
      (getType()
      ,super.bindChannel(source,focus,params)
      );
  }
  

}