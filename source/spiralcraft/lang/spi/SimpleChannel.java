//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.lang.spi;


import spiralcraft.lang.Reflectable;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TypeModel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.reflect.BeanReflector;


/**
 * An Binding which references a self-contained Object. Used to provide a
 *   programatically defined target against which Expressions can be evaluated.
 */
public class SimpleChannel<T>
  extends AbstractChannel<T>
{
 
  @SuppressWarnings("unchecked")
  private static final <X> Reflector<X> reflect(X val)
  {
    try
    {
      if (val instanceof Reflectable)
      { 
        try
        { return ((Reflectable<X>) val).reflect();
        }
        catch (Error x)
        { throw new Error("Error reflecting "+val,x);
        }
      }
      else
      { return TypeModel.<X>reflect(val);
      }
    }
    catch (BindException x)
    { throw new RuntimeException(x);
    }
  }

  private T _object;
  
  public SimpleChannel(Reflector<T> reflector)
  { super(reflector,false);
  }
  
  /**
   * <p>Publish an object's Bean interface
   * </p>
   */
  public SimpleChannel(T val,boolean isConstant)
  { 
    // super(BeanReflector.<T>getInstance((Class<T>) val.getClass()),isConstant);    
    super(isConstant?reflect(val):TypeModel.reflect(val),isConstant);
    _object=val;

    // System.out.println("SimpleBinding- noclass:"+super.toString()+":["+val+"]");
  }

  public SimpleChannel(Class<T> clazz,T val,boolean isConstant)
  { 
     
    super(BeanReflector.<T>getInstance(clazz),isConstant);
    _object=val; 

    //System.out.println("SimpleBinding- with class:"+super.toString()+":["+val+"]");
  }

  public SimpleChannel(Reflector<T> reflector,T val,boolean isConstant)
  { 
    super(reflector,isConstant);
    _object=val;
  }
  
  @Override
  protected T retrieve()
  { 
    // System.out.println("SimpleBinding "+super.toString()+" - returning "+_object);
    return _object;
  }
  
  @Override
  protected boolean store(T val)
  { 
    _object=val;
    return true;
  }
  
  @Override
  public boolean isWritable()
  { return true;
  }

}
