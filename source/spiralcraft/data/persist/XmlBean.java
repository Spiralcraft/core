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

import java.io.IOException;

import org.xml.sax.SAXException;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataException;

import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.DataWriter;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

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
  implements Registrant,PersistentReference
{

  private URI instanceURI;
  private URI typeURI;
  private Type<T> type;
  private RegistryNode registryNode;
  private T instance;
  
  /**
   * Construct an XmlObject from the given Type resource and instance
   *   data resource.
   *
   * The typeURI references a BuilderType (spiralcraft.data.builder.BuilderType),
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
  public XmlBean(URI typeURI,URI instanceURI)
    throws PersistenceException
  { 
    this.typeURI=typeURI;
    this.instanceURI=instanceURI;
    
    try
    { load();
    }
    catch (DataException x)
    { 
      throw new PersistenceException
        ("Error instantiating XmlObject: "+x.toString()
        ,x
        );
    }
    catch (SAXException x)
    { 
      throw new PersistenceException
        ("Error parsing "+instanceURI+": "+x.toString()
        ,x
        );
    }
    catch (IOException x)
    {
      throw new PersistenceException
        ("Error parsing "+instanceURI+": "+x.toString()
        ,x
        );
    }
  }
  
  public void setResourceUri(URI instanceURI)
  { this.instanceURI=instanceURI;
  }
  
  
  @SuppressWarnings("unchecked") // Narrow from Tuple.getType()
  private void load()
    throws DataException,SAXException,IOException
  {
    if (typeURI!=null)
    { type=TypeResolver.getTypeResolver().<T>resolve(typeURI);
    }
        
    if (instanceURI!=null)
    {
      DataReader reader=new DataReader();
      DataComposite data = (DataComposite) reader.readFromURI
        (instanceURI
        ,type
        );
      
      Type<T> actualType=(Type<T>) data.getType();
      
      instance = actualType.fromData(data,null); 
      type=actualType; 
    }
    
  }

  public void save()
    throws PersistenceException
  { 
    if (instanceURI!=null && type!=null)
    {
      DataWriter writer=new DataWriter();
      try
      {
        DataComposite data=type.toData(instance);
        // System.out.println(tuple.toText("|  "));
        writer.writeToURI(instanceURI,data);
      }
      catch (IOException x)
      { throw new PersistenceException("Error writing "+instanceURI+": "+x,x);
      }
      catch (DataException x)
      { throw new PersistenceException("Error writing "+instanceURI+": "+x,x);
      }
    }
    
  }
  
  public void register(RegistryNode node)
  {
    registryNode=node;
    registryNode.registerInstance(PersistentReference.class,this);
    if (instance instanceof Registrant)
    { ((Registrant) instance).register(registryNode.createChild("instance"));
    }
    else
    { registryNode.registerInstance(instance.getClass(),instance);
    }
  }
  
  /**
   *@return The Java object referred to and activated by this XmlObject
   */
  public T get()
  { return instance;
  }
}
