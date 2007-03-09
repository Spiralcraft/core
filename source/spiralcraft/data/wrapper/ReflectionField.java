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
package spiralcraft.data.wrapper;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.beans.PropertyDescriptor;

import spiralcraft.data.core.FieldImpl;

import spiralcraft.data.spi.StaticInstanceResolver;

import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Field;

public class ReflectionField
  extends FieldImpl
{
  private final Method readMethod;
  private final Method writeMethod;
  private final PropertyDescriptor descriptor;
  private final boolean isClassField;
  
  public ReflectionField(PropertyDescriptor descriptor)
  {
    this.descriptor=descriptor;
    readMethod=descriptor.getReadMethod();
    writeMethod=descriptor.getWriteMethod();
    setName(descriptor.getName());
    isClassField="class".equals(getName());
    
  }
  
  public Method getReadMethod()
  { return readMethod;
  }
  
  public Method getWriteMethod()
  { return writeMethod;
  }

  public PropertyDescriptor getPropertyDescriptor()
  { return descriptor;
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
    
    if (isClassField)
    { 
      // Dealt with specifically in construction
      return;
    }
    
    Object value=getValue(tuple);
    Type type=getType();
    
    if (value instanceof DataComposite)
    { type=((DataComposite) value).getType();
    }
    
    if (value!=null && writeMethod!=null)
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
            Object existingValue=null;
            if (readMethod!=null)
            { existingValue=readMethod.invoke(bean);
            }
            
            DataComposite compositeValue
              =(DataComposite) value;
            Type dataType=compositeValue.getType();
            
            if (compositeValue instanceof spiralcraft.data.Aggregate
                && dataType instanceof ReflectionType
               )
               { System.err.println("Field :"+getName());
               }
            Object convertedValue
              =dataType.fromData
                (compositeValue
                ,existingValue!=null
                  ?new StaticInstanceResolver(existingValue)
                  :null
                );
            
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
        }
        catch (IllegalAccessException x)
        { throw new DataException("Error depersisting field '"+getName()+"'",x);
        }
        catch (InvocationTargetException x)
        { throw new DataException("Error depersisting field '"+getName()+"'",x);
        }
      }
    }
    else
    {
      if (value!=null && writeMethod==null)
      { 
        System.err.println
          ("Field "+getUri()+" is not depersistable- no 'set' method: ");
      }
    }
  }
  
  
  @SuppressWarnings("unchecked")
  public void persistBeanProperty(Object bean,EditableTuple tuple)
    throws DataException
  {
    Type<? super Object> type=(Type<? super Object>) getType();
    
    if ( (readMethod!=null && writeMethod!=null)
          || getName().equals("class")
        )
    {
      try
      {
        Object value=readMethod.invoke(bean);;
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
    
    if (readMethod==null && writeMethod!=null)
    { 
      System.err.println
        ("Write only property: field "+getUri());
    }
  }
  

}
