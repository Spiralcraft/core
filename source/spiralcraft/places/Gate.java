package spiralcraft.places;

/**
 * A means for a UserAgent to gain access to a Place.
 *
 * A Gate is always associated with a single Place.
 *
 * Multiple UserAgents can use the same Gate, though each UserAgent will
 *   receive its own View. 
 */
public interface Gate
{ 
  /**
   * Open the Gate and obtain a View of the Place appropriate for
   *   the specified UserAgent.
   */
  View open(UserAgent agent);
  
  /**
   * Close the Gate with respect the the specified View
   */
  void close(View view);
}
