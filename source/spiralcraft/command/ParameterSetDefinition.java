package spiralcraft.command;

import java.util.LinkedHashMap;

public class ParameterSetDefinition
{
  private LinkedHashMap _parameters=new LinkedHashMap();
  
  public void addParameter(String name,int count,Class type,boolean required)
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
  
  public Class getType(String name)
  { return getParameter(name).type;
  }
}

class ParameterDefinition
{
  public final String name;
  public final int count;
  public final Class type;
  public final boolean required;

  public ParameterDefinition(String name,int count,Class type,boolean required)
  {
    this.name=name;
    this.count=count;
    this.type=type;
    this.required=required;
  }
  
}

