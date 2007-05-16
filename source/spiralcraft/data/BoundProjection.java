//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data;

/**
 * Materializes a Projection. A BoundProjection creates a projected Tuple for
 *   a given master Tuple via the project() method, which can be efficiently
 *   called serially.
 */
public interface BoundProjection
{
  /**
   * @return the Projection materialized by this BoundProjection
   */
  public Projection getProjection();
  
  /**
   * <P>Materialize the projection for a single Tuple by computing the value for
   *   each Field.
   * 
   * <P>If this Projection needs to be materialized for more than one Tuple,
   *   use the <code>bind(Cursor master)</code> method for efficiency.
   * 
   * @return A Tuple which contains the values resulting from the transformation
   *   of the specified masterTuple.
   */
  public Tuple project(Tuple masterTuple)
    throws DataException;
  
}
