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

import spiralcraft.data.EditableTuple;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;

import spiralcraft.data.reflect.ReflectionField;
import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.data.core.SchemeImpl;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.PropertySpecifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A Scheme derived from an AssemblyClass definition.
 */
public class BuilderScheme
  extends SchemeImpl
{
  private static final ClassLog log=ClassLog.getInstance(BuilderScheme.class);
  
  private final HashMap<String,PropertySpecifier> memberMap
    =new HashMap<String,PropertySpecifier>();
  
  private final AssemblyClass assemblyClass;
  private final ReflectionType<?> reflectionType;
  
  public BuilderScheme
    (Type<?> type
    ,AssemblyClass assemblyClass
    )
    throws DataException
  { 
    this.type=type;
    this.assemblyClass=assemblyClass;
    Type<?> nativeTypeWrapper
      =ReflectionType.canonicalType(assemblyClass.getJavaClass());
    if (nativeTypeWrapper!=null && nativeTypeWrapper instanceof ReflectionType<?>)
    { reflectionType=(ReflectionType<?>) nativeTypeWrapper;
    }
    else
    { reflectionType=null;
    }

    if (assemblyClass.localMemberIterable()!=null)
    {
      for (PropertySpecifier specifier : assemblyClass.localMemberIterable())
      { memberMap.put(specifier.getTargetName(),specifier);
      }
    }
  }
  
  /**
   * Call after creating Scheme to populate fields
   */
  public void addFields()
    throws DataException
  {
    List<? extends BuilderField> fieldList
      =generateFields(assemblyClass);

    for (BuilderField field: fieldList)
    { addField(field);
    }
    
  }
  
  
  protected List<? extends BuilderField>
    generateFields(AssemblyClass assemblyClass)
      throws DataException
  {
    List<BuilderField> fieldList=new ArrayList<BuilderField>();
    
    if (reflectionType!=null)
    {
      for (Field<?> field:reflectionType.getFieldSet().fieldIterable())
      { 
        try
        { 
          assemblyClass.getMember(field.getName());
        }
        catch (BuildException x)
        { throw new DataException("Error getting member "+field.getName(),x);
        }
      }
    }
    
    for (PropertySpecifier prop : assemblyClass.localMemberIterable())
    { 
      try
      { 
        BuilderField field=generateField(prop);
        fieldList.add(field);
      }
      catch (DataException x)
      { log.log(Level.WARNING,"Ignoring builder property on exception ",x);
      }
    }
    return fieldList;
  }
  
  protected BuilderField generateField(PropertySpecifier prop)
    throws DataException
  { 
    
    BuilderField field = new BuilderField
      (prop
      ,reflectionType!=null
        ?(ReflectionField<?>) reflectionType.getField(prop.getTargetName())
        :null
      );
    field.resolveType();

    if (type.getDebug())
    { log.fine
        ("Generated "+prop+" "+prop.getTargetName()+" = "
          +field.getType().getURI()
        );
    }
    return field;
  }
  
  /**
   * Copy bean properties of the Object into the Tuple
   */
  public void persistBeanProperties(Assembly<?> bean,EditableTuple tuple)
    throws DataException
  {
    for (Field<?> field: fields)
    { 
      if (field instanceof BuilderField)
      { ((BuilderField) field).persistBeanProperty(bean,tuple);
      }
    }
  }
  
  
  /**
   * Copy data values from the Tuple into bean properties of the Object
   */
  public void depersistBeanProperties(Tuple tuple,Object bean)
    throws DataException
  {
    for (Field<?> field: fields)
    { 
      if (field instanceof BuilderField)
      { ((BuilderField) field).depersistBeanProperty(tuple,bean);
      }
    }
  }  
  
}