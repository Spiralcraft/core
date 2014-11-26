//
// Copyright (c) 2014 Michael Toth
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

import java.net.URI;

import spiralcraft.data.TypeResolver;

public class DataDefinedType<T>
  extends TypeImpl<T>
{
  public DataDefinedType(TypeResolver resolver,URI uri)
  { super(resolver,uri);
  }
  
  @Override
  public void link()
  { 
    if (linked)
    { return;
    }
    
    if (scheme==null)
    { this.scheme=new SchemeImpl();
    }
    super.link();
  }

}