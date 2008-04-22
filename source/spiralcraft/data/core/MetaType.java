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
package spiralcraft.data.core;

import spiralcraft.data.Type;
import spiralcraft.data.Field;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Tuple;

import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.data.util.ConstructorInstanceResolver;
import spiralcraft.data.util.InstanceResolver;
import spiralcraft.data.util.StaticInstanceResolver;

import java.net.URI;

/**
 * A Type implementation that implements the canonical MetaType of a canonical Type
 */
@SuppressWarnings("unchecked") // Runtime class resolution
public class MetaType
  extends ReflectionType<Type>
{
  private static boolean debug;
  
  private Type<?> referencedType;
  private int anonRefId=1;
  
  /**
   * <P>Construct a TypeType that reflects the implementation of the referenced Type.
   * 
   * <P>Only used by the TypeResolver when resolver uses the ".type" URI operator,
   *  which implies a reference to a Canonical type.
   *
   * <P>The fromData() method will return the canonical instance of the referenced type.
   * If any data is contained in the tuple, an error in fromData() will result.
   */
  @SuppressWarnings("unchecked")
  public MetaType
    (TypeResolver resolver,URI uri,URI referencedTypeURI,Class referencedTypeImplClass)
    throws DataException
  {  
    super(resolver
          ,uri
          ,(Class<Type>) referencedTypeImplClass
          ,(Class<Type>) referencedTypeImplClass
          );
//    System.err.println
//      ("New MetaType: "+uri+" of "+referencedTypeURI+"="+super.toString());
    
    referencedType=resolver.resolve(referencedTypeURI);
  }
  
  
  public Type<?> fromString(String val)
    throws DataException
  { return getTypeResolver().resolve(URI.create(val));
  }
  
  public String toString(Type val)
  { return val.getURI().toString();
  } 
  
  /**
   * Create a new subtype instance for type extension (making new instances
   *   of various Type implementations), to bypass the type reference behavior
   *   which returns canonical instances.
   * 
   * @param composite
   * @param uri
   * @return
   * @throws DataException
   */
  public Type newSubtype(DataComposite composite,URI uri)
    throws DataException
  {
    
    InstanceResolver instanceResolver
      =new ConstructorInstanceResolver
        (new Class[] {TypeResolver.class,URI.class}
        ,new Object[] {getTypeResolver(),uri}
        );
    return super.fromData(composite,instanceResolver);
    
  }
  
  public Type fromData(DataComposite composite,InstanceResolver instanceResolver)
    throws DataException
  {
    Tuple tuple=composite.asTuple();
    
    boolean referenced=true;
    // A metaType uses the .type operator in the uri. It can only be used
    //   to generate the resolved instance of the base type, which cannot
    //   be customized (the tuple must be empty).
    for (Field field : getScheme().fieldIterable())
    { 
      if (field.getValue(tuple)!=null)
      { 
        referenced=false;
        break;
      }
    } 
    if (instanceResolver instanceof StaticInstanceResolver)
    { 
      // We already have an instance
      referenced=false;
    }
    
      
    if (referenced)
    { return referencedType;
    }
    else
    { 
      if (!(instanceResolver instanceof StaticInstanceResolver))
      {
        // A StaticInstanceResolver indicates that we've already created
        //   the desired instance.
        
        // Create an anonymous subtype
        // TODO: clean up and consolidate with newSubtype()
        final URI uri=URI.create(getURI().toString().concat("-"+(anonRefId++)));
        
        
        instanceResolver
          =new ConstructorInstanceResolver
            (new Class[] {TypeResolver.class,URI.class}
            ,new Object[] {getTypeResolver(),uri}
            );

        if (debug)
        { log.fine("Using MetaType to create new anonymous extended type "+uri);
        }
        
        Type type=super.fromData(composite,instanceResolver);
        type.link();
        return type;
      }
      else
      { return super.fromData(composite,instanceResolver);
      }
    }
    
  }
}