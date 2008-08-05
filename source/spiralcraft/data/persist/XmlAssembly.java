//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.data.persist;

import java.net.URI;

import spiralcraft.data.Type;
import spiralcraft.data.DataException;

import spiralcraft.data.builder.BuilderType;
import spiralcraft.lang.AccessException;


import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

/**
 * A persistent object represented in an XML based portable data format.
 *
 * A persistent object is backed by an AssemblyClass, which
 *   provides some basic structure and metadata with which to interpret the
 *   XML based data.
 *
 * Persistent objects can have their state saved and restored at runtime.
 *
 * An instance of a persistent object is tied to its non-volatile representation
 *   in a storage medium.
 */
@SuppressWarnings("unchecked") // BuilderType is not genericized
public class XmlAssembly<Treferent>
  extends AbstractXmlObject<Treferent,Assembly>
{
  
  /**
   * Construct an XmlObject from the given Type resource and instance
   *   data resource.
   *
   * The typeURI references a spiralcraft.data.builder.BuilderType,
   *   which refers to the spiralcraft.builder.AssemblyClass that should be 
   *   instantiated to create the target object. The AssemblyClass referred to
   *   is defined in <TypeURI>.assembly.xml
   * 
   * The instanceURI references a data resource which contains the instance data
   *   (persistent properties) for the specified type. This data will be applied
   *   to the object after it the Assembly is instatiated, and will be transferred
   *   from the object to the resource as a result of the save() method.
   * 
   * If the typeURI is not specifed, the instanceURI must be specified. The Type will
   *   be read from the data resource.
   *   
   * If the instanceURI is not specified, or does not exist, the typeURI must be
   *   specified. An Assembly will be instantiated as per the BuilderType. If the
   *   instanceURI is specifed but does not exist, the save() method will create it
   *   and store persistent properties from the object.
   */
  public XmlAssembly(URI typeURI,URI instanceURI)
    throws PersistenceException
  { 
    super(typeURI,instanceURI);
    load();
  }
  

  @Override
  protected void verifyType(Type type)
    throws DataException
  {
    if (!(type instanceof BuilderType))
    { 
      throw new DataException
        (typeURI.toString()+" does not reference an AssemblyClass"
        );
    }
  }
  
  @Override
  protected Assembly newInstance()
    throws DataException
  { 
    try
    { return ((BuilderType) (Type) type).newAssembly(null);
    }
    catch (BuildException x)
    { throw new DataException("Error instantiating assembly "+typeURI+": "+x,x);
    }
  }
  
  public Assembly<Treferent> getAssembly()
  { return instance;
  }
  
  /**
   *@return The Java object referred to and activated by this XmlObject
   */
  @Override
  public Treferent get()
  { return (Treferent) instance.get();
  }

  @Override
  public void set(
    Treferent object)
  { throw new AccessException("Cannot change contents of Assembly");
    // TODO Auto-generated method stub
    
  }






}
