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
package spiralcraft.data.core;


import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.core.FieldImpl;


public class SequenceField
  extends FieldImpl
{
  
  public SequenceField()
  { 
    try
    { setType(Type.resolve("class:/spiralcraft/data/types/standard/Integer"));
    }
    catch (DataException x)
    { x.printStackTrace();
    }
  }
  
  
  @Override
  public void resolve()
    throws DataException
  {
    if (!getType().isStringEncodable())
    {
      throw new DataException
        ("Type is not compatible with either Integer or String data"
        +"\r\n   field="+getURI()
        +"\r\n   type="+getType()
        );
    }
    super.resolve();
    
  }
  
  
}