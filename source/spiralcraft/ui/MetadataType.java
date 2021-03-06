//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.ui;

import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.lang.Reflector;

import spiralcraft.util.refpool.URIPool;

/**
 * An enumeration of UI related metadata types
 * 
 * @author mike
 *
 */
public enum MetadataType
{
  FIELD(URIPool.create("class:/spiralcraft/ui/FieldMetadata"))
  ;
  
  public final URI uri;
  public final Type<Tuple> type;
  public final Reflector<Tuple> reflector;
  
  private MetadataType(URI uri)
  { 
    try
    {
      this.uri=uri;
      this.type=Type.resolve(uri);
      this.reflector=DataReflector.getInstance(type);
    }
    catch (ContextualException x)
    { throw new RuntimeException(x);
    }
    
  }
  
  
  
}
