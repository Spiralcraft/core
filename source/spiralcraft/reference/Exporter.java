package spiralcraft.reference;

/**
 *  Provides access to a set of uniquely identifiable resources
 *  (eg. a database, file system, code repository, etc.) 
 *  by exporting References to objects of arbitrary type
 *  and by resolving unique identification strings associated
 *  with those references.
 *
 *  Note that the scope of the unique identification string is local to
 *  a specific exporter.
 */
public interface Exporter
{
  /**
   * Obtain a reference to a target object
   *@return A reference to the object, 
   */
  public Reference export(Object target)
    throws NotReferenceableException;

  /**
   * Obtain a reference to the object identified by the specified identifier. The Reference
   *   may or may not refer to an existing object. 
   * 
   *@return A Reference
   */
  public Reference resolve(String identifier)
    throws UnrecognizedIdentifierException;

}
