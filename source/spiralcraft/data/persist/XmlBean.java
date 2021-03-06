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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import spiralcraft.data.Type;
import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.data.DataException;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.EditableArrayTuple;


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
public class XmlBean<T>
  extends AbstractXmlObject<T,T>
{
  /**
   * Construct an XmlObject of the given Class read from the specified instance
   *   data resource.
   *
   * The instanceURI references a data resource which contains the instance data
   *   (persistent properties) for the specified Class. This data will be 
   *   applied
   *   to the object after it is instantiated, and will be transferred
   *   from the object to the resource as a result of the save() method.
   * 
   * If the Class is not specified, the instanceURI must be specified. The Class
   *   will be inferred from the data resource.
   *   
   * If the instanceURI is not specified, or does not exist, the Class must be
   *   specified. A new instance of the Java Class will be created. If the
   *   instanceURI is specified but does not exist, the save() method will 
   *   create it and store persistent properties from the object.
   */
  public XmlBean(Class<T> clazz,URI instanceURI)
    throws PersistenceException
  {
    super(clazz!=null?ReflectionType.canonicalURI(clazz):null,instanceURI);
    load();
    if (instance instanceof Declarable)
    { 
      ((Declarable) instance).setDeclarationInfo
        (new DeclarationInfo(((Declarable) instance).getDeclarationInfo(),null,instanceURI));
    }
  }
  
  /**
   * Construct an XmlObject from the given Type resource and instance
   *   data resource.
   *
   * The typeURI references a ReflectionType which refers to the Java Class that 
   *   should be instantiated. 
   * 
   * The instanceURI references a data resource which contains the instance data
   *   (persistent properties) for the specified type. This data will be applied
   *   to the object after it is instatiated, and will be transferred
   *   from the object to the resource as a result of the save() method.
   * 
   * If the typeURI is not specifed, the instanceURI must be specified. The Type will
   *   be read from the data resource.
   *   
   * If the instanceURI is not specified, or does not exist, the typeURI must be
   *   specified. A new instance of the Java class will be created. If the
   *   instanceURI is specifed but does not exist, the save() method will create it
   *   and store persistent properties from the object.
   */
  public XmlBean(URI typeURI,URI instanceURI)
    throws PersistenceException
  { 
    super(typeURI,instanceURI);
    load();
    if (instance instanceof Declarable)
    { 
      ((Declarable) instance).setDeclarationInfo
        (new DeclarationInfo(((Declarable) instance).getDeclarationInfo(),null,instanceURI));
    }
  }
  

  @Override
  protected void verifyType(Type<?> type)
  {
  }
  
  @Override
  protected T newInstance()
    throws DataException
  { 
    
    if (type.isDataEncodable())
    { return type.fromData(new EditableArrayTuple(type),null);
    }
    else
    {
      try
      { return type.getNativeClass().getDeclaredConstructor().newInstance();
      }
      catch (InstantiationException x)
      { throw new DataException("Error instantiating "+type.getNativeClass()+": "+x,x);
      }
      catch (IllegalAccessException x)
      { throw new DataException("Error instantiating "+type.getNativeClass()+": "+x,x);
      }
      catch (NoSuchMethodException x)
      { throw new DataException("Error instantiating "+type.getNativeClass()+": "+x,x);
      }
      catch (InvocationTargetException x)
      { throw new DataException("Error instantiating "+type.getNativeClass()+": "+x,x);
      }
    }
    
  }
  
  @Override
  public T get()
  { return instance;
  }
  
  @Override
  public void set(T instance)
  { this.instance=instance;
  }
}
