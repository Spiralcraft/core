package spiralcraft.loader;

/**
 * Controls the loading of applications for a single VM
 */
public class ApplicationManager
{

  private static ApplicationManager _INSTANCE;

  private LibraryCatalog _catalog=new LibraryCatalog();  

  /**
   * Obtain the singleton instance of the ApplicationManager.
   *
   * Creates the instance if it does not exist already.
   */
  public static synchronized ApplicationManager getInstance()
  { 
    if (_INSTANCE==null)
    { _INSTANCE=new ApplicationManager();
    }
    return _INSTANCE;
  }

}
