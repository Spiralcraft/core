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

import spiralcraft.data.core.PrimitiveTypeImpl;

import java.net.URI;
import java.util.TimeZone;

public class TimeZoneType
  extends PrimitiveTypeImpl<TimeZone>
{
  public TimeZoneType(TypeResolver resolver,URI uri)
  { super(resolver,uri,TimeZone.class);
  }
  
  @Override
  public TimeZone fromString(String str)
  { 
    return TimeZone.getTimeZone(str);
  }
  
  @Override
  public String toString(TimeZone zone)
  { return zone.getID();
  }
}