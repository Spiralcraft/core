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


import java.lang.reflect.InvocationTargetException;

import spiralcraft.data.Field;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataComposite;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

import spiralcraft.data.reflect.ReflectionField;
import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.data.core.FieldImpl;

import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.util.StaticInstanceResolver;

import spiralcraft.log.ClassLog;

import spiralcraft.builder.PropertySpecifier;
import spiralcraft.builder.PropertyBinding;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

@SuppressWarnings({ "unchecked", "rawtypes" }) // Not genericized yet
public class BuilderField
  extends FieldImpl
{
  private static final ClassLog log=ClassLog.getInstance(BuilderField.class);
 
  private final PropertySpecifier specifier;
  private final ReflectionField reflectionField;
  
  BuilderField
    (PropertySpecifier specifier
    ,ReflectionField reflectionField
    )
    throws DataException
  { 
    this.specifier=specifier;
    this.reflectionField=reflectionField;
    setName(specifier.getTargetName());
    try
    {
      
      if (debug && specifier.getPropertyType()==Object.class)
      { 
        log.debug("Property "
                  +reflectionField.getScheme().getType().getNativeClass()
                    +"."+specifier.getTargetName()+" has Object type");
      }
      
      Type type=BuilderType.canonicalType(specifier);
      if (type==null)
      { throw new DataException("No builder type for "+specifier);
      }
      else
      { setType(type);
      }
    }
    catch (BuildException x)
    { throw new DataException("Error resolving property",x);
    }
    catch (DataException x)
    { 
      log.fine(""+specifier+"  "+reflectionField);
      throw x;
    }
  }
  
  @Override
  public void resolveType()
    throws DataException
  {
    super.resolveType();
    if (specifier!=null)
    { //setType(BuilderType.canonicalType(specifier));
//      
//      List<AssemblyClass> contents=specifier.getContents();
//      if (contents!=null && contents.size()>0)
//      {
//        if (getType().isAggregate())
//        { 
//          // XXX Figure out aggregate type
//          
//          // Using Object.array is safe, but not very useful
//          setType(BuilderType.genericBuilderArrayType());
//          
//          // XXX This is dangerous
//          // setType(BuilderType.canonicalType(contents.get(0)));
//        }
//        else
//        { setType(BuilderType.canonicalType(contents.get(0)));
//        }
//      }
    }

  }
  
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
            reflectionField.writeValueToBean(getValue(tuple),bean);
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
            reflectionField.writeValueToBean(getValue(tuple),bean);
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

  
  public void persistBeanProperty(Assembly<?> assembly,EditableTuple tuple)
    throws DataException
  {
    if (specifier!=null)
    {
      // Use Assembly scaffold to persist
      if (specifier.isPersistent() && assembly!=null)
      { 
        // log.fine("Persisting "+getURI()+" "+specifier+" "+assembly);
        
        
        if (getType().getCoreType() instanceof BuilderType)
        {
          PropertyBinding binding=specifier.getPropertyBinding(assembly);
          Assembly[] assemblies=binding.pullContents();
          if (assemblies!=null && assemblies.length > 0)
          {
            // log.fine("Read "+assemblies);
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
              // not an aggregate
            
              Type type
                =BuilderType.canonicalType(assemblies[0].getAssemblyClass());
              if (type instanceof BuilderType)
              {
                setValue(tuple,type.toData(assemblies[0]));
              }
              else 
              { persistPropertyUsingReflection(assembly,tuple);
              }
            }
          } // contents not empty
          else 
          {  
            // Empty contents really do mean nothing to persist
           
            // System.err.println("BuilderField: persisting "+getName()+" as bean");
            // Can't do this- we must use assy container to persist
            // persistPropertyUsingReflection(assembly,tuple);
          }
        } // (getType() instanceof BuilderType)
        else 
        {
          // Not a builder type
          persistPropertyUsingReflection(assembly,tuple);
          
        }
      } // if (specifier.isPersistent() && assembly!=null);
      else
      {
        if (debug && !specifier.isPersistent())
        { 
          log.fine
            (specifier.getTargetName()+" ("+getURI()+") is not persistent");
        }
      }
    } // if (specifier!=null && !specifier.persistAsBean())
    else
    {
      // 2009-04-30 miketoth
      //
      // Don't try to persist anything without a specifier
      //
//      if ( (specifier==null || specifier.isPersistent())
//          && !(getType().getCoreType() instanceof BuilderType)
//         )
//      { 
//        log.fine("Persisting non-builder type "+getType().getCoreType().getURI());
//        persistPropertyUsingReflection(assembly,tuple);
//      }
    }
  }
  
  private void persistPropertyUsingReflection
    (Assembly assembly,EditableTuple tuple)
    throws DataException
  {
    if (reflectionField.getReadMethod()==null)
    { 
      log.fine("No read method "+reflectionField.getURI());
      return;
    }
    
    try
    {
      Object val
        =reflectionField.getReadMethod().invoke
          (assembly.get(),(Object[]) null);
      
      
      if (getType().isPrimitive())
      {
        setValue
          (tuple
          ,val
          );
      }
      else
      { 
        if (val!=null)
        {
          setValue
            (tuple
            ,((Type<Object>) ReflectionType.canonicalType(val.getClass()))
              .toData(val)
            );
        }
      }
    }
    catch (IllegalAccessException x)
    { 
      throw new DataException
      ("Error persisting bean property "+getURI(),x);
    }
    catch (InvocationTargetException x)
    {
      throw new DataException
      ("Error persisting bean property "
        +getURI(),x.getTargetException()
      );
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
    
    BuilderField bfield=(BuilderField) field;
    if (specifier!=bfield.specifier)
    { return false;
    }

    return true;
    
  }
  
    
}