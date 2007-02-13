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
package spiralcraft.data.builder;

import java.net.URI;

import java.util.List;

import spiralcraft.util.Path;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;

public class BuilderTypeFactory
  implements TypeFactory
{
  
  public Type createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    if (!BuilderType.isApplicable(uri))
    { return null;
    }
    
    String uriString=uri.toString();
    int bangPos=uriString.indexOf(BuilderType.INNER_PATH_SEPARATOR);
    Path path;
    if (bangPos>=0)
    { 
      path=new Path(uriString.substring(bangPos+1),'/');
      uriString=uriString.substring(0,bangPos);
    }
    else
    { path=null;
    }
    
    if (path==null)
    { return new BuilderType(resolver,uri);
    }
    else
    { 
      String childName=path.lastElement();
      String parentPath=path.parentPath().format("/");
      URI parentUri;
      if (parentPath.length()>0)
      { 
        parentUri=URI.create
          (uriString+BuilderType.INNER_PATH_SEPARATOR+parentPath);
      }
      else
      { parentUri=URI.create(uriString);
      }
      Type parentType=resolver.resolve(parentUri);
      
      return new BuilderType((BuilderType) parentType,childName);
    }
    
  }
  
}