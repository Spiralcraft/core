package spiralcraft.loader;

import java.util.HashMap;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.StringUtil;

import spiralcraft.registry.RegistryNode;
import spiralcraft.registry.Registrant;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 * A map of individual Strings to command-line format expansions, with support for
 *   hierarchical defaults. 
 */
public class Aliases
  implements Registrant
{
  private final Aliases _parent;
  private final HashMap _map=new HashMap();
  private RegistryNode _registryNode;

  public Aliases(Aliases parent)
  { _parent=parent;
  }

  public Aliases()
  { _parent=null;
  }

  public void register(RegistryNode registryNode)
  { 
    _registryNode=registryNode;
    Preferences prefs=(Preferences) _registryNode.findInstance(Preferences.class);
    if (prefs!=null)
    {
      try
      {
        String[] keys=prefs.keys();
        for (int i=0;i<keys.length;i++)
        {
          synchronized (_map)
          { _map.put(keys[i],prefs.get(keys[i],keys[i]));
          }
        }
      }
      catch (BackingStoreException x)
      { 
        System.err.println("Could not read aliases.");
        x.printStackTrace();
      }
    }
  }

  /**
   * Match the first element of the source array with an expansion
   *   and, if found, replace the first element with the expansion
   *   and return the result. If not found, return the source array
   */
  public synchronized String[] expand(String[] source)
  {
    if (source.length<1)
    { return source;
    }

    String expansion;
    synchronized (_map)
    { expansion=(String) _map.get(source[0]);
    }

    if (expansion==null)
    { 
      if (_parent!=null)
      { return _parent.expand(source);
      }
      else
      { return source;
      }
    }
    else
    {
      return (String[])  ArrayUtil.appendArrays
        (StringUtil.tokenizeCommandLine(expansion)
        ,ArrayUtil.truncateBefore(source,1)
        );
    }
    
  }
}
