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
package spiralcraft.data.types.meta;

import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.core.DerivedValue;
import spiralcraft.data.core.PrimitiveTypeImpl;
import spiralcraft.lang.ParseException;

import java.net.URI;

@SuppressWarnings("rawtypes")
public class DerivedValueType
  extends PrimitiveTypeImpl<DerivedValue>
{
  public DerivedValueType(TypeResolver resolver,URI uri)
  { super(resolver,uri,DerivedValue.class);
  }

  @Override
  public DerivedValue<?> fromString(String str)
    throws DataException
  { 
    try
    { return new DerivedValue(str);
    }
    catch (ParseException x)
    { throw new DataException("Error constructing expression from ["+str+"]: "+x,x);
    }
  }
  
  @Override
  public String toString(DerivedValue dv)
  { return dv.toString();
  }
}