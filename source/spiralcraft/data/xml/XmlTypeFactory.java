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

import spiralcraft.data.DataComposite;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;

import spiralcraft.data.access.DataFactory;
import spiralcraft.data.core.MetaType;
import spiralcraft.data.sax.DataReader;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.EditableArrayTuple;


import spiralcraft.data.util.ConstructorInstanceResolver;
import spiralcraft.data.util.StaticInstanceResolver;
import spiralcraft.log.ClassLogger;

import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

import spiralcraft.util.CycleDetector;

public class XmlTypeFactory
  implements TypeFactory
{
  
  @SuppressWarnings("unused")
  private static final ClassLogger log=new ClassLogger(XmlTypeFactory.class);
  
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
  
  @SuppressWarnings("unchecked")
  private synchronized Type<?> loadType(final TypeResolver resolver,final URI uri)
    throws DataException
  {
//    log.fine("loadType "+uri);
    final StaticInstanceResolver instanceResolver
      =new StaticInstanceResolver(null);
    
    boolean error=false;
    
    try
    {
      DataReader dataReader=new DataReader();
      dataReader.setDataFactory
        (
          new DataFactory()
          {

            boolean first=true;
            
            @Override
            public DataComposite create(
              Type type)
              throws DataException
            { 
              DataComposite composite;
              if (type.isAggregate())
              { composite=new EditableArrayListAggregate(type);
              }
              else
              { composite=new EditableArrayTuple(type.getScheme());
              }
              
              if (first)
              {
                // XXX Must communicate with meta-type specifically
                //  if it is being extended.
                
                first=false;
                Type instance;
                if (type instanceof MetaType)
                { 
                  // Need to do this explicitly, because MetaType thinks
                  //   that empty data is simply a type reference, and at
                  //   this point we don't know if data is empty
                  instance=((MetaType) type).newSubtype(composite,uri);
                }
                else
                {
                  instance=((Type<Type>) type).fromData
                    (composite
                    ,new ConstructorInstanceResolver
                      (new Class[] {TypeResolver.class,URI.class}
                      ,new Object[] {resolver,uri}
                      )
                    );
                }
//                log.fine("Preregistering "+instance);
                // Pre-instantiate so rest of XML can resolve Type
                instanceResolver
                  .setInstance
                    (instance
                    );
                resolver.register
                  (instance.getURI()
                  ,instance
                  );
                  
              }
              return composite;
            }
          }
        );
      
//      System.err.println("XmlTypeFactory: Loading "+uri);
      Tuple tuple
        =(Tuple) dataReader
          .readFromURI(URI.create(uri.toString()+".type.xml")
                      ,resolver.getMetaType()
                      );
          
//      System.err.println("XmlTypeFactory: data is "+tuple.toText("  "));
      
      Type<?> type=(Type<?>) tuple.getType().fromData(tuple,instanceResolver);
//      System.err.println("XmlTypeFactory: Loaded "+uri+" = "+type.getURI());

      if (type!=instanceResolver.getInstance())
      { 
        error=true;
        throw new DataException
          ("XmlTypeFactory error "+type+" != "+instanceResolver.getInstance());
        
      }
      log.fine("Linking "+type);
      type.link();
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
      throw new DataException("Error loading XML Type "+uri,x);
    }
    catch (IOException x)
    { 
      error=true;
      throw new DataException(x.toString(),x);
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