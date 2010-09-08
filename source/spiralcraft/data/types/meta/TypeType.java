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
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.core.MetaType;
import spiralcraft.data.core.SchemeImpl;
import spiralcraft.data.core.TypeImpl;


import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.EditableArrayTuple;

import java.net.URI;

/**
 * A Type implementation that reflects Type objects for
 *   the purpose of examining and extending them.
 */

//@SuppressWarnings("unchecked") // Runtime .class resolution
@SuppressWarnings("rawtypes")
public class TypeType
  extends ReflectionType<Type>
{
  
  { 
    ReflectionType.registerCanonicalType
      (TypeImpl.class,URI.create("class:/spiralcraft/data/types/meta/Type"));
    ReflectionType.registerCanonicalType
      (FieldImpl.class,URI.create("class:/spiralcraft/data/types/meta/Field"));
    ReflectionType.registerCanonicalType
      (SchemeImpl.class,URI.create("class:/spiralcraft/data/types/meta/Scheme"));
    
  }
  
  /**
   * Construct a TypeType which creates Types with the specified URI based
   *   on TypeImpl bean implementation for extension using data constructs.
   *
   * This is the Constructor that is used when a ProtoType instantiates
   *   a data file that defines a Type- in the fromData() method, this instance
   *   will create Type objects with the specified URI that have their Scheme
   *   and other Properties defined from data.
   */
  @SuppressWarnings("unchecked")
  public TypeType(TypeResolver resolver,URI uri)
  { super(resolver,uri,(Class) TypeImpl.class,(Class) TypeImpl.class);
  }

  @Override
  public Tuple toData(Type obj)
    throws DataException
  { 
    log.fine("Generating typeRef for "+obj);
    
    MetaType metaType
      =new MetaType(obj);
    return new EditableArrayTuple(metaType);
  }
  
//  /**
//   * Generate the data structure for a Type definition
//   * 
//   * @param type
//   * @return
//   */
//  public Tuple toTypeData(TypeImpl type)
//    throws DataException
//  { return (Tuple) super.toData(type);
//  }
//  
  
//  /**
//   * Types are always singletons- just resolve the URI.
//   */
//  public TypeImpl fromString(String val)
//    throws DataException
//  { return (TypeImpl) getTypeResolver().resolve(URI.create(val));
//  }
  
//  public String toString(TypeImpl val)
//  { return val.getURI().toString();
//  } 
 
}