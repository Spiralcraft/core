package spiralcraft.lang;

/**
 * Simple implementation of Focus
 */
public class DefaultFocus
  implements Focus
{

  private Environment _environment;
  private Optic _subject;

  public void setEnvironment(Environment val)
  { _environment=val;
  }
    
  public void setSubject(Optic val)
  { _subject=val;
  }

  /**
   * Return the Environment which resolves
   *   names for this Focus.
   */
  public Environment getEnvironment()
  { return _environment;
  }

  /**
   * Return the subject of expression evaluation
   */
  public Optic getSubject()
  { return _subject;
  }

}
