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

import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

import spiralcraft.data.reflect.ReflectionField;
import spiralcraft.data.reflect.ReflectionScheme;


import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.PropertySpecifier;

import java.util.HashMap;

import java.beans.PropertyDescriptor;

/**
 * A Scheme derived from an AssemblyClass definition.
 */
public class BuilderScheme
  extends ReflectionScheme
{
  
  // private final AssemblyClass assemblyClass;
  private final HashMap<String,PropertySpecifier> memberMap
    =new HashMap<String,PropertySpecifier>();
  
  public BuilderScheme(TypeResolver resolver,Type<?> type,AssemblyClass assemblyClass)
  { 
    super(resolver,type,assemblyClass.getJavaClass());
    // this.assemblyClass=assemblyClass;
    for (PropertySpecifier specifier : assemblyClass.memberIterable())
    { memberMap.put(specifier.getTargetName(),specifier);
    }
  }
  
  @Override
  protected BuilderField generateField(PropertyDescriptor prop)
    throws DataException
  { 
    BuilderField field = new BuilderField
      (resolver
      ,prop
      ,memberMap.get(prop.getName())
      );
    field.resolveType();
    return field;
  }
  
  /**
   * Copy data values from the Tuple into bean properties of the Object
   */
  @Override
  public void depersistBeanProperties(Tuple tuple,Object assembly)
    throws DataException
  {
    for (Field field: fields)
    { 
      if (field instanceof ReflectionField)
      { ((ReflectionField) field).depersistBeanProperty(tuple,assembly);
      }
    }
  }
  
  /**
   * Copy data values from the Tuple into bean properties of the Object
   */
  @Override
  public void persistBeanProperties(Object assembly,EditableTuple tuple)
    throws DataException
  {
    for (Field field: fields)
    { 
      if (field instanceof ReflectionField)
      { ((ReflectionField) field).persistBeanProperty(assembly,tuple);
      }
    }
  }
}