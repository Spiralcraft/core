package spiralcraft.ui;

/**
 * Base class for UI Controls. Represents the Controller role
 *  in a Hierarchical Model/View/Controller design pattern
 *. 
 * Controls are normally peered with a single user interface component
 *   from an implementation specific toolkit (ie. Swing, WebUI, etc.)
 */
public interface Control
{
  public void init();

  public void destroy();
}
