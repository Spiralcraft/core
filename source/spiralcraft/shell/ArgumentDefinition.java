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
package spiralcraft.shell;

import java.util.LinkedHashMap;

public class ArgumentDefinition
{
  private LinkedHashMap<String,ParameterDefinition> _parameters
  	=new LinkedHashMap<String,ParameterDefinition>();
  
  public void addParameter(String name,int count,Class<?> type,boolean required)
  { _parameters.put(name,new ParameterDefinition(name,count,type,required));
  }
  
  public ParameterDefinition getParameter(String name)
  { return (ParameterDefinition) _parameters.get(name);
  }
  
  public boolean isNameValid(String name)
  { return _parameters.get(name)!=null;
  }
  
  public int getCount(String name)
  { return getParameter(name).count;
  }
  
  public Class<?> getType(String name)
  { return getParameter(name).type;
  }
}

class ParameterDefinition
{
  public final String name;
  public final int count;
  public final Class<?> type;
  public final boolean required;

  public ParameterDefinition
    (String name,int count,Class<?> type,boolean required)
  {
    this.name=name;
    this.count=count;
    this.type=type;
    this.required=required;
  }
  
}

