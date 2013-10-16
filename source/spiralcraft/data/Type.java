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
package spiralcraft.data;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXException;

import spiralcraft.common.attributes.Attribute;
import spiralcraft.common.attributes.AttributeSet;
import spiralcraft.data.core.DeltaType;
import spiralcraft.data.core.MetaType;
import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.DataWriter;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.spi.ArrayTuple;
// import spiralcraft.log.ClassLogger;
import spiralcraft.data.util.ConstructorInstanceResolver;
import spiralcraft.data.util.InstanceResolver;
import spiralcraft.lang.spi.Translator;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
//import spiralcraft.log.ClassLog;
import spiralcraft.rules.RuleSet;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.string.StringConverter;
import spiralcraft.util.string.StringUtil;
import spiralcraft.util.thread.ThreadLocalStack;
import spiralcraft.vfs.Resource;


/**
 * Describes the data type of a data element.<BR>
 */
public abstract class Type<T>
{  
  private static final ClassLog log=ClassLog.getInstance(Type.class);
  private static final Level LOG_LEVEL
//    =ClassLog.getInitialDebugLevel(Type.class,Level.FINE);
    =ClassLog.getInitialDebugLevel(Type.class,null);
  
  public static <X> Type<X> resolve(String uriString)
    throws DataException
  { return TypeResolver.getTypeResolver().<X>resolve(URIPool.create(uriString));
  }

  public static <X> Type<X> resolve(URI uri)
    throws DataException
  { return TypeResolver.getTypeResolver().<X>resolve(uri);
  }
  
