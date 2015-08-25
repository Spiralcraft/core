//
// Copyright (c) 2015 Michael Toth
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
import spiralcraft.data.DataException;
import spiralcraft.data.core.Prototype;
import spiralcraft.data.sax.DataReader;


import spiralcraft.data.util.StaticInstanceResolver;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;
import spiralcraft.util.CycleDetector;
import spiralcraft.util.refpool.URIPool;

/**
 * A Prototype is a type that is created from a predefined object. Creating
 *   a new object from a Prototype essentially creates a copy of the 
 *   predefined object.
 * 
 * @author mike
 *
 */
public class XmlPrototypeFactory
  implements TypeFactory
{
  

  private static final ClassLog log=ClassLog.getInstance(XmlTypeFactory.class);
  private final Level debugLevel
    =ClassLog.getInitialDebugLevel(XmlTypeFactory.class,Level.INFO);
  
  private boolean debug=false;
  
  private ThreadLocal<CycleDetector<URI>> cycleDetectorRef
    =new ThreadLocal<CycleDetector<URI>>()
  {
    @Override
    protected synchronized CycleDetector<URI> initialValue() {
      return new CycleDetector<URI>();
    }
  };
  
    
  
  @Override
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
    URI resourceUri=URIPool.create(uri.toString()+".proto.xml");
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
    { 
      if (resource.exists())
      {
        if (debug || debugLevel.isFine())
        { log.fine(uri+" exists");
        }
        return true;
      }
      else
      {
        if (debug || debugLevel.isFine())
        { log.fine(uri+" does not exist");
        }
        return false;
      }
    }
    catch (IOException x)
    { 
      throw new DataException
        ("IOException checking resource "+resourceUri+": "+x.toString()
        ,x
        );
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private synchronized Type<?> loadType(final TypeResolver resolver,final URI uri)
    throws DataException
  {
//    log.fine("loadType "+uri);
    
    // Hold the pre-generated type instance to be registered to allow
    //   for cyclic references
    final StaticInstanceResolver instanceResolver
      =new StaticInstanceResolver(null);
    
    boolean error=false;
    
    try
    {
      DataReader dataReader=new DataReader();

      
      Object object=dataReader
          .readFromURI(URIPool.create(uri.toString()+".proto.xml")
                      ,null
                      );
          
      Type<?> type=Prototype.create(resolver,uri,object);

      return type;
    }
    catch (DataException x)
    {
      error=true;
      throw x;
    }
    catch (SAXException x)
    { 
      error=true;
      throw new DataException("Error loading XML prototype for uri '"+uri+"'",x);
    }
    catch (IOException x)
    { 
      error=true;
      throw new DataException("Error loading XML prototype for '"+uri+"'",x);
    }
    finally
    {
      if (error && instanceResolver.getInstance()!=null)
      { 
        resolver.unregister
          (((Type<?>)instanceResolver.getInstance()).getURI()
          ,((Type<?>)instanceResolver.getInstance())
          );
      }
    }

  }

}