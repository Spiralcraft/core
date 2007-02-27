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
package spiralcraft.data.sax;

import java.net.URI;

import org.xml.sax.SAXException;

import java.io.IOException;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.spi.ConstructorInstanceResolver;

import spiralcraft.stream.Resource;
import spiralcraft.stream.Resolver;
import spiralcraft.stream.UnresolvableURIException;

public class XmlTypeFactory
  implements TypeFactory
{
  
  public Type createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    if (!typeExists(uri))
    { return null;
    }
    else
    { return loadType(resolver,uri);
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
    { return false;
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
  
  private Type loadType(TypeResolver resolver,URI uri)
    throws DataException
  {
    try
    {

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
          
          
      return (Type) tuple.getType().fromData(tuple,instanceResolver);    
    }
    catch (SAXException x)
    { throw new DataException(x.toString(),x);
    }
    catch (IOException x)
    { throw new DataException(x.toString(),x);
    }

  }

  
}