package spiralcraft.tuple;

import java.util.List;

/**
 * A list of Fields 
 */
public interface FieldList
  extends List
{
  /**
   *@return The first field with the specified name, or  null
   *   if none was foud
   */
  Field findFirstByName(String name);
  
}
