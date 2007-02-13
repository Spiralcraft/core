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


import spiralcraft.data.TypeResolver;

import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

import spiralcraft.data.wrapper.ReflectionScheme;
import spiralcraft.data.wrapper.ReflectionField;

import spiralcraft.data.core.FieldImpl;

import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.PropertySpecifier;

import java.util.HashMap;
import java.util.List;

import java.net.URI;

import java.beans.PropertyDescriptor;

/**
 * A Scheme derived from an AssemblyClass definition.
 */
public class BuilderScheme
  extends ReflectionScheme
{
  
  private final AssemblyClass assemblyClass;
  private final HashMap<String,PropertySpecifier> memberMap
    =new HashMap<String,PropertySpecifier>();
  
  public BuilderScheme(TypeResolver typeResolver,Type type,AssemblyClass assemblyClass)
  { 
    super(typeResolver,type,assemblyClass.getJavaClass());
    this.assemblyClass=assemblyClass;
    for (PropertySpecifier specifier : assemblyClass.memberIterable())
    { memberMap.put(specifier.getTargetName(),specifier);
    }
  }
  
  protected BuilderField generateField(PropertyDescriptor prop)
    throws DataException
  { 
    Type defaultType=null;
    try
    { defaultType=findType(prop.getPropertyType());
    }
    catch (DataException x)
    { 
      // This should NEVER happen- there always exists a Type for
      //   every java class
      x.printStackTrace();
    }
      
    BuilderField field=new BuilderField
      (prop
      ,memberMap.get(prop.getName())
      ,defaultType
      );

    return field;
  }
  
  /**
   * Copy data values from the Tuple into bean properties of the Object
   */
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