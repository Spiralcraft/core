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

import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataException;

import spiralcraft.data.sax.DataReader;
import spiralcraft.data.sax.DataWriter;

import spiralcraft.registry.Registrant;
import spiralcraft.registry.RegistryNode;

import spiralcraft.stream.Resource;
import spiralcraft.stream.Resolver;

/**
 * A persistent object represented in an XML based portable data format.
 *
 * Persistent objects can have their state saved and restored at runtime.
 *
 * An instance of a persistent object is tied to its non-volatile representation
 *   in a storage medium.
 */
public abstract class AbstractXmlObject<T,C>
  implements Registrant,PersistentReference
{

  protected URI instanceURI;
  protected URI typeURI;
  protected Type<C> type;
  protected C instance;
  protected RegistryNode registryNode;
  
  /**
   * Construct an XmlObject from the given Type resource and instance
   *   data resource.
   * 
   * If the typeURI is not specifed, the instanceURI must be specified. The Type will
   *   be read from the data resource.
   *   
   * If the instanceURI is not specified, or does not exist, the typeURI must be
   *   specified. An appropriate object will be instantiated as per the specified Type.
   *   If the instanceURI is specifed but does not exist, the save() method will create
   *   it and store persistent properties from the object.
   *   
   * The subclass extending this should call load() once construction is complete.
   */
  protected AbstractXmlObject(URI typeURI,URI instanceURI)
    throws PersistenceException
  { 
    this.typeURI=typeURI;
    this.instanceURI=instanceURI;

  }
  
  public void setResourceUri(URI instanceURI)
  { this.instanceURI=instanceURI;
  }
  
  /**
   * Verify that the Type is appropriate for a given implementation
   */
  protected abstract void verifyType(Type type)
    throws DataException;
  
  /**
   * Create a new default instance of the specified type
   */
  protected abstract C newInstance()
    throws DataException;

  /**
   *@return The Java object referred to and activated by this XmlObject
   */
  public abstract T get();

  @SuppressWarnings("unchecked") // Narrowing from Assembly to Assembly<T>
  public void load()
    throws PersistenceException
  {
    try
    { 
      if (typeURI!=null)
      {
        type=TypeResolver.getTypeResolver().<C>resolve(typeURI);
        verifyType(type);
        
        if (instanceURI==null)
        { instance = newInstance();
        }
      }

      if (instanceURI!=null)
      {
        DataReader reader=new DataReader();
        Resource resource=Resolver.getInstance().resolve(instanceURI);
        if (!resource.exists())
        { instance=newInstance();
        }
        else
        {
        
          Tuple tuple = (Tuple) reader.readFromResource
            (resource
            ,type
            );

          Type actualType=tuple.getType();
          verifyType(actualType);

          instance = (C) actualType.fromData(tuple,null);
          type=actualType; 
        }
      }
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

  public void save()
    throws PersistenceException
  { 
    if (instanceURI!=null && type!=null)
    {
      DataWriter writer=new DataWriter();
      try
      {
        // Re-evaluate type?
        Tuple tuple=(Tuple) type.toData(instance);
        writer.writeToURI(instanceURI,tuple);
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
    if (instance instanceof Registrant)
    { ((Registrant) instance).register(registryNode.createChild("instance"));
    }
    else
    { registryNode.registerInstance(instance.getClass(),instance);
    }    
    registryNode.registerInstance(PersistentReference.class,this);
  }
  
}
