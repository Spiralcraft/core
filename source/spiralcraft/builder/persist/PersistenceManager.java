package spiralcraft.builder.persist;

import spiralcraft.tuple.Store;
import java.net.URI;

/**
 * Manages the lifecycle of PersistentReferences.
 */
public class PersistenceManager
{
  /**
   * Create a new PersistenceManager which uses the specified tuple Store
   *   as the storage mechanism for PersistentReferences.
   */
  public PersistenceManager(Store store)
  {
  }
  
  /**
   * Create a new PersistentReference to be stored at the given location and
   *   based on the specified AssemblyClass 
   */
  public PersistentReference create(URI storeName,URI assemblyClassUri)
  { return null;
  }
  
  /**
   * Activate the PersistentReference stored at the given location 
   */
  public PersistentReference activate(URI storeName)
  { return null;
  }
  
  /**
   * Deactive the specified PersistentReference. The persistence data
   *   will be flushed and the reference will no longer be accessible.
   */
  public void deactivate(PersistentReference reference)
  {
  }

}
