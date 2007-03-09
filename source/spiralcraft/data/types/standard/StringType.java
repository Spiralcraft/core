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
package spiralcraft.data.types.standard;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;

import spiralcraft.data.core.PrimitiveTypeImpl;

import java.net.URI;

public class StringType
  extends PrimitiveTypeImpl<String>
{
  private int maxLength=-1;
  
  public StringType(TypeResolver resolver,URI uri)
  { super(resolver,uri,String.class); 
  }
  
  
  public String fromString(String val)
  { return val;
  }

  public void setMaxLength(int val)
  { maxLength=val;
  }
  
  public int getMaxLength()
  { return maxLength;
  }
  
  public ValidationResult validate(Object val)
  {
    if (val==null)
    { return null;
    }
    
    ValidationResult ret=super.validate(val);
    if (ret!=null)
    { return ret;
    }
    
    if (maxLength>-1)
    {
      if ( ((String) val).length()>maxLength)
      { return new ValidationResult("String length exceeds "+maxLength);
      }
    }
    return null;
  }
}
