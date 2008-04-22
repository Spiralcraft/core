package spiralcraft.data.query;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;

import spiralcraft.lang.Focus;

/**
 * A Query which provides access to all instances of a given Type. This is usually the 
 *   eventual upstream source for all Queries. 
 */
public class Scan
  extends Query
{
  
  /**
   * Construct an unconfigured Scan
   */
  public Scan()
  { }
  
  /**
   * Construct a new Scan for the given Type
   */
  public Scan(Type<?> type)
  { this.type=type;
  }
  
  public Scan(Query baseQuery)
  { super(baseQuery);
  }
  
  /**
   * Specify the Type whos instances will be retrieved.
   */
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  /**
   * @return the Type whos instances will be retrieved
   */
  public Type<?> getType()
  { return type;
  }
  
  public FieldSet getFieldSet()
  { 
    if (type!=null)
    { return type.getScheme();
    }
    else
    { return null;
    }
  }


  @SuppressWarnings("unchecked")
  public <T extends Tuple> BoundQuery<?,T> 
    getDefaultBinding(Focus<?> focus,Queryable<?> queryable)
    throws DataException
  { 
    return (BoundQuery<?,T>) queryable.getAll(type);
  }
  
  public String toString()
  { return super.toString()+"[type="+getType()+"]";
  }
}


