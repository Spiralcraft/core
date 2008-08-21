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

import java.util.List;

import java.beans.PropertyDescriptor;

import spiralcraft.data.Field;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataComposite;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;


import spiralcraft.data.reflect.ReflectionField;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.util.StaticInstanceResolver;

import spiralcraft.builder.PropertySpecifier;
import spiralcraft.builder.PropertyBinding;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;

@SuppressWarnings("unchecked") // Not genericized yet
public class BuilderField
  extends ReflectionField
{
 
  private final PropertySpecifier specifier;
  
  public BuilderField
    (TypeResolver resolver
    ,PropertyDescriptor prop
    ,PropertySpecifier specifier
    )
    throws DataException
  { 
    super(resolver,prop);
    this.specifier=specifier;
  }
  
  @Override
  public void resolveType()
    throws DataException
  {
    super.resolveType();
    if (specifier!=null)
    {
      List<AssemblyClass> contents=specifier.getContents();
      if (contents!=null && contents.size()>0)
      {
        if (getType().isAggregate())
        { 
          // XXX Figure out aggregate type
          
          // Using Object.array is safe, but not very useful
          setType(BuilderType.genericBuilderArrayType());
          
          // XXX This is dangerous
          // setType(BuilderType.canonicalType(contents.get(0)));
        }
        else
        { setType(BuilderType.canonicalType(contents.get(0)));
        }
      }
    }
  }
  
  @Override
  public void depersistBeanProperty(Tuple tuple,Object subject)
    throws DataException
  {
    Object fieldValue=getValue(tuple);
    Type<?> type=getType();

    if (fieldValue!=null) // We only depersist non-null data
    {
      
      if (fieldValue instanceof DataComposite)
      { type=((DataComposite) fieldValue).getType();
      }
      
      try
      {
        Assembly<?> assembly=(Assembly<?>) subject;
        if (specifier!=null)
        {
          PropertyBinding binding=specifier.getPropertyBinding(assembly);
          StaticInstanceResolver resolver=new StaticInstanceResolver(binding);
          
          if (type.getCoreType() instanceof BuilderType)
          { 
            if (type.isAggregate())
            {
              Assembly<?>[] valueAssemblies
                =(Assembly[]) type.fromData
                  ((DataComposite) fieldValue,resolver);
                  
              if (valueAssemblies!=null)
              { binding.replaceContents(valueAssemblies);
              }
            }
            else
            { 
              Assembly<?> valueAssembly
                =(Assembly<?>) type.fromData
                  ((DataComposite) fieldValue,resolver);
                  
              if (valueAssembly!=null)
              { binding.replaceContents(new Assembly[] {valueAssembly});
              }
            }
            
          } // Is a builder type
          else
          {
            // System.out.println("Not a builder type "+type.getCoreType());
            // Use bean method to depersist
            Object bean=assembly.get();
            super.depersistBeanProperty(tuple,bean);
          }
          
          
        } // specifier!=null
        else
        {
          if (getType().getCoreType() instanceof BuilderType)
          { 
            // Can't depersist an Assembly where there is no
            //   declared PropertySpecifier to contain it
            throw new DataException
              ("Field '"+getName()+"' must be declared in Assembly: "
              +getScheme().getType().getURI()
              +" in order to depersist."
              );
          }
          else
          {
            // We -can- depersist a bean value into a property
            //   that has no descriptor
            Object bean=assembly.get();
            super.depersistBeanProperty(tuple,bean);
          }
        }
      }
      catch (BuildException x)
      { 
        throw new DataException
          ("Error depersisting field '"+getURI()+"':"+x
          ,x
          );
      }
    } // Don't depersist null (use the null) type instead.
  }

  
  @Override
  public void persistBeanProperty(Object object,EditableTuple tuple)
    throws DataException
  {
    Assembly assembly=(Assembly) object;
    if (specifier!=null)
    {
      // Use Assembly scaffold to persist
      if (specifier.isPersistent() && assembly!=null)
      { 
        PropertyBinding binding=specifier.getPropertyBinding(assembly);
        Assembly[] assemblies=binding.getContents();
        if (assemblies!=null && assemblies.length > 0)
        {
          // We definitely have a builder subtype here
          if (getType().isAggregate())
          { 
            // Even though specifier is present, we don't have a Builder type
            //   for aggregates, so getType() will return probably a reflection
            //   type aggregate. getType() doesn't help us much.
            
            EditableArrayListAggregate aggregate
              =new EditableArrayListAggregate(getType());
            for (Assembly subAssembly: assemblies)
            { 
              Type type=BuilderType.canonicalType(subAssembly.getAssemblyClass());
              
              if (type instanceof BuilderType)
              { 
                // Narrow the data conversion
//                System.err.println("BuilderField: Adding "+type);
                aggregate.add(type.toData(subAssembly));
              }
              else
              {
                System.out.println("BuilderField: Unpacking "+subAssembly);
                // Not a builder type, and can't get type from assembly
                // Unpackage the assembly and store as a reflection type
                Object subjectBean=subAssembly.get();
                type=
                  TypeResolver.getTypeResolver().resolve
                    (ReflectionType.canonicalURI(subjectBean.getClass()));
                // Narrow the data conversion
                aggregate.add(type.toData(subjectBean));
                
              }
              
            } // for
            setValue(tuple,aggregate);
          }
          else
          { 
            Type type=BuilderType.canonicalType(assemblies[0].getAssemblyClass());
            if (type instanceof BuilderType)
            {
              setValue(tuple,type.toData(assemblies[0]));
            }
            else 
            { super.persistBeanProperty(assembly.get(),tuple);
            }
          }
        }
        else
        {    
//          System.err.println("BuilderField: persisting "+getName()+" as bean");
          Object bean=assembly.get();
          super.persistBeanProperty(bean,tuple);
        }
      } // if (specifier.isPersistent() && assembly!=null);
    } // if (specifier!=null);
    else
    {
      if (!(getType().getCoreType() instanceof BuilderType))
      { 
        Object bean=assembly.get();
        super.persistBeanProperty(bean,tuple);
      }
    }
  }
  
  @Override
  public boolean isFunctionalEquivalent(Field field)
  { 
    if (!super.isFunctionalEquivalent(field))
    { return false;
    }
    if (!(field instanceof BuilderField))
    { return false;
    }
    
//    BuilderField bfield=(BuilderField) field;

    return true;
    
  }
  
    
}