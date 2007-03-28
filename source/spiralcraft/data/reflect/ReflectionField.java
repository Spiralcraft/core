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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import java.beans.PropertyDescriptor;

import spiralcraft.data.core.FieldImpl;

import spiralcraft.data.spi.StaticInstanceResolver;

import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Field;

public class ReflectionField
  extends FieldImpl
{
  private final PropertyDescriptor descriptor;
  protected final TypeResolver resolver;
  private Method readMethod;
  private Method writeMethod;
  private boolean forcePersist;
  private boolean depersist=true;
  
  public ReflectionField(TypeResolver resolver,PropertyDescriptor descriptor)
  {
    this.descriptor=descriptor;
    readMethod=descriptor.getReadMethod();
    writeMethod=descriptor.getWriteMethod();
    setName(descriptor.getName());

    if ("class".equals(getName()))
    { 
      // The class field must be persisted to depersist the object,
      //   but the field is read-only so it shouldn't be depersisted
      //   in the normal manner (bean property)
      forcePersist=true;
      depersist=false;
    }
    
    this.resolver=resolver;
    if (descriptor.getPropertyEditorClass()!=null)
    { 
      System.err.println
        ("ReflectionField "
        +getName()
        +" has editor "+descriptor.getPropertyEditorClass()
        );
    }
        
  }
  
  public Method getReadMethod()
  { return readMethod;
  }
  
  public Method getWriteMethod()
  { return writeMethod;
  }
  
  public void setForcePersist(boolean val)
  { forcePersist=val;
  }
  
  public void setDepersist(boolean val)
  { depersist=val;
  }
  
  public PropertyDescriptor getPropertyDescriptor()
  { return descriptor;
  }
  
  public void resolveType()
    throws DataException
  {
    try
    { 
      setType(findType(descriptor.getPropertyType()));
    }
    catch (TypeNotFoundException x)
    { 
      // This should NEVER happen- there always exists a Type for
      //   every java class
      x.printStackTrace();
    }
  }
  
  public void setArchetypeField(Field field)
    throws DataException
  {
//    System.err.println("ReflectionField: "+getURI()+" extends "+field.getURI());
    super.setArchetypeField(field);
    if (field instanceof ReflectionField)
    { 
      ReflectionField rfield=(ReflectionField) field;
      if (readMethod==null)
      { 
        readMethod=rfield.getReadMethod();
//        if (readMethod!=null)
//        { System.err.println("ReflectionField: "+getURI()+" inherited readMethod");
//        }
      }
      
      if (writeMethod==null)
      {
        writeMethod=rfield.getWriteMethod();
//        if (writeMethod!=null)
//        { System.err.println("ReflectionField: "+getURI()+" inherited writeMethod");
//        }
      }
    }
    
  }
  
  
  public boolean isFunctionalEquivalent(Field field)
  {
    if (!super.isFunctionalEquivalent(field))
    { return false;
    }
    if (!(field instanceof ReflectionField))
    { return false;
    }
    
    ReflectionField rfield=(ReflectionField) field;
    if (readMethod==null && rfield.getReadMethod()!=null)
    { return false;
    }
    
    if (writeMethod==null && rfield.getWriteMethod()!=null)
    { return false;
    }
    
    if (readMethod!=null)
    { 
      if (rfield.getReadMethod()==null)
      { return false;
      }
      if (!rfield.getReadMethod().getDeclaringClass().isAssignableFrom
            (getScheme().getType().getNativeClass())
         )
      { 
        // If rfield is from a subclass, which should be assignable from
        //   this bean's native class, the methods are functionally equivalent
        return false;
      }
    }  
    
    if (writeMethod!=null)
    { 
      if (rfield.getWriteMethod()==null)
      { return false;
      }
      if (!rfield.getWriteMethod().getDeclaringClass().isAssignableFrom
            (getScheme().getType().getNativeClass())
         )
      { 
        // If rfield is from a subclass, which should be assignable from
        //   this bean's native class, the methods are functionally equivalent
        return false;
      }
    }  

    return true;
    
  }
  
  public void depersistBeanProperty(Tuple tuple,Object bean)
    throws DataException
  { 
    
    if (!depersist)
    { return;
    }
    
    Object value=getValue(tuple);
    Type type=getType();
    
    if (value instanceof DataComposite)
    { type=((DataComposite) value).getType();
    }
    
    if (value!=null)
    {
      if (type.getScheme()!=null && type.getNativeClass()==null)
      {
        System.err.println("Not depersisting "+type.getURI());
        // Does not correspond to a Java type
      }
      else
      {
        try
        {
          if (type.isPrimitive())
          { 
            if (writeMethod!=null)
            {
              try
              { writeMethod.invoke(bean,value);
              }
              catch (IllegalArgumentException x)
              {
                throw new DataException
                  ("Error depersisting field '"+getName()+"': Does not accept"
                  +" argument type "+value.getClass().getName()
                  ,x
                  );
              }
            }
            else
            {
              System.err.println
                ("ReflectionField: Field "+getURI()+" is not depersistable- no 'set' method: ");              
            }
            
          }
          else
          { 
            Object existingValue=null;
            if (readMethod!=null)
            { existingValue=readMethod.invoke(bean);
            }
            
            DataComposite compositeValue
              =(DataComposite) value;
            Type dataType=compositeValue.getType();
            
            Object convertedValue
              =dataType.fromData
                (compositeValue
                ,existingValue!=null
                  ?new StaticInstanceResolver(existingValue)
                  :null
                );
            
//            System.err.println
//              ("ReflectionField: Field "+getURI()
//              +": depersisting "+convertedValue
//              );            
            
            if (writeMethod!=null)
            {
              try
              { writeMethod.invoke(bean,convertedValue);
              }
              catch (IllegalArgumentException x)
              {
                throw new DataException
                ("Error depersisting field '"+getName()+"': Does not accept"
                    +" argument type "+convertedValue.getClass().getName()
                    ,x
                );
              }
            }
            else if (convertedValue!=existingValue)
            { 
              System.err.println
                ("ReflectionField: Field "+getURI()
                 +" is not depersistable- no 'set' method "
                 +" and no pre-existing value used"
                 );            
              
            }
            else
            {
//              System.err.println
//              ("ReflectionField: Field "+getURI()
//               +" used pre-existing bean value to depersist data"
//               );            
            }
          }
        }
        catch (IllegalAccessException x)
        { throw new DataException("Error depersisting field '"+getName()+"'",x);
        }
        catch (InvocationTargetException x)
        { throw new DataException("Error depersisting field '"+getName()+"'",x);
        }
      }
    }
  }
  
  
  @SuppressWarnings("unchecked")
  public void persistBeanProperty(Object bean,EditableTuple tuple)
    throws DataException
  {
//    System.err.println("ReflectionField "+getURI()+" persistBeanProperty");
    Type<? super Object> type=(Type<? super Object>) getType();
    
    if ( (readMethod!=null && writeMethod!=null)
          || forcePersist
        )
    {
      try
      {
        Object value=readMethod.invoke(bean);
        if (value!=null)
        {
          type=(Type<? super Object>) TypeResolver.getTypeResolver().resolve
            (ReflectionType.canonicalURI(value.getClass()));
          if (!type.isPrimitive())
          { 
            if (type.getNativeClass()!=null)
            { setValue(tuple,type.toData(value));
            }
            else
            { setValue(tuple, ((Type<? super Object>) getType()).toData(value));
            }
          }
          else
          { 
//            System.err.println("ReflectionField "+getURI()+" persisting "+value);
            // Value itself is of primitive type
            setValue(tuple,value);
          }
        }
        else
        { 
          // Persist null value
          setValue(tuple,value);
        }
      }
      catch (IllegalAccessException x)
      { throw new DataException("Error depersisting field '"+getName()+"'",x);
      }
      catch (InvocationTargetException x)
      { throw new DataException("Error depersisting field '"+getName()+"'",x);
      }
    }
    
//    if (readMethod!=null && writeMethod==null)
//    { System.err.println("ReflectionField: Read only property: field "+getURI());
//    }
    
//    if (readMethod==null && writeMethod!=null)
//    { System.err.println("ReflectionField: Write only property: field "+getURI());
//    }
  }
  
  protected Type findType(Class iface)
    throws TypeNotFoundException
  { 
    URI uri=ReflectionType.canonicalURI(iface);
    return resolver.resolve(uri);
  }
}
