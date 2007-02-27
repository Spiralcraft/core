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

import spiralcraft.data.Type;
import spiralcraft.data.Field;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.InstanceResolver;
import spiralcraft.data.Tuple;

import spiralcraft.data.core.TypeImpl;

import spiralcraft.data.spi.ConstructorInstanceResolver;
import spiralcraft.data.spi.StaticInstanceResolver;

import spiralcraft.data.wrapper.ReflectionType;

import java.net.URI;

/**
 * A Type implementation that reflects Type objects for
 *   the purpose of examining and extending them.
 */
public class TypeType
  extends ReflectionType<TypeImpl>
{
  private final InstanceResolver instanceResolver;
  private boolean metaType;
  
  /**
   * Construct a TypeType which creates Types with the specified URI based
   *   on TypeImpl bean implementation for extension using data constructs.
   *
   * This is the Constructor that is used when a ProtoType instantiates
   *   a data file that defines a Type- in the fromData() method, this instance
   *   will create Type objects with the specified URI that have their Scheme
   *   and other Properties defined from data.
   */
  public TypeType(TypeResolver resolver,URI uri)
  { 
    super(resolver,uri,TypeImpl.class,TypeImpl.class);
    // System.out.println("New TypeType for extension: "+uri+"="+super.toString());
    
    instanceResolver
      =new ConstructorInstanceResolver
      (new Class[] {TypeResolver.class,URI.class}
      ,new Object[] {resolver,uri}
      );
      
    metaType=false;
    // Use the URI field in the Tuple?
  }
  
  /**
   * Construct a TypeType that reflects the specified Type
   *  implementation- only called from TypeResolver when resolver uses
   *  the ".type" URI operator, which implies a reference to a Canonical
   *  type.
   *
   * The fromData() method, when the TypeType is constructed in this manner,
   *   will return the canonical instance of the base type. If any data is
   *   associated, an error in fromData() will result.
   */
  @SuppressWarnings("unchecked")
  public TypeType(TypeResolver resolver,URI uri,URI baseUri,Class baseTypeImplClass)
    throws DataException
  { 
    super(resolver
          ,uri
          ,(Class<TypeImpl>) baseTypeImplClass
          ,(Class<TypeImpl>) baseTypeImplClass
          );
    // System.out.println("New TypeType MetaType: "+uri+" of "+baseUri+"="+super.toString());
    instanceResolver
      =new StaticInstanceResolver
        (resolver.resolve(baseUri));
    metaType=true;
  }
  
  public TypeImpl fromString(String val)
    throws DataException
  { return (TypeImpl) getTypeResolver().resolve(URI.create(val));
  }
  
  public String toString(TypeImpl val)
  { return ((Type) val).getURI().toString();
  } 
  
  /**
   * Helps the ReflectionType generate instances 
   */
  protected TypeImpl obtainInstance(Tuple tuple,InstanceResolver instanceResolver)
    throws DataException
  {
    
    // Construct a new instance
    
    // System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
    // System.err.println("New-ing a "+getUri()+":"+super.toString());
    TypeImpl instance=null;
    instance=(TypeImpl) this.instanceResolver.resolve(getNativeClass());
    if (instance!=null)
    { return instance;
    }
    else
    { return super.obtainInstance(tuple,instanceResolver);
    }
  }

  public TypeImpl fromData(DataComposite composite,InstanceResolver instanceResolver)
    throws DataException
  {
    if (metaType)
    {
      Tuple tuple=composite.asTuple();
      // A metaType uses the .type operator in the uri. It can only be used
      //   to generate the resolved instance of the base type, which cannot
      //   be customized (the tuple must be empty).
      for (Field field : getScheme().fieldIterable())
      { 
        if (field.getValue(tuple)!=null)
        { throw new DataException("A MetaType cannot be customized");
        }
      } 
      TypeImpl type=(TypeImpl) this.instanceResolver.resolve(Type.class);
      // System.err.println("Returning metaType "+type.toString());
      return type;
    }
    else
    { return super.fromData(composite,instanceResolver);
    }
  }
}