package spiralcraft.log;

import java.util.logging.Logger;

import spiralcraft.registry.RegistryPathObject;
import spiralcraft.registry.RegistryNode;

/**
 * A logger which derives its name from the Registry path
 */
public class RegistryLogger
  extends Logger
  implements RegistryPathObject
{

  /**
   * Create root RegistryLogger
   */
  public RegistryLogger()
  { 
    super("",null);
  }

  public RegistryLogger(RegistryLogger parent,String name)
  { 
    super(name,null);
    setParent(parent);
  }
  
  public synchronized RegistryPathObject registryPathObject(RegistryNode registryNode)
  { 
    if (getName().equals(""))
    { return new RegistryLogger(this,registryNode.getName());
    }
    else
    { return new RegistryLogger(this,getName()+"."+registryNode.getName());
    }
  }

}
