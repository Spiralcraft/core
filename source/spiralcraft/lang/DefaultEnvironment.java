package spiralcraft.lang;

import java.util.HashMap;
import java.util.Map;

public class DefaultEnvironment
  implements Environment
{
  private final Map _names=new HashMap();

  public Optic resolve(String name)
  { return (Optic) _names.get(name);
  }

  public void bind(String name,Optic val)
  { _names.put(name,val);
  }

  
}
