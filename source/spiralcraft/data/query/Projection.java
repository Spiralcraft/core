//
// Copyright (c) 2009 Michael Toth
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



import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.parser.CurrentFocusNode;
import spiralcraft.lang.parser.Node;
import spiralcraft.lang.parser.ParentFocusNode;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.DataReflector;

/**
 * <p>Generates a set of data from a source Query as defined by an Expression
 *   that returns a Tuple based type.
 * </p>
 * 
 */
public class Projection<T extends Tuple>
  extends Query
{
 
  private static final ClassLog log
    =ClassLog.getInstance(Projection.class);
  
  private Expression<T> x;
  
  public Projection()
  {
  }
  
  /**
   * Construct a Projection which reads data from the specified source Query 
   *   returns the result of the specified projectionX expression for every
   *   non-null row in the source query. 
   */
  public Projection(Query source,Expression<T> x)
  { 
    this.x=x;
    setSource(source);
  }
  
  @Override
  public FieldSet getFieldSet()
  { 
    if (sources.size()>0)
    { return sources.get(0).getFieldSet();
    }
    else
    { return null;
    }
  }
    
  public Projection
      (Projection<T> baseQuery
      ,Expression<T> x
      )
  { 
    super(baseQuery);
    this.x=x;
  }

  
  /**
   * Specify the Expression which generates a result Tuple based on a 
   *   source Tuple. 
   */
  public void setX(Expression<T> x)
  { this.x=x;
  }
 

  
  public void setSource(Query source)
  { addSource(source);
  }
  
  /**
   *@return the Expression which generates a result Tuple based on the
   *  a source Tuple.
   */
  public Expression<T> getX()
  { return x;
  }

  
  @SuppressWarnings("unchecked")
  @Override
  public <X extends Tuple> BoundQuery<?,X> 
    getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { 
    return 
      new ProjectionBinding(this,focus,store);
  }
  
  private boolean referencesCurrentFocus(Node node)
  {
    if (node instanceof CurrentFocusNode)
    { return true;
    }
    if (node instanceof ParentFocusNode)
    { return false;
    }
    
    Node[] children=node.getSources();
    if (children!=null)
    {
      for (Node child:children)
      { 
        if (child==null)
        { log.warning(node+" returned null child");
        }
        else if (referencesCurrentFocus(child))
        { return true;
        }
      }
    }
    return false;
  }
  
  @Override
  public String toString()
  { return super.toString()
      +"[projectionX="+x+"]: sources="
      +getSources().toString();
  }

  
}

class ProjectionBinding<Tt extends Tuple>
  extends UnaryBoundQuery<Projection<Tt>,Tt,Tuple>
{
  private final Focus<?> paramFocus;
  private boolean resolved;
  private Channel<Tt> projectionChannel;
  
  public ProjectionBinding
    (Projection<Tt> query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query.getSources(),paramFocus,store);
    setQuery(query);
    this.paramFocus=paramFocus;
    
  }

  @Override
  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
      try
      {
        if (debugLevel.canLog(Level.DEBUG))
        { log.fine("Binding projectionX "+getQuery().getX());
        }

        projectionChannel=paramFocus.telescope(sourceChannel)
          .bind(getQuery().getX());
        if (debugLevel.canLog(Level.DEBUG))
        { log.debug("projectionChannel: "+projectionChannel);
        }
        this.boundType=
          ((DataReflector<Tt>) projectionChannel.getReflector()).getType();
          
        if (debugLevel.canLog(Level.FINE))
        { projectionChannel.setDebug(true);
        }
      }
      catch (BindException x)
      { throw new DataException("Error binding projectionX "+x,x);
      }
      resolved=true;
    }
  }
  

  @Override
  protected SerialCursor<Tt> newSerialCursor(SerialCursor<Tuple> source)
    throws DataException
  { return new ProjectionSerialCursor(source);
  }
  
  @Override
  protected ScrollableCursor<Tt> 
    newScrollableCursor(ScrollableCursor<Tuple> source)
    throws DataException
  { return new ProjectionScrollableCursor(source);
  }

  protected class ProjectionSerialCursor
    extends UnaryBoundQuerySerialCursor
  {
    
    public ProjectionSerialCursor(SerialCursor<Tuple> source)
      throws DataException
    { 
      super(source);
    }
  
    @Override
    protected boolean integrate()
    { 
      if (sourceChannel.get()!=null)
      {
        Tt t=projectionChannel.get();
        if (debugFine)
        { 
          log.fine(toString()+" Projection: projected "+t+"   from   "
            +sourceChannel.get());
        }
        if (t!=null)
        { 
          dataAvailable(t);
          return true;
        }
      }
      return false;
      
    }
    
    @Override
    public Type<?> getResultType()
    { return ((DataReflector<Tt>) projectionChannel.getReflector()).getType();
    }

  }

  protected class ProjectionScrollableCursor
    extends UnaryBoundQueryScrollableCursor
  {
    
    
    public ProjectionScrollableCursor(ScrollableCursor<Tuple> source)
      throws DataException
    { 
      super(source);
    }

    @Override
    protected boolean integrate()
    { 
      Tt t=projectionChannel.get();
      if (debugFine)
      { log.fine(toString()+" Projection: projected "+t+"   from   "+sourceChannel.get());
      }
      if (t!=null)
      { 
        dataAvailable(t);
        return true;
      }
      return false;
    }

    @Override
    public Type<?> getResultType()
    { return ((DataReflector<Tt>) projectionChannel.getReflector()).getType();
    }

  }
}