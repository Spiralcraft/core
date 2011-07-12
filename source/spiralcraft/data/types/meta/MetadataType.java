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
package spiralcraft.data.types.meta;

import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.lang.Reflector;

/**
 * <p>An enumeration of data model related metadata types for channels
 * </p>
 * 
 * @author mike
 *
 */
public enum MetadataType
{
  FIELD(URI.create("class:/spiralcraft/data/types/meta/Field"));
  
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
