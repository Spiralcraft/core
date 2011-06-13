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
package spiralcraft.cli;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import spiralcraft.lang.util.Configurator;
import spiralcraft.util.ListMap;

/**
 * Maps command line arguments to a spiralcraft.lang.Reflector
 */
public abstract class ReflectorArguments<T>
  extends Arguments
{
  protected final Configurator<T> configurator;
  private HashMap<Character,String> shortOptionMap
    =new HashMap<Character,String>();
  
  protected ListMap<String,String> values
    =new ListMap<String,String>();
  
  protected ReflectorArguments(Configurator<T> configurator)
  { this.configurator=configurator;
  }
  
  protected void mapShortOption(char shortOption,String longOption)
  { shortOptionMap.put(shortOption,longOption);
  }

  
  /**
   * Subclass should process an option, and return true
   *   if the option was recognized. String will be 'interned'
   *   for convenience using == as a comparator.
   * @param option 
   */
  @Override
  protected boolean processOption(String option)
  { 
    if (option.charAt(0)=='-')
    { return processLongOption(option.substring(1));
    }
    else if (option.length()>0)
    { 
      for (int i=0;i<option.length();i++)
      { 
        String longOption=shortOptionMap.get(option.charAt(i));
        if (longOption==null)
        { 
          throw new IllegalArgumentException
            ("Unrecognized option -"+option.charAt(i));
        }
        if (!processLongOption(longOption))
        { return false;
        }
        
      }
      return true;
    }
    else
    { return false;
    }
  }
  
  protected boolean processLongOption(String option)
  { 

    Class<?> type=configurator.getType(option);
    if (type==Boolean.class || type==boolean.class)
    { values.set(option,"true");
    }
    else if (!hasMoreArguments())
    { throw new IllegalArgumentException("Option --"+option+" requires an argument, which must be translatable to a "+type.getName());
    }
    else
    { values.add(option,nextArgument());
    }
    return true;
  }
  
  @Override
  protected void completed()
  {
    for (String key:values.keySet())
    {
      Class<?> type=configurator.getType(key);
      List<String> valueList=values.get(key);
      if (type.isArray() && Collection.class.isAssignableFrom(type))
      { configurator.set(key,valueList.toArray(new String[valueList.size()]));
      }
      else
      { configurator.set(key,valueList.get(valueList.size()-1));
      }
    }
  }

}
