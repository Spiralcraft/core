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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class URLType
  extends PrimitiveTypeImpl<URL>
{
  public URLType(TypeResolver resolver,URI uri)
  { super(resolver,uri,URL.class);
  }
  
  @Override
  public URL fromString(String str)
  { 
    try
    { return str!=null?new URL(str):null;
    }
    catch (MalformedURLException x)
    { throw new IllegalArgumentException("Malformed URL: "+str);
    }
  }
}