package spiralcraft.tuple;

/**
 * A Tuple is a collection of related data values associated with something
 *   in a problem domain.
 *
 * A Tuple is defined by a Scheme, which defines the name, data type,
 *   ordinal position and constraints associated with each data value in the
 *   Tuple as well as the Tuple as a whole. 
 *
 * The primary function of a Tuple is to provide a uniform means of access
 *   to data regardless of the physical storage mechanism which contains that
 *   data. As such, the Tuple interface provides support for data modification
 *   in a manner consistent with the ACID properties of transactional databases. 
 *
 * A Tuple is different than a Java object in the following ways:
 *  
 *   Data Storage:
 *
 *     A Java object is a self-contained construct which associates code in a
 *     Java class with binary data stored on a heap in RAM allocated to the
 *     Java Virtual Machine.
 *
 *     A Tuple is an interface which associates a Scheme with binary data stored
 *     in an implementation specific location, such as a database, a file, or
 *     across a network.
 * 
 *   Domain Specific Methods:
 *
 *     A Java object exposes problem domain specific methods which perform
 *     computations against the data contained in the object. 
 *
 *     A Tuple contains no problem domain specific methods. All the methods
 *     exposed by a Tuple are specific to the retrieval, storage and
 *     modification of data. However, a Scheme may contain metadata which
 *     can associate problem domain specific code with a Tuple.
 * 
 *   Data Modification: 
 *
 *     A Java object's methods allow instantaneous modification of individual
 *     data elements. The changes are immediately visible to all accessors.
 *     
 *     Tuples provides a journalling facility which supports the atomic
 *     modification of data, to support situations where a group of changes
 *     only make sense when applied as a unit. Each Tuple instance represents 
 *     a single version of the data, and contains a link to a Tuple which
 *     represents the next version of the data, if one exists.
 *
 *   Identity:
 *
 *     A Java object contains no intrinsic concept of identity other than the
 *     comparison of Java language references using the == operator. The
 *     design contract associated with the "equals" and "hashCode" methods
 *     is useful for determining the "identicalness" of two objects, but it
 *     does not provide sufficiently  support the notion of identity within
 *     a problem domain.
 *
 *     A Tuple provides support for the concept of identity as it applies to
 *     a problem domains. A single tuple identifies a specific version of
 *     a problem domain object. Two tuples identify different versions of
 *     the -same- problem domain object as long as they have the same Scheme
 *     and return equal values from the getId() method. 
 *
 *   Capacity:
 *
 *     The number of Java objects that can be simultaneously referenced by a
 *     running application is a function of the size of the object data.
 *
 *     The number of Tuples that can be simultaneously referenced by a running
 *     application is a function of the amount of per-Tuple RAM overhead 
 *     introduced by the Tuple implementation, which can theoretically be
 *     minimized to be a single pointer into an external data store.
 *
 * Life Cycle:
 *
 * A Tuple is either in a volatile or an immutable state. The volatile state is
 *   the default initial state of the Tuple, and is when Tuple data is modified.
 *   A Tuple exits the volatile state when 'commitBuffer' is called. 
 *
 * Once a Tuple has exited the volatile state, it does so permanently, and it
 *   becomes immutable. At this point, calls the any method which modifies the 
 *   Tuple will throw an IllegalStateException. 
 *
 * The volatile state provides an opportunity to manipulate Tuple data, the
 *   commitBuffer transition provides an opportunity for implementations to
 *   propogate/store that data atomically, and the final, immutable commited
 *   state allows the Tuple to participate in maps and other long term
 *   data structures/operations.
 *   
 * Because a Tuple is immutable once committed, in order to further modify
 *   the data represented by the Tuple it is necessary to create a new Tuple
 *   in a volatile state (a 'buffer') which contains a copy of the data in the
     original Tuple, via the createBuffer() method. 
 *
 * Once a buffer of an immutable Tuple is committed via the commitBuffer method,
 *   it is registered as a successor with the original Tuple. This forms an
 *   update chain (a journal) which permits data processing and storage
 *   components to update their references to point to the latest version of
 *   any given problem domain object.
 * 
 * Identity:
 *
 * Implementations of Tuple must implement the equals() and the hashCode()
 *   methods appropriately. A Tuple is considered equal to another tuple if
 *   and only if all of its data elements are equal. Two tuples which have
 *   different Schemes may be equal. 
 *
 * All Tuples have an Id, an immutable, opaque identifier which, together with
 *   a Tuple's Scheme,  associates the Tuple with problem domain object.
 *
 * All the Tuples in a journal (an update chain) will have the same Id and the
 *   same Scheme.
 */
public interface Tuple
{
  /**
   * Return the Scheme of this Tuple
   */
  Scheme getScheme();
  
  /**
   * Return the ID of this Tuple. If the Tuple does not support an
   *   identification method, the implementation should return a self
   *   reference (ie. return this).
   *
   * The Object returned must be immutable, and must support the
   *   the equals()/hashCode() design contract.
   */
  Object getId();
  
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
   * Commit the data in a buffered Tuple and render the Tuple immutable.
   *
   *@return The immutable Tuple resulting from the commit (may not be the
   *          same as the buffer, esp. in the case where no data has
   *          been modified.
   *@throws IllegalStateException if the Tuple is not in a buffered state
   *@throws BufferConflictException if the original has been updated prior
   *   to this method being caled.
   */
  Tuple commitBuffer()
    throws BufferConflictException;

  /**
   * Create a new Tuple which contains a copy of the data in this Tuple and
   *   is in a modifiable, buffered state.
   *
   *@throws IllegalStageException if the Tuple is already in a buffered state.
   */
  Tuple createBuffer();
  
  /**
   * Indicate whether or not the Tuple is in a volatile (buffered) state.
   */
  boolean isVolatile();
  
  /**
   * Return the most current version of this Tuple. If the problem domain
   *   object represented by this Tuple no longer exists, 
   *   this method will return null. If this Tuple is the most current version,
   *   or this Tuple is volatile, this method will return a reference to itself.
   */
  Tuple currentVersion();
  
  /**
   * Return the next version of this Tuple. This method will return null if 
   *   the Tuple does not have a more up-to date version or if it is volatile.
   */
  Tuple nextVersion();
  
  /**
   * Return the immutable Tuple this buffer is based on. If this Tuple is 
   *   immutable, or this Tuple is the first version of the data it represents
   *   this method will return null.
   */
  Tuple original();
  
  /**
   * Delete a buffered Tuple and commit the buffer.
   *
   *@throws IllegalStateException if the Tuple is not in a buffered state
   *@throws BufferConflictException if the original has been updated prior
   *   to this method being caled.
   */
  void delete()
    throws BufferConflictException;
  
  /**
   * Indicate whether the Tuple has been deleted.
   */
  boolean isDeleted();
}
