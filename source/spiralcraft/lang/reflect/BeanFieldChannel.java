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
package spiralcraft.lang.reflect;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.TranslatorChannel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class BeanFieldChannel<Tprop,Tbean>
  extends TranslatorChannel<Tprop,Tbean>
{

  private final Field _field;
  private final boolean _constant;
  private boolean _writable;
  
  public BeanFieldChannel
    (Channel<Tbean> source
    ,BeanFieldTranslator<Tprop,Tbean> translator
    )
  {
    super(source,translator,null);
    _field=translator.getField();
    _constant 
      = source.isConstant() && 
        Modifier.isFinal(_field.getModifiers());
    _writable
      =Modifier.isPublic(_field.getModifiers())
        && !Modifier.isFinal(_field.getModifiers());
  }



  @Override
  public boolean isConstant()
  { return _constant;
  }

  @Override
  public boolean isWritable()
  { return _writable;
  }
  
  @Override
  public synchronized boolean set(Object val)
    throws AccessException
  {
    try
    { 
      Object source=getSourceValue();
      if (source==null)
      { return false;
      }
      _field.set(getSourceValue(),val);
      return true;
    }
    catch (IllegalAccessException x)
    { 
      throw new AccessException
        (x.toString()+" writing bean field '"+_field.getName()+"'",x);
    }
  }

  @Override
  public String toString()
  { 
    return super.toString()
      +":"+super.toString()
      +":[field="+_field.getName()+" ("+_field.getType()+")]";
  }

}