  public static <X> Type<List<X>> getArrayType(Type<X> type)
  { 
    try
    { 
      return type.getTypeResolver().<List<X>>resolve
        (URIPool.create(type.getURI().toString()+".array"));
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  }
    
  public static <X> Type<List<X>> getAggregateType(Type<X> type)
  { 
    try
    { 
      return type.getTypeResolver().<List<X>>resolve
        (URIPool.create(type.getURI().toString()+".list"));
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  }


  
  @SuppressWarnings("unchecked")
  
  /**
   * Return the corresponding BufferType for specified type. If the type
   *   is already a BufferType, it will be returned.
   */
  public static <X> Type<Buffer> getBufferType(Type<X> type)
  { 
    try
    { 
      if (type instanceof BufferType)
      { return (Type<Buffer>) type;
      }
      
      if (type.getCoreType().isPrimitive())
      { throw new IllegalArgumentException
          ("Cannot buffer a primitive type "+type.getURI());
      }
//      log.fine
//        ("Buffer Type for "+type+" is "
//        +resolve(type.getURI().toString()+".buffer")
//        );
      return type.getTypeResolver().<Buffer>resolve
        (URIPool.create(type.getURI().toString()+".buffer"));
      
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  }
  
  
  @SuppressWarnings("unchecked")
  
  /**
   * Return the corresponding DeltaType for specified type. If the type
   *   is already a DeltaType, it will be returned.
   */
  public static <X> Type<DeltaTuple> getDeltaType(Type<X> type)
  { 
    try
    { 
      if (type instanceof DeltaType)
      { return (Type<DeltaTuple>) type;
      }
//      log.fine
//        ("Delta Type for "+type+" is "
//        +resolve(type.getURI().toString()+".buffer")
//        );
      return type.getTypeResolver().<DeltaTuple>resolve
        (URIPool.create(type.getURI().toString()+".delta"));
      
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  }
  
  private static ThreadLocalStack<URI> linkStack
    =new ThreadLocalStack<URI>();
  
//  protected final ClassLog log=ClassLog.getInstance(getClass());
  
  protected final void pushLink(URI uri)
  { 
    linkStack.push(uri);
    if (LOG_LEVEL.isTrace())
    { 
      log.trace
        (StringUtil.repeat("->",getLinkDepth())
        +"Linking Type "+uri
        );
      if (LOG_LEVEL.isFine())
      { log.log(Level.FINE,"Link trace",new Exception());
      }
    }
  }
  
  protected final void popLink()
  { 
    if (LOG_LEVEL.isTrace())
    { 
      log.trace
        (StringUtil.repeat("->",getLinkDepth())
        +"Done Linking Type "+linkStack.get()
        );
    }
    linkStack.pop();
  }
  
  protected int getLinkDepth()
  { return linkStack.size();
  }
  
  protected final URI[] linkStack()
  { return linkStack.contents(new URI[0]);
  }
  
  static
  {
    StringConverter.registerInstance
      (Type.class
      ,new StringConverter<Type<?>>()
      {
        @Override
        public Type<?> fromString(String val)
        { 
          try
          { return resolve(val);
          }
          catch (DataException x)
          { throw new IllegalArgumentException(x);
          }
        }
        
        @Override
        public String toString(Type<?> val)
        { return val!=null?val.getURI().toString():null;
        }
      }
      );
  }
  

  protected final ArrayList<Method> methods
    =new ArrayList<Method>();
  protected final HashMap<String,Method[]> methodMap
    =new HashMap<String,Method[]>();
//  protected RuleSet<Type<T>,T> ruleSet;
  protected boolean debug;
  protected Translator<?,T> externalizer;
  protected AttributeSet attributes;
  
  /**
   * The TypeResolver which instantiated this particular Type.
   */
  public abstract TypeResolver getTypeResolver();

  /**
   * The canonical URI for this type.
   */
  public abstract URI getURI();
  
  /**
   * The URI of the package or container in which this type is defined
   * 
   * @return
   */
  public abstract URI getPackageURI();
  
  /**
   * A brief textual description of this type suitable for the context(s) in
   *   which it will be encountered by a user, administrator or developer.
   * 
   * @return
   */
  public abstract String getDescription();
  
  /**
   * The Type used to describe Type objects of this Type, to support the
   *   defining of Type extensions using data.
   */
  public abstract Type<?> getMetaType();
  
  /**
   * The public Java class or interface used to programatically access or
   *   manipulate this data element.
   *
   * @return A Class Or Interface, or null if the data element should be
   *   manipulated as Tuple data.
   */
  public abstract Class<T> getNativeClass();

  /**
   * A primitive type is a 'leaf node' of a data tree. A DataComposite
   *   which contains data of a primitive Type references a plain old Java
   *   objects of this Types native type. 
   *
   * A DataComposite which contains non-primitive Types holds other
   *   DataComposites (ie. Tuples and Aggregates)
   * 
   * @return Whether this is a primitive type.
   */
  public abstract boolean isPrimitive();
  //
  // Usage survey: 2008-12-04
  //
  //   AbstractCollectionType.toData()
  //   ArrayType.fromData()
  //   ArrayType.toData()
  //   TypeImpl.isAssignableFrom() (delegates to native type)
  //   DataReflector.getInstance()
  //   ReflectionField.depersistBeanProperty()
  //   ReflectionField.persistBeanProperty()
  //   DataHandler.AggregateFrame,ContainerFrame,DetailFrame - Tuple<->Native
  //   BufferType.link() which fields to auto-buffer (only relationships)
  
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   */
  public abstract Scheme getScheme();
  
  /**
   * @return Whether this Type is an aggregate (array or collection) of another
   *   type.
   */
  public abstract boolean isAggregate();
   
  /**
   * @return The Type of data aggregated by this aggregate Type, or null if 
   *   this is not an aggregate Type.
   */
  public abstract Type<?> getContentType();
  
  /**
   * @return The first non-aggregate returned by recursive calls to
   *   getContentType(), or this type if not an aggregate.
   */
  public abstract Type<?> getCoreType();
  
  /**
   * An Archetype is a Type which defines a data structure and operations
   *   inherited by this type. The Archetype will typically be recognized
   *   by common data-aware components, and this subtype will augment the
   *   Archetype with extra Fields and operations, usually in the context of
   *   a more specific data model.<P>
   * 
   * A Type and all its archetypes are represented by a single Tuple. The
   *   sequence of Fields in the Scheme will start with the Archetype's Fields
   *   and end with the Fields declared in this Type.<P>
   *
   * @return The archetype of this Type
   */
  public abstract Type<?> getArchetype();
  
  @SuppressWarnings("unchecked") // Scheme is not genericized
  public Key<T> getPrimaryKey()
  { 
    link();
    Key<T> key=null;
    if (getScheme()!=null)
    { key=(Key<T>) getScheme().getPrimaryKey();
    }
    if (key==null && getBaseType()!=null)
    { key=getBaseType().getPrimaryKey();
    }
    return key;
    
  }
  
  /**
   * @return Whether this Type or any of its archetypes (recursively) is the
   *   the specified Type.
   */
  public abstract boolean hasArchetype(Type<?> type);
  
  /**
   * A base Type is a means for this Type to inherit an identity, data structure
   *   and operations from another Type, in order to further specify a variation
   *   of the base Type.<P>
   *
   * A Type which extends a base Type has compound instances composed of extents.<P>
   *
   * In a given compound instance, there is one extent per Type in the 
   *   hierarchy which holds the data associated with the Type. The extents in
   *   the instance are chained to each other. The most specific sub-type, when
   *   instantiated, will cause the other extents to be created. Any deletion
   *   will cause all the extents to be deleted. Each extent is aware of the
   *   most specific extent in the heirarchy as well as its immediate base
   *   extent.<P>
   * 
   * Data structure and operation inheritance is realized via delegation. A more
   *   specific extent will delegate field access and operations to its
   *   immediate base extent if the operations are not relevant to the more
   *   specific extent.<P>
   *
   * Polymorphism via virtualization is realized by delegating virtual
   *   operations on a general extent to the most specific extent.<P>
   *
   * Tuples which participate as an extent in a class hierarchy require extra
   *   storage to maintain their relationship to other Tuples of the same
   *   instance.<P>
   */
  public abstract Type<T> getBaseType();
  
  /**
   * 
   * @return Whether another Type can extend this Type. 
   */
  public abstract boolean isExtendable();
  
  /**
   * 
   * @return Whether this Type can be directly instantiated. 
   */
  public abstract boolean isAbstract();

  /**
   * @return Whether this Type or any of its base Types (recursively) is the
   *   the specified Type.
   */
  public abstract boolean hasBaseType(Type<?> type);
  
  /**
   * @return Whether a variable of this Type may be assigned a value corresponding to
   *   the specified Type. Recurses through all archetypes and base types.
   */
  public abstract boolean isAssignableFrom(Type<?> type);
  

  /**
   * Returns the Field with the specified name in this Type or its base Types
   * @param name
   * @return the Field
   */
  public abstract <X> Field<X> getField(String name);
  
  /**
   * Returns the FieldSet composed of the fields in this Type and
   *   all its base types. 
   *   
   * @param name
   * @return the Field
   */
  public abstract FieldSet getFieldSet();
  
  
  /**
   * Return the Keys defined for this Type, if any.
   * 
   * @return
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Key<T>[] getKeys()
  {
    Key[] baseKeys=null;
    
    if (getBaseType()!=null)
    { baseKeys=getBaseType().getKeys();
    }
    
    Key<T>[] localKeys=null;
    if (scheme()!=null)
    { 
      localKeys
        =(Key<T>[]) ArrayUtil.toArray(Key.class,scheme().keyIterable());
    }
    
    if (localKeys==null && baseKeys==null)
    { return null;
    }
    
    if (localKeys!=null && (baseKeys==null || baseKeys.length==0))
    { return localKeys;
    }

    if (baseKeys!=null && (localKeys==null || localKeys.length==0))
    { return baseKeys;
    }
    
    return ArrayUtil.concat(localKeys,baseKeys);
     
  }
  
  /**
   * <p>Return the Key defined with respect to the specified field vector.
   * </p>
   * 
   * @param fieldNames
   * @return
   */
  @SuppressWarnings("unchecked")
  public Key<T> findKey(String[] fieldNames)
  {
    link();
    Key<T> key=null;
    if (getBaseType()!=null)
    { key=getBaseType().findKey(fieldNames);
    }
    if (key==null && getArchetype()!=null)
    { key=(Key<T>) getArchetype().findKey(fieldNames);
    }
    if (key==null)
    { key=(Key<T>) getScheme().findKey(fieldNames);
    }
    return key;
  }
  
  
  /**
   * The Method with the specified name that best matches the
   *   specified parameters.
   * 
   * @param name
   * @param parameterTypes
   * @return
   */
  public Method findMethod(
    String name,
    Type<?>[] parameterTypes)
  {
    Method[] matches=methodMap.get(name);
    if (matches!=null)
    { 
      for (Method method:matches)
      {
        Type<?>[] formalParams=method.getParameterTypes();
        if (formalParams.length==parameterTypes.length)
        {
          for (int i=0;i<formalParams.length;i++)
          { 
            if (formalParams[i].isAssignableFrom(parameterTypes[i]))
            { return method;
            }
            
          }
        }
      }
    }
    if (getArchetype()!=null)
    { return getArchetype().findMethod(name,parameterTypes);
    }
    return null;
  }


  /**
   * 
   * @return The Methods that belong to this Type
   */
  public Method[] getMethods()
  {
    Method[] ret=new Method[methods.size()];
    methods.toArray(ret);
    return ret;
    
  }  
  
  /**
   * 
   * @param resolver
   * @param uri
   * @return A resolver that constructs a new Type instance that uses
   *   this Type as prototype
   */
  public InstanceResolver getExtensionResolver(TypeResolver resolver,URI uri)
  {
    return new ConstructorInstanceResolver
      (new Class<?>[] {TypeResolver.class,URI.class}
      ,new Object[] {resolver,uri}
      );
  }
  
  /**
   * <p>Return the Translator used to externalize/internalize this Type
   * </p> 
   * 
   * @return
   */
  public abstract Translator<?,T> getExternalizer()
    throws DataException;
 
  /**
   * Indicate whether Objects of this type can be encoded to and decoded from
   *   String form. This will only return true if getNativeClass()!=null.
   *
   * @return Whether Objects of this type can be represented as a String.
   */
  public abstract boolean isStringEncodable();

  /**
   * Indicates whether a Type referers to data that is encodable as
   *   a DataComposite (A Tuple or Aggregate). This will only return false
   *   for primitives that should always be encoded as Strings (some primitives
   *   can be encoded as Data if they are immutable).
   */
  public abstract boolean isDataEncodable();
  
  /**
   * Translate the canonical String representation of a value of this Type to
   *   an Object presenting a suitable interface.<P>
   *
   * @return An object with a Java class compatible with the Class or Interface
   *    returned from the getNativeClass() method.
   *
   * @throws IllegalArgumentException If the supplied String cannot be
   *    translated.
   */
  public abstract T fromString(String str)
    throws DataException;
  
  /**
   * Translate a native representation of a value of this Type to the 
   *   canonical String representation.<P>
   *
   * @return The canonical String representation of this Type for the specified
   *    Object.
   *
   * @throws IllegalArgumentException If the supplied Object is not compatible
   *    with the Class or Interface returned from the getNativeClass() method.
   */
  public abstract String toString(T value);
  
  /**
   * Translates a DataComposite representation of a value of this Type to an 
   *   Object presenting a suitable interface. Optionally pass a context, which
   *   Types can use to recursively construct parts of a complex object.
   *   
   * @return An object with a Java class compatible with the Class or Interface
   *    returned from the getNativeClass() method.
   *
   * @throws IllegalArgumentException If the Tuple cannot be
   *    translated.
   *
   * @throws DataException If an error occurs in the translation process.
   */
  public abstract T fromData(DataComposite composite,InstanceResolver resolver)
    throws DataException;
  
  /**
   * Translates a native representation of a value of this Type to a
   *   DataComposite representation of its composition.
   *
   * @return An DataComposite representation of this Object.
   *
   * @throws IllegalArgumentException If the supplied Object is not compatible
   *    with the Class or Interface returned from the getNativeClass() method.
   *
   * @throws DataException If an error occurs in the translation process.
   */
  public abstract DataComposite toData(T object)
    throws DataException;
  
  /**
   * Get the Comparator that should be used to define orderings of values of
   *   this type.
   */
  public abstract Comparator<T> getComparator();
  
  /**
   * 
   * @return The RuleSet associated with this type.
   */
  public abstract RuleSet<Type<T>,T> getRuleSet();
  
//  { 
//    if (ruleSet!=null)
//    { return ruleSet;
//    }
//    else if (getBaseType()!=null)
//    { return getBaseType().getRuleSet();
//    }
//    return null;
//  }
  
  /**
   * Called by the TypeResolver to allow the type to recursively resolve any
   *   referenced Types. This method has no effect after it is called once
   *   by the TypeResolver.
   *   
   * May throw a RuntimeDataException if linking failed
   */
  public abstract void link();

  /**
   * Indicate whether a Type is has been linked.
   */
  public abstract boolean isLinked();
  
  public boolean getDebug()
  { return debug;
  }
  
  public void setAttributes(Attribute[] attributes)
  { this.attributes=new AttributeSet(attributes);
  }

  public Attribute getAttribute(Class<?> clazz)
  { 
    if (this.attributes!=null)
    { return attributes.getAttribute(clazz);
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public T fromXmlResource(Resource resource)
    throws DataException
  {
    try
    {
      if (isPrimitive())
      { return (T) new DataReader().readFromResource(resource,this);
      }
      else
      { 
        DataComposite data
          =(DataComposite) new DataReader()
            .readFromResource
              (resource
              ,this
              );
        return (T) data.getType().fromData(data,null);
      }
    }
    catch (SAXException x)
    { throw new DataException("Error reading data from "+resource.getURI(),x);
    }
    catch (IOException x)
    { throw new DataException("Error reading data from "+resource.getURI(),x);
    }
  }  
  
  public void toXmlResource(Resource resource,T object)
    throws DataException
  {
    try
    { new DataWriter().writeToResource(resource,toData(object));
    }
    catch (IOException x)
    { throw new DataException("Error writing data to "+resource.getURI(),x);
    }
    
  }
  
  protected RuntimeDataException newLinkException(Exception x)
  { return new RuntimeDataException("Error linking "+getURI(),x);
  }
  
  /**
   * @return This type's scheme, without linking the type as a side effect
   */
  protected abstract Scheme scheme();
  
  /**
   * Return a reference to this type that can be persisted
   * 
   * @return
   */
  public Tuple getReference()
  {
    try
    { return new ArrayTuple(new MetaType(this).getFieldSet());
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  }
}
