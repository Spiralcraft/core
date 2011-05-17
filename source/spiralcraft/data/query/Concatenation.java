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

/**
 * A Query operation which concatenates the results of several Queries of the
 *   same type
 */
public class Concatenation
  extends Query
{
  
  { mergeable=true;
  }
  
  public Concatenation()
  {
  }
  
  /**
   * Construct a Selection which reads data from the specified source Query and filters
   *   data according to the specified constraints expression.
   */
  public Concatenation(Query ... sources)
    throws DataException
  { 
    for (Query query:sources)
    { addSource(query);
    }
    resolve();
  }
  
  @Override
  public void resolve()
    throws DataException
  {
    super.resolve();
    
    type=commonBaseType(sources);
    if (type==null)
    {
      throw new DataException
        ("Concatenation source queries have no base type in common: "+this);
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
  { return new ConcatenationBinding<Concatenation,T>(this,focus,store);
   
  }
  

    
}

