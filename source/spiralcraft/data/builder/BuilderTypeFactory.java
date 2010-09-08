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

import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.PropertySpecifier;

import spiralcraft.builder.Assembly;

public class BuilderTypeFactory
  implements TypeFactory
{
   
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Heterogeneous factory method
  public Type createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    if (!BuilderType.isApplicable(uri))
    { return null;
    }
    // System.err.println("BuilderTypeFactory: create type "+uri);
 
    URI baseURI=TypeResolver.desuffix(uri,".assy");
    String uriString=baseURI.toString();
 
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
          (uriString+BuilderType.INNER_PATH_SEPARATOR+parentPath+".assy");
      }
      else
      { parentUri=URI.create(uriString+".assy");
      }
      BuilderType parentType=(BuilderType) resolver.<Assembly<?>>resolve(parentUri);
      
      return new BuilderType
        (parentType
        ,deriveInnerClass(parentType,childName)
        );
    }
    
  }
  
  private AssemblyClass deriveInnerClass
    (BuilderType outerType
    ,String pathElement
    )
    throws DataException
  {
    AssemblyClass targetAssemblyClass;
    String propertyName;
    String objectId;
    
    int dotPos=pathElement.indexOf(".");
    if (dotPos>=0)
    { 
      propertyName=pathElement.substring(0,dotPos);
      objectId=pathElement.substring(dotPos+1);
    }
    else
    {
      propertyName=pathElement;
      objectId=null;
    }
      
    AssemblyClass containingClass
      =outerType.getAssemblyClass();
    
    PropertySpecifier containingProperty=null;
    try
    {
      containingProperty=containingClass.getMember(propertyName);
    }
    catch (BuildException x)
    { 
      throw new DataException
        ("Error getting AssemblyClass member "+propertyName);
    }
    
    if (containingProperty==null)
    { 
      throw new DataException
        ("Creating inner type "+pathElement+" in "+outerType.toString()+": PropertySpecifier '"+propertyName+"' not found in AssemblyClass "
        +containingClass
        );
    }

    
    List<AssemblyClass> contents=containingProperty.getContents();
    if (objectId==null)
    { 
      if (contents.size()==1)
      { targetAssemblyClass=contents.get(0);
      }
      else
      {
        throw new DataException
          ("PropertySpecifier '"+propertyName+"' is ambiguous in AssemblyClass "
          +containingClass
          );
      }
    }
    else
    { 
      AssemblyClass target=null;
      for (AssemblyClass candidate: contents)
      {
        if (objectId.equals(candidate.getId()))
        { 
          target=candidate;
          break;
        }
      }
      if (target!=null)
      { targetAssemblyClass=target;
      }
      else
      {
        throw new DataException
          ("AssemblyClass with id '"+objectId+"'"
          +" in property '"+propertyName+"' "
          +"not found in AssemblyClass "
          +containingClass
          );
      }
      
      
    }
    return targetAssemblyClass;
  }
  
}