package spiralcraft.data.query;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.lang.Focus;

/**
 * A Query which provides access to all instances of a given Type. This is usually the 
 *   eventual upstream source for all Queries. 
 */
public class TypeAccess
  extends Query
{

  private Type type;
  
  public TypeAccess()
  {
  }
  
  public TypeAccess(Query baseQuery)
  { super(baseQuery);
  }
  
  /**
   * Specify the Type whos instances will be retrieved.
   */
  public void setType(Type type)
  { this.type=type;
  }
  
  /**
   * @return the Type whos instances will be retrieved
   */
  public Type getType()
  { return type;
  }
  
  public FieldSet getFieldSet()
  { return type.getScheme();
  }

  public BoundQuery<?> bind(Focus focus,Queryable store)
    throws DataException
  { 
    return store.getAll(type);
  }
  
}


