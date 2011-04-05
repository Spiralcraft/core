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

import spiralcraft.beans.MappedBeanInfo;
import spiralcraft.data.core.FieldImpl;

import spiralcraft.data.util.StaticInstanceResolver;

import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Field;

import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.reflect.BeanPropertyTranslator;
import spiralcraft.log.Level;

@SuppressWarnings({"unchecked","rawtypes"}) // Not fully genericized yet
public class ReflectionField<T>
  extends FieldImpl<T>
{
  private final BeanPropertyTranslator translator;
  protected final TypeResolver resolver;
  private Method readMethod;
  private Method writeMethod;
  private boolean forcePersist;
  private boolean depersist=true;
  
  public ReflectionField
    (TypeResolver resolver
    ,MappedBeanInfo parentInfo
    ,PropertyDescriptor descriptor)
  {
    Reflector reflector
      =BeanReflector
        .getInstance(parentInfo.getBeanDescriptor().getBeanClass());
    if (reflector instanceof BeanReflector)
    { 
      translator
        =((BeanReflector) reflector).getTranslator(descriptor.getName());
    
      readMethod=translator.getReadMethod();

      
    }
    else
    { 
      throw new RuntimeException
        ("Can't get property "+descriptor.getName()+" from Void for "
        +parentInfo.getBeanDescriptor().getBeanClass()
        );
    }
    
    
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
  { return translator.getProperty();
  }
  
  @Override
  protected Type resolveType()
    throws DataException
  {
    try
    { return this.<T>findType(translator.getReflector().getContentType());
    }
    catch (TypeNotFoundException x)
    {
      // This should NEVER happen- there always exists a Type for
      //   every java class
      x.printStackTrace();
      throw x;
    }

   
  }
  
  @Override
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
  
  
  @Override
  public boolean isFunctionalEquivalent(Field  field)
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
  
  
  public void writeValueToBean(Object value,Object bean)
    throws DataException
  {
    Type<?> type=getType();
    
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
          if (type.isPrimitive()
              || (type.isAggregate() && type.getContentType().isPrimitive())
             )
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
            Type<?> dataType=compositeValue.getType();
            
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
                  ("Error depersisting field '"+getURI()+"': Does not accept"
                      +" argument type "+convertedValue.getClass().getName()
                      ,x
                  );
              }
              catch (InvocationTargetException x)
              {
                throw new DataException
                  ("Error depersisting field '"+getURI()+"': Did not accept"
                      +" value '"+convertedValue+"' depersisted from data "
                      +" composite "+compositeValue+" using type class "
                      +dataType.getClass()
                      +(existingValue!=null?" (existing value was "+existingValue+")":"")
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
              System.err.println
                ("readMethod="+readMethod);
              
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
        { 
          throw new DataException
            ("Error depersisting field '"+getURI()+"'",x);
        }
        catch (InvocationTargetException x)
        { 
          throw new DataException
            ("Error depersisting field '"+getURI()+"'",x);
        }
      }
    }
    
  }
  
  public void depersistBeanProperty(Tuple tuple,Object bean)
    throws DataException
  { 
    
    if (!depersist)
    { return;
    }
    
    Object value=getValue(tuple);
    writeValueToBean(value,bean);
  }
  
  
  public void persistBeanProperty(Object bean,EditableTuple tuple)
    throws DataException
  {
//    System.err.println("ReflectionField "+getURI()+" persistBeanProperty");
    Type<T> type=getType();
    
    if ( (readMethod!=null && writeMethod!=null)
          || forcePersist
        )
    {
      if (debug)
      {
        log.fine
          ("Persisting "+getURI()+": "
          +(forcePersist?"FORCED":readMethod+","+writeMethod)
          );
      }
      try
      {
        T value=(T) readMethod.invoke(bean);
        if (value!=null)
        {
          type=TypeResolver.getTypeResolver().<T>resolve
            (ReflectionType.canonicalURI(value.getClass()));
          if (!type.isPrimitive())
          { 
            if (type.getNativeClass()!=null)
            { setValue(tuple,(T) type.toData(value));
            }
            else
            { setValue(tuple,(T) ((Type<? super Object>) getType()).toData(value));
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
      { throw new DataException("Error persisting field '"+getURI()+"'",x);
      }
      catch (InvocationTargetException x)
      { 
        log.log
          (Level.WARNING,"Error persisting field '"+getURI()+"'"
          ,x);
      }
    }
  }
  
  protected <X> Type<X> findType(Class<?> iface)
    throws DataException
  { 
    URI uri=ReflectionType.canonicalURI(iface);
    return resolver.resolve(uri);
  }
}
