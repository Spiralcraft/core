package spiralcraft.lang;


/**
 * Default implementation of an Optic.
 */
public class OpticAdapter
  implements Optic
{
  /**
   * Return null. no names exposed
   */
  public Optic resolve(String name,Expression[] parameters)
  { return null;
  }

  /**
   * The target is null
   */
  public Object get()
  { return null;
  };

  /**
   * The target cannot be modified 
   */
  public boolean set(Object value)
  { return false;
  }

}
