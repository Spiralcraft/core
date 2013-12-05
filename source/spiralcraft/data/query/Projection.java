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



import java.util.Iterator;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
//import spiralcraft.lang.parser.CurrentFocusNode;
//import spiralcraft.lang.parser.Node;
//import spiralcraft.lang.parser.ParentFocusNode;
import spiralcraft.lang.spi.ThreadLocalChannel;
//import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.lang.TupleReflector;

/**
 * <p>Generates a set of data from a source Query as defined by an Expression
 *   that returns a Tuple based type.
 * </p>
 * 
 */
public class Projection<T extends Tuple>
  extends Query
{
 
//  private static final ClassLog log
//    =ClassLog.getInstance(Projection.class);
  
  private Expression<T> x;
  
  { mergeable=true;
  }
  
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
  
  
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  public Query getSource()
  { 
    return getSources()!=null && getSources().size()>0
      ?getSources().get(0) 
      :null;
  }
  
  @Override
  public FieldSet getFieldSet()
  { 
    if (type!=null)
    { return type.getScheme();
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

  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public <X extends Tuple> BoundQuery<?,X> 
    getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { 
    return 
      new ProjectionBinding(this,focus,store);
  }
  
//  private boolean referencesCurrentFocus(Node node)
//  {
//    if (node instanceof CurrentFocusNode)
//    { return true;
//    }
//    if (node instanceof ParentFocusNode)
//    { return false;
//    }
//    
//    Node[] children=node.getSources();
//    if (children!=null)
//    {
//      for (Node child:children)
//      { 
//        if (child==null)
//        { log.warning(node+" returned null child");
//        }
//        else if (referencesCurrentFocus(child))
//        { return true;
//        }
//      }
//    }
//    return false;
//  }
  
  @Override
  public String toString()
  { return super.toString()
      +"[projectionX="+x+"]: sources="
      +getSources().toString();
  }

  
}

class ProjectionBinding<Tt extends Tuple>
  extends BoundQuery<Projection<Tt>,Tuple>
{
  private boolean resolved;
  private Channel<Tt> projectionChannel;
  private BoundQuery<?,Tuple> source;
  protected ThreadLocalChannel<Tuple> sourceChannel;  
  protected boolean aggregate;

  @SuppressWarnings("unchecked")
  public ProjectionBinding
    (Projection<Tt> query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query,paramFocus);
    
    Query sourceQuery=query.getSource();
    
    source
      =(BoundQuery<?,Tuple>) (store!=null
      ?store.query(sourceQuery,paramFocus)
      :sourceQuery.bind(paramFocus)
      );

    if (source==null)
    {  
      throw new DataException
        ("Querying "+store+" returned null (unsupported Type?) for "
        +sourceQuery.toString());
    }    
  }

  @Override
  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
      try
      {
        source.resolve();
        sourceChannel
          =new ThreadLocalChannel<Tuple>
            (new TupleReflector<Tuple>(source.getQuery().getFieldSet(),null));
        
        if (debugLevel.canLog(Level.DEBUG))
        { log.fine("Binding projectionX "+getQuery().getX());
        }

        projectionChannel=paramFocus.telescope(sourceChannel)
          .bind(getQuery().getX());
        if (debugLevel.canLog(Level.DEBUG))
        { log.debug("projectionChannel: "+projectionChannel);
        }
        
        if (!(projectionChannel.getReflector() instanceof DataReflector<?>))
        { 
          throw new DataException
            ("Projection expression must return a Tuple or an Aggregate of "
            +" tuples: "+getQuery().getX()
            );
        }
        this.boundType=
          ((DataReflector<Tt>) projectionChannel.getReflector()).getType();
        
        if (this.boundType.isAggregate())
        { 
          this.aggregate=true;
          this.boundType=this.boundType.getContentType();
        }
        
        if (this.boundType.isPrimitive())
        { 
          throw new DataException
            ("Projection expression cannot return a primitive type: "
               +getQuery().getX()
            );
        }
        
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
  public SerialCursor<Tuple> doExecute()
    throws DataException
  { return new ProjectionSerialCursor();
  }

  protected class ProjectionSerialCursor
    extends BoundQuerySerialCursor
  {
    
    private final SerialCursor<?> parentCursor;
    private Iterator<Tt> childIterator;
    
    public ProjectionSerialCursor()
      throws DataException
    { 
      super();
      parentCursor=source.execute();
    }
  

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean next()
      throws DataException
    {
      sourceChannel.push(parentCursor.getTuple());
      try
      {
        if (aggregate)
        {
          while (true)
          { 
            if (childIterator!=null)
            {
              if (childIterator.hasNext())
              { 
                dataAvailable(childIterator.next());
                return true;
              }
              else
              { 
                childIterator=null;
              }
            }
          
            if (childIterator==null)
            { 
            
              if (parentCursor.next())
              {
                sourceChannel.set(parentCursor.getTuple());
                Iterable childVal=(Iterable<Tt>) projectionChannel.get();
                if (childVal!=null)
                { childIterator=childVal.iterator();
                }
              }
              else
              { return false;
              }
            
            }
          }
        }
        else
        { 
          while (true)
          { 
            if (parentCursor.next())
            {
              sourceChannel.set(parentCursor.getTuple());
              Tt childVal=projectionChannel.get();
              if (childVal!=null)
              { 
                dataAvailable(childVal);
                return true;
              }
              
            }
            else
            { return false;
            }
          }
        }
      }
      finally
      { sourceChannel.pop();
      }
    }
    
    @Override
    public void close()
      throws DataException
    {
      if (childIterator!=null)
      { 
        while (childIterator.hasNext())
        { childIterator.next();
        }
      }
      if (parentCursor!=null)
      { parentCursor.close();
      }
    }
    
    
    
    @Override
    public Type<?> getResultType()
    {  
      Type<?> ret
        =((DataReflector<Tt>) projectionChannel.getReflector()).getType();
      return ret;
    }

  }

}