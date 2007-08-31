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
package spiralcraft.text.xml;

/**
 * An attribute name/value pair 
 */
public class Attribute
{
  public Attribute(String name,String value)
  { 
    _name=name;
    _value=value;
  }

  public String getName()
  { return _name;
  }

  public String getValue()
  { return _value;
  }

  public String toString()
  { return "["+_name+"=\""+_value+"\"]";
  }

  private String _name;
  private String _value;
  


}
