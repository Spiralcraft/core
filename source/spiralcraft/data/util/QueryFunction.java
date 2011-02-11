//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.data.util;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.QueryChannel;
import spiralcraft.data.query.Queryable;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.ContextualFunction;
import spiralcraft.lang.util.LangUtil;

/**
 * Provides a Java level interface for running a Query using different sets
 *   of parameters.
 * 
 * @author mike
 *
 * @param <R>
 * @param <I>
 */
public class QueryFunction<I,R extends DataComposite>
  extends ContextualFunction<I,R,DataException>
{

  
  public static final <I> QueryFunction<I,Tuple> 
    singularInstance
      (final Reflector<I> inputReflector
      ,final Query q
      ,final Queryable<Tuple> useStore
      )
  {
    return new QueryFunction<I,Tuple>()
    {
      {
        this.query=q;
        this.singular=true;
        this.store=useStore;
        this.input=inputReflector;
      }
      
      @Override
      protected Reflector<I> getInputReflector()
      { return input;
      }       
    };
  }


  public static final <I> QueryFunction<I,Aggregate<Tuple>> 
    aggregateInstance
      (final Reflector<I> inputReflector
      ,final Query q
      ,final Queryable<Tuple> useStore
      )
  {
    return new QueryFunction<I,Aggregate<Tuple>>()
    {
      {
        this.query=q;
        this.input=inputReflector;
        this.singular=false;
        this.store=useStore;
      }
      
      @Override
      protected Reflector<I> getInputReflector()
      { return input;
      }      
    };
  }
  
  protected Query query;
  protected boolean singular;
  protected Queryable<Tuple> store;
  protected Reflector<I> input;
  
  public void setQuery(Query query)
  { this.query=query;
  }
  
  public void setInputReflector(Reflector<I> input)
  { this.input=input;
  }
  
  public void setQueryable(Queryable<Tuple> queryable)
  { this.store=queryable;
  }
  
  @Override
  protected Reflector<I> getInputReflector()
  { return input;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Channel<R> bindSource(
    Focus<I> inputFocus)
    throws BindException
  {
    try
    {
      BoundQuery<?,Tuple> binding
        =(store!=null
          ?store
          :LangUtil.assertInstance(Space.class,inputFocus)
          ).query(query,inputFocus);
      
      QueryChannel channel=binding.bind();
      if (singular)
      { return channel.resolve(inputFocus,"@top",null);
      }
      else
      { return (Channel<R>) channel;
      }
    }
    catch (DataException x)
    { throw new BindException("Error binding query",x);
    }
    
  }

}
