package spiralcraft.exec;

/**
 * Supplies an entry point for application execution.
 *
 * Implemented by the root object of the runtime object hierarchy
 *   for an application instance.
 *
 * An Executable is normally instantiated from an Assembly (spiralcraft.builder) and
 *   invoked by the Executor.
 */
public interface Executable
{
  
  /**
   * Execute the application
   */
  public void execute(String[] args);

}
