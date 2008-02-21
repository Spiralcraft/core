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
package spiralcraft.data.xml;

import java.net.URI;

import org.xml.sax.SAXException;

import java.io.IOException;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.sax.DataReader;

import spiralcraft.data.util.ConstructorInstanceResolver;

import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

import spiralcraft.util.CycleDetector;

public class XmlTypeFactory
  implements TypeFactory
{
  
  private ThreadLocal<CycleDetector<URI>> cycleDetectorRef
    =new ThreadLocal<CycleDetector<URI>>()
  {
    protected synchronized CycleDetector<URI> initialValue() {
      return new CycleDetector<URI>();
    }
  };
  
  public Type<?> createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    if (cycleDetectorRef.get().detectOrPush(uri))
    { return null;
    }
    try
    {
      if (typeExists(uri))
      { return loadType(resolver,uri);
      }
      return null;
    }
    finally
    { cycleDetectorRef.get().pop();
    }
  }
  
  private boolean typeExists(URI uri)
    throws DataException
  { 
    URI resourceUri=URI.create(uri.toString()+".type.xml");
    Resource resource=null;
    
    try
    { resource=Resolver.getInstance().resolve(resourceUri);
    }
    catch (UnresolvableURIException x)
    { 
      throw new DataException
        ("Could not resolve resource "+resourceUri+": "+x.toString()
        ,x
        );
    } 
    
    try
    { return resource.exists();
    }
    catch (IOException x)
    { 
      throw new DataException
        ("IOException checking resource "+resourceUri+": "+x.toString()
        ,x
        );
    }
  }
  
  private Type<?> loadType(TypeResolver resolver,URI uri)
    throws DataException
  {
    try
    {
//      System.err.println("XmlTypeFactory: Loading "+uri);
      Tuple tuple
        =(Tuple) new DataReader()
          .readFromURI(URI.create(uri.toString()+".type.xml")
                      ,resolver.getMetaType()
                      );
  
      InstanceResolver instanceResolver
        =new ConstructorInstanceResolver
          (new Class[] {TypeResolver.class,URI.class}
          ,new Object[] {resolver,uri}
          );
          
//      System.err.println("XmlTypeFactory: data is "+tuple.toText("  "));
      
      Type<?> type=(Type<?>) tuple.getType().fromData(tuple,instanceResolver);
//      System.err.println("XmlTypeFactory: Loaded "+uri+" = "+type.getURI());
      
      return type;
    }
    catch (SAXException x)
    { throw new DataException(x.toString(),x);
    }
    catch (IOException x)
    { throw new DataException(x.toString(),x);
    }

  }

  
}