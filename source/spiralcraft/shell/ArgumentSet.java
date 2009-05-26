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
package spiralcraft.shell;

import java.util.HashMap;

import spiralcraft.util.ArrayUtil;
/**
 * A structure to hold parameter values passed into commands.
 */
public class ArgumentSet
{
  private final ArgumentDefinition _definition;
  private HashMap<String,Object[]> _values;
  
  
  
  public ArgumentSet(ArgumentDefinition definition)
  { _definition=definition;
  }
  
  public ArgumentDefinition getDefinition()
  { return _definition;
  }
  
  public void setFlag(String name)
  {
    if (_definition.isNameValid(name))
    {
      int count=_definition.getCount(name);
      // Class type=_definition.getType(name);
    
      if (count!=0)
      {
        throw new IllegalArgumentException
          ("Parameter '"+name+"' requires a value");
      }
      
      setValuesRaw(name,new Object[0]);
    }
    else
    {
      if (name.equals(""))
      { throw new IllegalArgumentException("Unnamed arguments not accepted");
      }
      else
      { throw new IllegalArgumentException("Unrecognized argument name '"+name+"'");
      }
    }
  }
  
  public void addValue(String name,Object value)
  {
    if (_definition.isNameValid(name))
    {
      int count=_definition.getCount(name);
      Class<?> type=_definition.getType(name);
    
      if (count==0)
      {
        throw new IllegalArgumentException
          ("Argument '"+name+"' does not accept a value");
      }
      if (!type.isAssignableFrom(value.getClass()))
      { 
        throw new IllegalArgumentException
          ("Argument '"+name+"' requires a value of type "+type);
      }
      
      if (count==1)
      { setValuesRaw(name,new Object[] {value});
      }
      else
      { 
        Object[] values=getValues(name);
        if (values==null)
        { setValuesRaw(name,new Object[] {value});
        }
        else
        { setValuesRaw(name,ArrayUtil.append(values,value));
        }
      }
    }
    else
    {
      if (name.equals(""))
      { throw new IllegalArgumentException("Unnamed arguments not accepted");
      }
      else
      { throw new IllegalArgumentException("Unrecognized argument name '"+name+"'");
      }
    }
  }
  
  public int getCount(String name)
  { 
    if (_values==null)
    { return 0;
    }
    else
    { 
      Object[] values=_values.get(name);
      if (values==null)
      { return 0;
      }
      else
      { return values.length;
      }
    }
  }
  
  public Object[] getValues(String name)
  { 
    if (_values==null)
    { return null;
    }
    else
    { return _values.get(name);
    }
  }
  
  public Object getValue(String name)
  { 
    if (_values==null)
    { return null;
    }
    else
    { 
      Object[] values=_values.get(name);
      if (values==null)
      { return null;
      }
      else
      { return values[0];
      }
    }
  }

  private void setValuesRaw(String name,Object[] values)
  {
    if (_values==null)
    { _values=new HashMap<String,Object[]>();
    }
    _values.put(name,values);
  }
}
