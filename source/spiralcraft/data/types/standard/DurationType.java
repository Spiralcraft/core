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
import spiralcraft.util.string.DurationToString;
import spiralcraft.util.string.StringConverter;

import java.net.URI;

import spiralcraft.time.Duration;

public class DurationType
  extends PrimitiveTypeImpl<Duration>
{
  
  
  private StringConverter<Duration> converter;
  
  public DurationType(TypeResolver resolver,URI uri)
  { super(resolver,uri,Duration.class);
  }
  
  @Override
  public synchronized  Duration fromString(String str)
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
  public synchronized String toString(Duration duration)
  { 
    link();
    if (duration!=null)
    { return converter.toString(duration);
    }
    else
    { return null;
    }
  }

  @Override
  protected void linkPrimitive()
  { 
    converter=new DurationToString();
  }  
}