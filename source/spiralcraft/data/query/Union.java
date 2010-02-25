//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.query;


import spiralcraft.lang.Focus;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;


/**
 * A Query operation which combines the results of several Queries
 */
public class Union
  extends Query
{
  
  public Union()
  {
  }
  
  /**
   * Construct a Union which reads data from the specified source Query and filters
   *   data according to the specified constraints expression.
   */
  public Union(Query ... sources)
    throws DataException
  { 
    for (Query query:sources)
    { addSource(query);
    }
    
    
  }
  
  @Override
  public void resolve()
    throws DataException
  { 
    super.resolve();
    for (Query query:sources)
    { 
      if (type==null)
      { type=query.getType();
      }
      else
      { 
        Type<?> initialType=type;
        // Make sure all types have something in common, and return
        //   the most concrete common type.
        if (type.hasBaseType(query.getType()))
        { 
          // We found a more general query
          type=query.getType();
        }
        else 
        {
          while (type!=null && !query.getType().hasBaseType(type))
          { 
            type=type.getBaseType();
          }
          
          if (type==null)
          {
            throw new DataException
              ("Query type"+initialType.getURI()
              +" has nothing in common with "
              +query.getType().getURI()
              );
          }
        }
      }
      
    }    
  }
  
  
  @Override
  public FieldSet getFieldSet()
  { return type.getScheme();
  }



  
  @Override
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding
    (Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new UnionBinding<Union,T>(this,focus,store);
   
  }
  

    
}

