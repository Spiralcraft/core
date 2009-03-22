//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.lang;

import java.net.URI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.log.ClassLog;
import spiralcraft.util.ClassLoaderLocal;

/**
 * <p>Maps a URI space into some implementation defined type model, and
 *   provides a Reflector instance for each type in the model.
 * </p>
 */
public abstract class TypeModel
{
  protected static final ClassLog clog=ClassLog.getInstance(TypeModel.class);
  
  private static final ClassLoaderLocal<List<TypeModel>> registeredModels
    =new ClassLoaderLocal<List<TypeModel>>();
  
  /** 
   * Register the Type model in the ClassLoader that loaded it
   * 
   * @param model
   */
  static void register(TypeModel model)
  {
    List<TypeModel> ret
      =registeredModels.getInstance(model.getClass().getClassLoader());
    if (ret==null)
    {
      ret=new ArrayList<TypeModel>();
      registeredModels.setInstance(model.getClass().getClassLoader(),ret);      
    }
//    clog.fine("Registering "+model);
    ret.add(model);
  }
  
  
  
  public static TypeModel[] getRegisteredModels()
  {
    List<TypeModel> models=new LinkedList<TypeModel>();
    for (List<TypeModel> someModels: registeredModels.getAllContextInstances())
    { models.addAll(someModels);
    }
    return models.toArray(new TypeModel[models.size()]);
    
  }
  
  
  { register(this);
  }
  
  /**
   * <p>A unique name associated with this type model.
   * </p>
   * 
   * <p>The names "java" and "spiralcraft.data" are already used.
   * </p>
   * @return
   */
  public abstract String getModelId();
  
  /**
   * <p>Find the type (via a Reflector instance) specified by the given URI.
   * </p>
   * 
   * @param <X> The native class/interface of the object provided by the
   *               reflector. 
   * @param typeURI A URI which identifies the type
   * @return The Reflector instance which represents the type.
   * @throws BindException if an error occurred resolving the type
   */
  public abstract <X> Reflector<X> findType(URI typeURI)
    throws BindException;
}
