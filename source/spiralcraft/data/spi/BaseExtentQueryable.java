package spiralcraft.data.spi;

import java.util.HashMap;
import java.util.Map;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;
import spiralcraft.data.query.Union;

import spiralcraft.lang.Focus;

/**
 * <p>A "virtual" Queryable which delegates to multiple Queryables
 *   that represent narrower extents of a base extent to provide
 *   a complete view of all instances of the base extent.
 * </p>
 * 
 * <p>Effectively performs a union of all the extents
 * </p>
 * 
 */
public class BaseExtentQueryable<Ttuple extends Tuple>
  implements Queryable<Ttuple>
{

  private Type<?> type;
  private HashMap<Type<?>,Queryable<Ttuple>> subtypeQueryables
    =new HashMap<Type<?>,Queryable<Ttuple>>();
  
  public BaseExtentQueryable(Type<?> type)
  { this.type=type;
  }
  
  public void addExtent(Type<?> subtype,Queryable<Ttuple> extent)
  { 
    if (subtype==type)
    { 
      throw new IllegalArgumentException
        ("Unsupported configuration- concrete base type:"
        +" extent Type cannot be same as " +
        		"base Type "+type.getURI()
        );
    }
    if (subtype.hasBaseType(type))
    { subtypeQueryables.put(subtype,extent);
    }
    else
    { 
      throw new IllegalArgumentException
        ("Subtype "+type.getURI()+" does not extend "+type.getURI());
    }
  }
  
  @Override
  public boolean containsType(Type<?> type)
  { return type.isAssignableFrom(this.type);
  }

  @Override
  public BoundQuery<?,Ttuple> getAll(Type<?> type)
    throws DataException
  {
    if (type.isAssignableFrom(this.type))
    {
      Scan[] scans=new Scan[subtypeQueryables.size()];

      int i=0;
      for (Map.Entry<Type<?>,Queryable<Ttuple>> entry 
            : subtypeQueryables.entrySet()
          )
      { scans[i++]=new Scan(entry.getKey());
      }
      return new Union(scans).getDefaultBinding(null,this);
    }
    else
    {
      Queryable<Ttuple> queryable=subtypeQueryables.get(type);
      if (queryable!=null)
      { return queryable.getAll(type);
      }
      else
      { 
        throw new DataException
          ("This Queryable does not contain Type "+type);
      }
      
    }
  }

  @Override
  public Type<?>[] getTypes()
  { return new Type[] {type};
  }

  public BoundQuery<?,Ttuple> query(Query q, Focus<?> context)
    throws DataException
  { 
    
    // Get the default binding for the query
    BoundQuery<?,Ttuple> ret=q.solve(context, this);
    ret.resolve();
    return ret;
  }

}
