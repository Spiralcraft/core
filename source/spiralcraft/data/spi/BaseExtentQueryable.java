//
// Copyright (c) 2008,2010 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.spi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Concatenation;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Queryable;
import spiralcraft.data.query.Scan;

import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

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
  
  private static final ClassLog log
    =ClassLog.getInstance(BaseExtentQueryable.class);
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(BaseExtentQueryable.class,null);

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
      subtypeQueryables.put(subtype,extent);
      log.debug("Using concrete base type for "+subtype.getURI());
//      throw new IllegalArgumentException
//        ("Unsupported configuration- concrete base type:"
//        +" extent Type cannot be same as " +
//        		"base Type "+type.getURI()
//        );
    }
    else if (subtype.hasBaseType(type))
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
      return new Concatenation(scans).getDefaultBinding(null,this);
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
  
  

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public BoundQuery<?,Ttuple> solve(Query q, Focus<?> context)
    throws DataException
  {
    // Queries that map to a single scan type that is wider than this
    //   queryable's type will be mapped to a concatenation of 
    //   the results of the query as bound to each subtype extent.
    BoundQuery<?,Ttuple> ret=null;
      
    Set<Type<?>> scanTypes=q.getAccessTypes(new HashSet<Type<?>>());
    if (scanTypes.size()==1 
        && scanTypes.iterator().next().isAssignableFrom(this.type)
        )
    { 
      ArrayList<BoundQuery> subQueries=new ArrayList<BoundQuery>();
      ArrayList<Queryable<Ttuple>> skippedSubtypes
        =new ArrayList<Queryable<Ttuple>>();
      
      boolean mergeable=q.isMergeable();
      
      for (Map.Entry<Type<?>,Queryable<Ttuple>> entry 
            : subtypeQueryables.entrySet()
          )
      { 
        // Deletage to each subtype queryable to check if there is a 
        //   custom solution for the top level query.
        
        BoundQuery<?,Ttuple> subQuery
          =entry.getValue().solve(q,context);
        
        if (subQuery==null)
        { 
          if (subQueries.size()==0)
          { 
            // Keep for later
            skippedSubtypes.add(entry.getValue());
          }
          else
          { 
            // If one provides a custom accessor, we need to combine all
            //   subQueries at this level
            subQuery=entry.getValue().query(q,context);
          }
        }
        
        if (subQuery!=null)
        { 
          // One of the subtype queryables provides a custom solution
          
          if (subQueries.size()>0 && !mergeable)
          { 
            subQueries=null;
            break;
          }
          else
          { 
            if (skippedSubtypes.size()>0 && !mergeable)
            { 
              subQueries=null;
              break;
            }
            
            for (Queryable<Ttuple> skipped : skippedSubtypes)
            { subQueries.add(skipped.query(q,context));
            }
            subQueries.add(subQuery);
          }
        }
      }
      
      if (subQueries!=null && subQueries.size()>0)
      { 
        if (debugLevel.isDebug())
        {
          log.debug
            ("Optimized "+q+" for BaseExtentQueryable "+this.type.getURI());
        }
        ret=q.merge((List) subQueries,context);
      }
      
    }
  
    
    
    
    if (ret!=null)
    { ret.resolve();
    }
    return ret;
  }
  
  @Override
  public BoundQuery<?,Ttuple> query(Query q, Focus<?> context)
    throws DataException
  { 
    BoundQuery<?,Ttuple> ret=solve(q,context);
    if (ret==null)
    {
      if (debugLevel.isDebug())
      {
        log.debug
          ("Using default solution for BaseExtentQueryable "+this.type.getURI()
          +": "+q);
      }
      // Get the default binding for the query
      ret=q.solve(context, this);
    }
    ret.resolve();
    return ret;
  }

}
