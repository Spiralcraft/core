//
// Copyright (c) 2014 Michael Toth
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
package spiralcraft.data.core;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.TypeParameter;
import spiralcraft.data.TypeResolver;

/**
 * Means by which extended types accept arguments
 * 
 * @author mike
 *
 */
public class TypeParameterField<T>
  extends FieldImpl<T>
{
  @SuppressWarnings({ "unchecked", "rawtypes"})
  public TypeParameterField(TypeParameter<T> parameter) throws DataException
  { 
    setName(parameter.getName());
    setType
      (parameter.getType()!=null
        ?parameter.getType()
        :(Type) TypeResolver.getTypeResolver().getMetaType()
      );
  }
  
  public void fromData(TypeImpl<?> typeImpl,Tuple data)
    throws DataException
  { 
    Object val=super.getValue(data);
    Object arg;
    if (val instanceof DataComposite)
    { arg=((DataComposite) val).getType().fromData((DataComposite) val,null);
    }
    else
    { arg=val;
    }
//    log.fine("Type arg: "+typeImpl.getURI()+"."+getName()+"="+arg);
    typeImpl.setArgument(getName(),arg);
  }
  
}


