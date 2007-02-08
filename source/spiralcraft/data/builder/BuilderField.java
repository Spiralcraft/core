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

import java.lang.reflect.Method;
import java.lang.reflect.Array;

import java.beans.PropertyDescriptor;

import spiralcraft.data.Tuple;
import spiralcraft.data.DataComposite;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

import spiralcraft.data.wrapper.ReflectionField;

import spiralcraft.data.spi.StaticInstanceResolver;

import spiralcraft.builder.PropertySpecifier;
import spiralcraft.builder.PropertyBinding;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

public class BuilderField
  extends ReflectionField
{
 
  private final PropertySpecifier specifier;
  
  public BuilderField(PropertyDescriptor prop,PropertySpecifier specifier)
  { 
    super(prop);
    this.specifier=specifier;
  }
  
  public void depersistBeanProperty(Tuple tuple,Object val)
    throws DataException
  {
    Object fieldValue=getValue(tuple);
    if (fieldValue!=null)
    {
      
      try
      {
        Assembly assembly=(Assembly) val;
        if (specifier!=null)
        {
          PropertyBinding binding=specifier.getPropertyBinding(assembly);
          StaticInstanceResolver resolver=new StaticInstanceResolver(binding);
          
          if (getType().getCoreType() instanceof BuilderType)
          { 
            if (getType().isAggregate())
            {
              Assembly[] valueAssemblies
                =(Assembly[]) getType().fromData
                  ((DataComposite) fieldValue,resolver);
                  
              if (valueAssemblies!=null)
              { binding.replaceContents(valueAssemblies);
              }
            }
            else
            { 
              Assembly valueAssembly
                =(Assembly) getType().fromData
                  ((DataComposite) fieldValue,resolver);
                  
              if (valueAssembly!=null)
              { binding.replaceContents(new Assembly[] {valueAssembly});
              }
            }
            
          }
          else
          {
            // Use bean method to depersist
            Object bean=assembly.getSubject().get();
            super.depersistBeanProperty(tuple,bean);
          }
        }
        else
        {
          if (getType().getCoreType() instanceof BuilderType)
          { 
            throw new DataException
              ("Field '"+getName()+"' must be declared in Assembly: "
              +getScheme().getType().getUri()
              +" in order to depersist."
              );
          }
          else
          {
            Object bean=assembly.getSubject().get();
            super.depersistBeanProperty(tuple,bean);
          }
        }
      }
      catch (BuildException x)
      { 
        throw new DataException
          ("Error depersisting field '"+getName()+"' in "
          +getScheme().getType().getUri()+": "+x
          ,x
          );
      }
    }
  }

  
  public void persistBeanProperty(Object object,EditableTuple tuple)
    throws DataException
  {
    Assembly assembly=(Assembly) object;
    if (specifier!=null)
    {
      // Use Assembly scaffold to persist
      if (specifier.isPersistent())
      { 
        PropertyBinding binding=specifier.getPropertyBinding(assembly);
        if (getType().getCoreType() instanceof BuilderType)
        { 
          if (getType().isAggregate())
          { setValue(tuple,getType().toData(binding.getContents()));
          }
          else
          { setValue(tuple,getType().toData(binding.getContents()[0]));
          }
        }
        else
        {    
          Object bean=assembly.getSubject().get();
          super.persistBeanProperty(bean,tuple);
        }
      }
    }
    else
    {
      if (!(getType().getCoreType() instanceof BuilderType))
      { 
        Object bean=assembly.getSubject().get();
        super.persistBeanProperty(bean,tuple);
      }
    }
  }
  

}