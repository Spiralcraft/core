package spiralcraft.tuple;

/**
 * An ordered set of heterogeneously typed values, described by a Scheme.
 *
 * A Tuple represents the state of unit of data at a particular point in time.
 *
 * The Tuple and associated interfaces provide a means to access and
 *   manipulate data in an implementation independent manner. Data producers
 *   and consumers can interact throught the Tuple interface without knowlege
 *   of the specific data structures, storage and processing mechanisms used
 *   by their counterparts
 *
 * Tuples and their Schemes enable just in-time binding between data handling
 *   components and application specific data structures.
 *
 * A Tuple is either in a 'buffered' state or not. The buffered state is
 *   the default initial state of the Tuple. A Tuple exits the buffered state
 *   when 'commitBuffer' is called. 
 *
 * Once a Tuple has exited the buffered state, it does so permanently, and it
 *   becomes immutable. At this point, calls the any method which modifies the 
 *   Tuple will throw an IllegalStateException. 
 *
 * The buffered state provides an opportunity to manipulate Tuple data, the
 *   commitBuffer transition provides an opportunity for implementations to
 *   propogate/store that data atomically, and the final, immutable commited
 *   state allows the Tuple to participate in maps and other long term
 *   data structures/operations.
 *   
 * Because a Tuple is immutable once committed, in order to further modify
 *   the data represented by the Tuple it is necessary to create a new Tuple
 *   in a buffered state (a 'buffer') which contains a copy of the data in the
     original Tuple, via the createBuffer() method. 
 *
 * Once a buffer of an immutable Tuple is committed via the commitBuffer method,
 *   it is registered as a successor with the original Tuple. This forms an
 *   update chain which permits data processing and storage components to
 *   update their references to point to the latest version of any given
 *   piece of data.
 *   
 */
public interface Tuple
{
  /**
   * Return the Scheme of this Tuple
   */
  Scheme getScheme();
  
  /**
   * Return the Object identified by the specified Field.
   */
  Object get(Field field);
  
  /**
   * Replace the Object identified by the specified Field.
   *
   *@throws IllegalStateException If the Tuple is not in a buffered state
   */
  void set(Field field,Object value);
  
  /**
   * Commit the data in a buffered Tuple and render the Tuple immutable
   *
   *@throws IllegalStateException if the Tuple is not in a buffered state
   */
  void commitBuffer();

  /**
   * Create a new Tuple which contains a copy of the data in this Tuple and
   *   is in a modifiable, buffered state.
   *
   *@throws IllegalStageException if the Tuple is already in a buffered state.
   */
  Tuple createBuffer();
  
  /**
   * Indicate whether or not the Tuple is in a buffered state.
   */
  boolean isBuffer();
  
  /**
   * Return the most current version of this Tuple. If the data represented
   *   by this Tuple is no longer relevent, this method will return null. If
   *   this Tuple is the most current version, or this Tuple is a buffer,
   *   this method will return a reference to this Tuple.
   */
  Tuple currentVersion();
  
  /**
   * Return the next successive version of this Tuple. If the data represented
   *   by this Tuple is no longer relevent, this method will return null. If
   *   this Tuple is the most current version or this Tuple is a buffer, this
   *   method will return null.
   */
  Tuple nextVersion();
  
  /**
   * Return the immutable Tuple this buffer is based on. If this Tuple is not
   *   a buffer, this method will return null.
   */
  Tuple original();
}
