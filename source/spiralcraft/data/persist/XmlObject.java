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

import spiralcraft.data.builder.BuilderType;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

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
public class XmlObject
  implements Registrant,PersistentReference
{

  private URI resourceUri;
  private URI typeUri;
  private Type type;
  private Assembly assembly;
  private RegistryNode registryNode;
  
  /**
   * Construct an XmlObject from a URI, that is expected to conform to the
   *   datatype specified by typeUri.
   *
   * The URI points to a resource in spiralcraft.data XML format, which
   *   contains instance data for the specified object.
   */
  public XmlObject(URI resourceUri,URI typeUri)
    throws PersistenceException
  { 
    this.resourceUri=resourceUri;
    this.typeUri=typeUri;
    try
    { load();
    }
    catch (BuildException x)
    { 
      throw new PersistenceException
        ("Error instantiating XmlObject: "+x.toString()
        ,x
        );
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
        ("Error parsing "+resourceUri+": "+x.toString()
        ,x
        );
    }
    catch (IOException x)
    {
      throw new PersistenceException
        ("Error parsing "+resourceUri+": "+x.toString()
        ,x
        );
    }
  }
  
  public void setResourceUri(URI resourceUri)
  { this.resourceUri=resourceUri;
  }
  
  private void load()
    throws BuildException,DataException,SAXException,IOException
  {
    if (typeUri!=null)
    {
      type=TypeResolver.getTypeResolver().resolve(typeUri);
      if (!(type instanceof BuilderType))
      { 
        throw new DataException
          (typeUri.toString()+" does not reference an AssemblyClass"
          );
      }
      
      if (resourceUri==null)
      { assembly = ((BuilderType) type).newAssembly(null);
      }
    }
    
    if (resourceUri!=null)
    {
      DataReader reader=new DataReader();
      Tuple tuple = (Tuple) reader.readFromUri
        (resourceUri
        ,type
        );
      
      Type actualType=tuple.getScheme().getType();
      
      assembly = (Assembly) actualType.fromData(tuple,null); 
      type=actualType; 
    }
    
  }

  public void save()
    throws PersistenceException
  { 
    if (resourceUri!=null && type!=null)
    {
      DataWriter writer=new DataWriter();
      try
      {
        writer.writeObjectToUri(resourceUri,type,assembly);
      }
      catch (IOException x)
      { throw new PersistenceException("Error writing "+resourceUri+": "+x,x);
      }
      catch (DataException x)
      { throw new PersistenceException("Error writing "+resourceUri+": "+x,x);
      }
    }
    
  }
  
  public void register(RegistryNode node)
  {
    registryNode=node;
    registryNode.registerInstance(PersistentReference.class,this);
    assembly.register(registryNode.createChild("instance"));
  }
  
  public Object get()
  { return assembly.getSubject().get();
  }
}
