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
package spiralcraft.util;

/**
 * Associates a String name with a value
 */
public class NamedValue
{
  private String _name;
  private Object _value;

  public NamedValue()
  {
  }

  public void setName(String name)
  { _name=name;
  }

  public void setValue(Object value)
  { _value=value;
  }

  public String getName()
  { return _name;
  }

  public Object getValue()
  { return _value;
  }
}
