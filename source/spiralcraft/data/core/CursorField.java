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


import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;


import spiralcraft.data.Tuple;


import spiralcraft.data.core.FieldImpl;

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
  @SuppressWarnings("unchecked")
  public Channel<T> bindChannel(Focus<Tuple> focus)
    throws BindException
  { 
    return new CursorChannel
      (getType()
      ,super.bindChannel(focus)
      );
  }
  

}