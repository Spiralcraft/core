package spiralcraft.ui;

/**
 * Base class for UI Controllers
 */
public abstract class Controller
{
  private Controller _parent;

  public void setParent(Controller parent)
  { _parent=parent;
  }

  public void init()
  {
  }

  public void destroy()
  {
  }
}
