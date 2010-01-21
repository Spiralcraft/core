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
package spiralcraft.data.reflect;

import java.net.URI;


import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.Managable;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * A Type which references a Java class either directly or through a
 *   spiralcraft.builder.AssemblyClass.
 * 
 * @author mike
 *
 */
public class ReflectionTypeFactory
  implements TypeFactory
{
  
  private static final ClassLog log
    =ClassLog.getInstance(ReflectionTypeFactory.class);
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(ReflectionTypeFactory.class,null);
  
  @SuppressWarnings("unchecked")
  public Type<?> createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    
    final ClassLoader loader=resolver.getClassLoader();
    final ClassLoader oldClassLoader=Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(loader);
    try
    {
      // Determine if this is a situation where we augment a reflection type
      //   with an AssemblyType chain 
      
      AssemblyClass assemblyClass=null;
      try
      { assemblyClass=AssemblyLoader.getInstance().findAssemblyClass(uri);
      }
      catch (BuildException x)
      {
      }
      
      if (assemblyClass!=null)
      { 
        Class javaClass=assemblyClass.getJavaClass();
        if (!ReflectionType.isManaged(javaClass)
            && !Type.class.isAssignableFrom(javaClass)
            && ( 
                 assemblyClass.getBaseClass()!=null 
                 || javaClass.isAnnotationPresent(Managable.class)
               )
           )
        { 
          // Exclude:
          //   Managed types which are always mapped to specific xxxType
          //     classes
          //   Type types themselves, which are explicitly managed
          //   Non-managable objects where the AssemblyClass is simply
          //     a default wrapper.
          
          
          if (debugLevel.canLog(Level.DEBUG))
          { 
            log.log
              (Level.DEBUG,"Created AssemblyType for "+uri+" : "+assemblyClass);
          }
          return new AssemblyType(resolver,uri,assemblyClass);
        }
      }
      else
      { 
        if (debugLevel.canLog(Level.DEBUG))
        { log.log(Level.DEBUG,"Got null resolving AssemblyClass "+uri);
        }
      }
    }
    finally
    { Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
    
    
    
    String path=uri.getPath().substring(1);

    String className
        =path.replace('/','.').replace(ReflectionType.INNER_CLASS_SEPARATOR,"$");
    
    
    Class<Object> clazz=null;
    try 
    { clazz = (Class<Object>) loader.loadClass(className);
    }
    catch (ClassNotFoundException x)
    { return null;
    }
    
    return new ReflectionType<Object>(resolver,uri,clazz);
  }

  
}