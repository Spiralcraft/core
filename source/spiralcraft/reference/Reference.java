package spiralcraft.reference;

public interface Reference
{

  /**
   * Obtain the target of this reference.
   *
   *@return The target, or null if the target does not exist
   */
  public Object getTarget();
  
  /**
   * Obtain the unique identifier for this reference. The identifier
   *   is only valid within the scope of the Exporter which created the
   *   reference.
   *
   *@return An opaque String that uniquely identifies the target object
   */
  public String getIdentifier();

  /**
   * Obtain the Exporter which created this reference.
   *
   *@return The Exporter
   */
  public Exporter getExporter();
}
