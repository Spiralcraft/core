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

import java.net.URI; 


/**
 * Describes the data type of a data element.<BR>
 */
public interface Type<T>
{  
  /**
   * The TypeResolver which instantiated this particular Type.
   */
  TypeResolver getTypeResolver();

  /**
   * The canonical URI for this type.
   */
  URI getURI();
  
  /**
   * The Type used to describe Type objects of this Type, to support the
   *   defining of Type extensions using data.
   */
  Type getMetaType();
  
  /**
   * The public Java class or interface used to programatically access or
   *   manipulate this data element.
   *
   * @return A Class Or Interface, or null if the data element should be
   *   manipulated as Tuple data.
   */
  Class<?> getNativeClass();

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
  boolean isPrimitive();
  
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   */
  Scheme getScheme();
  
  /**
   * @return Whether this Type is an aggregate (array or collection) of another
   *   type.
   */
  boolean isAggregate();
   
  /**
   * @return The Type of data aggregated by this aggregate Type, or null if 
   *   this is not an aggregate Type.
   */
  Type getContentType();
  
  /**
   * @return The first non-aggregate returned by recursive calls to
   *   getContentType(), or this type if not an aggregate.
   */
  Type getCoreType();
  
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
  Type getArchetype();
  
  /**
   * @return Whether this Type or any of its archetypes (recursively) is the
   *   the specified Type.
   */
  boolean hasArchetype(Type type);
  
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
  Type getBaseType();
  
  
  /**
   * @return Whether this Type or any of its base Types (recursively) is the
   *   the specified Type.
   */
  boolean hasBaseType(Type type);
  
  
  /**
   * Indicate whether Objects of this type can be encoded to and decoded from
   *   String form. This will only return true if getNativeClass()!=null.
   *
   * @return Whether Objects of this type can be represented as a String.
   */
  boolean isStringEncodable();
  
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
  T fromString(String str)
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
  String toString(T value);
  
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
  T fromData(DataComposite composite,InstanceResolver resolver)
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
  DataComposite toData(T object)
    throws DataException;
  
  
  /**
   * Determine whether a value is appropriate for the native type and 
   *   other contraints of this Type.
   *
   * @return null, if the value is valid for this Type, or a ValidationResult
   *   containing specifics of the failure.
   */
  ValidationResult validate(Object value);
  
  /**
   * Called by the TypeResolver to allow the type to recursively resolve any
   *   referenced Types. This method has no effect after it is called once
   *   by the TypeResolver.
   */
  void link()
    throws DataException;
}
