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

import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.PrimitiveTypeImpl;
import spiralcraft.util.string.InstantToString;
import spiralcraft.util.string.StringConverter;

import java.net.URI;

import spiralcraft.time.Instant;

public class InstantType
  extends PrimitiveTypeImpl<Instant>
{
  
  private String format="yyyy-MM-dd HH:mm:ss.SSS Z";
  
  private StringConverter<Instant> converter;
  
  public InstantType(TypeResolver resolver,URI uri)
  { super(resolver,uri,Instant.class);
  }
  
  @Override
  public synchronized  Instant fromString(String str)
    throws DataException
  { 
    link();
    
    if (str!=null)
    { return converter.fromString(str);

    }
    else
    { return null;
    }
  }
  
  @Override
  public synchronized String toString(Instant instant)
  { 
    link();
    if (instant!=null)
    { return converter.toString(instant);
    }
    else
    { return null;
    }
  }

  @Override
  protected void linkPrimitive()
  { 
    converter=new InstantToString(format);
  }
  
  public String getFormat()
  {
    return format;
  }

  public void setFormat(
    String format)
  {
    if (linked)
    { throw new IllegalStateException("Type "+getURI()+" is already linked");
    }
    this.format = format;
  }

  
}