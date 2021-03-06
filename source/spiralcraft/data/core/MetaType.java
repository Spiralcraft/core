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
import spiralcraft.data.TypeParameter;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Tuple;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.util.ConstructorInstanceResolver;
import spiralcraft.data.util.InstanceResolver;
import spiralcraft.data.util.StaticInstanceResolver;
import spiralcraft.log.ClassLog;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;

import java.net.URI;

/**
 * A Type implementation that implements the canonical MetaType of a canonical Type
 */
@SuppressWarnings({"unchecked","rawtypes"}) // Runtime class resolution
public class MetaType
  extends ReflectionType<Type>
{
  private static final ClassLog log
    =ClassLog.getInstance(MetaType.class);
  
  private static boolean debug=false;
  
  private Type<?> referencedType;
  private int anonRefId=1;

  private TypeParameter[] addParameters;
  
  
  /**
   * <p>Construct a TypeType that reflects the implementation of the referenced
   *  Type.
   * </p>
   * 
   * <p>Only used by the TypeResolver when resolver uses the ".type" URI operator,
   *  which implies a reference to a Canonical type.
   * </p>
   *
   * <p>The fromData() method will return the canonical instance of the referenced type.
   * If any data is contained in the tuple, an error in fromData() will result.
   * </p>
   */
  public MetaType
    (TypeResolver resolver,URI uri,URI referencedTypeURI,Class referencedTypeImplClass)
    throws DataException
  {  
    super(resolver
          ,uri
          , referencedTypeImplClass
          , referencedTypeImplClass
          );
    if (debug)
    { log.fine("Created metaType "+System.identityHashCode(this)+" "+uri+" of "+referencedTypeURI);
    }
    
    referencedType=resolver.resolve(referencedTypeURI,false);
    this.addParameters=referencedType.getParameters();
  }
  
  /**
   * <p>Construct a temporary type reference used to generate Type definition data
   *   via the toData() method.
   * </p>
   * @param referencedType
   */
  public MetaType(Type referencedType)
    throws DataException
  { 
    super
      (referencedType.getTypeResolver()
      ,URIPool.create(referencedType.getURI().toString()+".type")
      ,(Class) referencedType.getClass()
      ,(Class) referencedType.getClass()
      );
    if (debug)
    { log.fine("Created metaType "+System.identityHashCode(this)+" "+uri+" of "+referencedType);
    }
    this.referencedType=referencedType;
    link();
  }  
  
  @Override
  public Type<?> fromString(String val)
    throws DataException
  { return Type.resolve(URIPool.create(val));
  }
  
  @Override
  public String toString(Type val)
  { return val.getURI().toString();
  } 
  
  /**
   * Create a new subtype instance for type extension (making new instances
   *   of various Type implementations), to bypass the type reference behavior
   *   which returns canonical instances- e.g. providing an in-line extension
   *   for String.type
   * 
   * @param composite
   * @param uri
   * @return
   * @throws DataException
   */
  public Type newSubtype(DataComposite composite,URI uri)
    throws DataException
  {
    referencedType.link();
    InstanceResolver instanceResolver
      =referencedType.getExtensionResolver
        (TypeResolver.getTypeResolver()
        ,uri
        );
    if (debug)
    { log.fine("Creating new auto-subtype of "+getURI()+"@"+System.identityHashCode(this)+": "+uri);
    }
    return super.fromData(composite,instanceResolver);
    
  }
  
  @Override
  /**
   * Called by the ReflectionType to add any extra fields not part of the
   *   bean- a.k.a. type parameters so that they can be set when overriding
   *   the type definition.
   */
  protected void addFields()
    throws DataException
  { 
    if (addParameters!=null)
    {
      for (TypeParameter param : addParameters)
      { scheme.addField(new TypeParameterField(param));
      }
    }
  }
  
  @Override
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
      // XXX A StaticInstanceResolver is provided by ReflectionField when
      //  reading the existing value of a bean property. This may have
      //  nothing to do with whether we're creating a referenced type or not,
      //  as the referenced value may be the pre-existing value assigned
      //  to the bean property.
      //
      //  A StaticInstanceResolver is also provided by the XmlTypeFactory
      //    to indicate that a type is being created. These two cases
      //    potentially cause some conflict.
      
      
      // We have either already instantiated a new instance that is to be
      //   configured, or the StaticInstanceResolver refers to some default
      //   value. 
      
      // Best way to differentiate for now is to avoid modifying a linked
      //   type.
      
      Type<?> defaultInstance
        =(Type<?>) instanceResolver.resolve(getNativeClass());
      if (defaultInstance!=null && !defaultInstance.isLinked())
      { 
        // We're in the process of creating a unique Type
        referenced=false;
        
      }
      else
      { 
        // A linked type indicates some pre-existing default value
        // A null defaultInstance doesn't provide us with anything useful
        instanceResolver=null;
      }
      
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
//        final URI uri
//          =URIPool.create(getURI().toString().concat("-"+(anonRefId++)));
        
        final URI uri
          =URIUtil.addPathSuffix
              (URIUtil.removePathSuffix(getURI(),".type")
              ,"-"+(anonRefId++)
              );
        
        instanceResolver
          =new ConstructorInstanceResolver
            (new Class[] {TypeResolver.class,URI.class}
            ,new Object[] {TypeResolver.getTypeResolver(),uri}
            );

        if (debug)
        { log.fine("Using MetaType to create new anonymous extended type "+uri);
        }
        
        // Resolve the generic type to make sure it has been linked
        Type genericType=TypeResolver.getTypeResolver().resolve
          (URIUtil.removePathSuffix(getURI(),".type"));
//        log.fine("Generic type is "+genericType);
        
        // Write any type arguments to the derived type
        Type type=super.fromData(composite,instanceResolver);
        for (Field field:scheme.fieldIterable())
        { 
          if (field instanceof TypeParameterField)
          { ((TypeParameterField) field).fromData((TypeImpl) type,(Tuple) composite);
          }
        }
        
        if (type instanceof DataDefinedType)
        { 
          ((DataDefinedType) type).setArchetype(genericType);
          if (genericType.isAbstract())
          { ((DataDefinedType) type).setAbstract(true);
          }
        }
        
        // Register the type (but don't link)
        TypeResolver.getTypeResolver().register(uri,type);
        // type.link();
        return type;
      }
      else
      { 
        // Create an instance of the ReflectionType we're extending
        return super.fromData(composite,instanceResolver);
      }
    }
    
  }
}