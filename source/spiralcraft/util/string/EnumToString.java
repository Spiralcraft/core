//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.util.string;

public class EnumToString<T extends Enum<T>>
  extends StringConverter<T>
{

  private final Class<T> enumType;
  
  public EnumToString(Class<T> enumType)
  { this.enumType=enumType;
  }
  
  @Override
  public T fromString(String val)
  { 
    try
    { return Enum.valueOf(enumType,val);
    }
    catch (IllegalArgumentException x)
    {
      StringBuilder list=new StringBuilder();
      for (T enm : enumType.getEnumConstants())
      { 
        if (list.length()>0)
        { list.append(",");
        }
        list.append(enm.name());
      }
      throw new IllegalArgumentException(x.getMessage()+". Valid constants are ["+list+"]");
    }
  }
  
  @Override
  public String toString(T enumVal)
  { return enumVal.name();
  }

}
