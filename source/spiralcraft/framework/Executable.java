package spiralcraft.framework;

/**
 * Implemented by the root object of the runtime object hierarchy
 *   for a application instance. 
 */
public interface Executable
{
  
  /**
   * Execute the application
   */
  public void exec(String[] args);

}
