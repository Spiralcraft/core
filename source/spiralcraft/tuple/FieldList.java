package spiralcraft.tuple;

import java.util.List;

/**
 * A list of Fields 
 */
public interface FieldList<F extends Field>
  extends List<F>
{
  /**
   *@return The first Field with the specified name, or  null
   *   if none was found
   */
  F findFirstByName(String name);
  
  /**
   *@return The Field at the given position.
   */
  F getField(int pos);
  
}
