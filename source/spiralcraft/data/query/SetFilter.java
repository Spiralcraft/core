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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.TeleFocus;
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

/**
 * <p>A Query operation which constrains the result of a source Query by
 *   testing a function of the result for inclusion in a set that is
 *   constant over the source result.
 * </p>
 * 
 * <p>The set is hash-indexed for a constant-time filter comparison
 * </p>
 */
public class SetFilter<T>
  extends Query
{
 
  private static final ClassLog log
    =ClassLog.getInstance(SetFilter.class);
  
  private Expression<?> filterSetX;
  private Expression<T> searchX;
  private boolean excludeMatch;
  
  { mergeable=true;
  }
  
  public SetFilter()
  {
  }
  
  /**
   * Construct a Selection which reads data from the specified source Query and filters
   *   data according to the specified constraints expression.
   */
  public SetFilter(Query source,Expression<?> filterSetX,Expression<T> searchX)
  { 
    this.filterSetX=filterSetX;
    this.searchX=searchX;
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
    
  public SetFilter
      (Selection baseQuery
      ,Expression<?> filterSetX
      ,Expression<T> searchX
      )
  { 
    super(baseQuery);
    this.filterSetX=filterSetX;
    this.searchX=searchX;
  }

  
  /**
   * Specify the Expression which resolves to the set (something that can be
   *   iterated) that is used to filter the results.
   */
  public void setFilterSetX(Expression<?> filterSetX)
  { this.filterSetX=filterSetX;
  }
 
  /**
   * The Expression which resolves the value to search for within the set, 
   *   as a function of the source tuple.
   * 
   * @param searchX
   */
  public void setSearchX(Expression<T> searchX)
  { this.searchX=searchX;
  }
  
  public void setSource(Query source)
  { 
    type=source.getType();
    addSource(source);
  }
  
  /**
   *@return the Expression which resolves to the set
   */
  public Expression<?> getFilterSetX()
  { return filterSetX;
  }
  
  /**
   *@return the Expression which resolves the value to search for within the
   *  set, as a function of the source tuple.
   */
  public Expression<T> getSearchX()
  { return searchX;
  }

  /**
   * Specify that when the search value is found in the set, exclude the Tuple 
   *   from the result instead of including it. Defaults to false.
   * 
   * @param excludeMatch
   */
  public void setExcludeMatch(boolean excludeMatch)
  { this.excludeMatch=excludeMatch;
  } 
  
  public boolean getExcludeMatch()
  { return this.excludeMatch;
  }

  
  @Override
  public <X extends Tuple> BoundQuery<?,X> 
    getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new SetFilterBinding<T,X>(this,focus,store);
   
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
      +"[constraints="+filterSetX+"]: sources="
      +getSources().toString();
  }

  
}

class SetFilterBinding<Ti,Tt extends Tuple>
  extends UnaryBoundQuery<SetFilter<Ti>,Tt,Tt>
{
  private Focus<Tt> focus;
  private IterationDecorator<?,Ti> decorator;
  private Channel<Ti> searchChannel;
  private boolean resolved;
  private boolean excludeMatch;
  
  public SetFilterBinding
    (SetFilter<Ti> query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query,query.getSources(),paramFocus,store);
    this.excludeMatch=getQuery().getExcludeMatch();
    
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
    
      try
      {
        Channel<?> setChannel=paramFocus.bind(getQuery().getFilterSetX());    
        decorator=setChannel.<IterationDecorator>decorate(IterationDecorator.class);
        if (debugLevel.canLog(Level.FINE))
        { setChannel.setDebug(true);
        }
      }
      catch (BindException x)
      { throw new DataException("Error binding searchX "+x,x);
      }
        
      focus= new TeleFocus<Tt>(paramFocus,sourceChannel);
      
      if (debugLevel.canLog(Level.DEBUG))
      { log.fine("Binding searchX "+getQuery().getSearchX());
      }
      
      try
      { 
        searchChannel=focus.bind(getQuery().getSearchX());
        if (debugLevel.canLog(Level.FINE))
        { searchChannel.setDebug(true);
        }
      }
      catch (BindException x)
      { throw new DataException("Error binding searchX "+x,x);
      }
      resolved=true;
    }
  }
  

  @Override
  protected SerialCursor<Tt> newSerialCursor(SerialCursor<Tt> source)
    throws DataException
  { return new SelectionSerialCursor(source);
  }
  
  @Override
  protected ScrollableCursor<Tt> 
    newScrollableCursor(ScrollableCursor<Tt> source)
    throws DataException
  { return new SelectionScrollableCursor(source);
  }

  protected class SelectionSerialCursor
    extends UnaryBoundQuerySerialCursor
  {
    protected final Collection<Ti> set;
    
    public SelectionSerialCursor(SerialCursor<Tt> source)
      throws DataException
    { 
      super(source);
      set=new HashSet<Ti>();
      Iterator<Ti> iterator=decorator.iterator();
      if (iterator!=null)
      { 
        while (iterator.hasNext())
        { set.add(iterator.next());
        }
      }
    }
  
    @Override
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
        if (debugFine)
        { log.fine(toString()+"BoundSetFilter: eod ");
        }
        return false;
      }
    
      if (set.contains(searchChannel.get()) ^ excludeMatch)
      {  
        if (debugFine)
        { log.fine(toString()+"BoundSetFilter: passed "+t);
        }
        dataAvailable(t);
        return true;
      }
      else
      { 
        if (debugFine)
        { log.fine(toString()+"BoundSetFilter: filtered "+t);
        }
        return false;
      }
    }
    
    @Override
    public Type<?> getResultType()
    { 
      Type<?> ret=sourceCursor.getResultType();
      if (ret!=null)
      { return ret;
      }
      else
      { 
        log.fine("Source cursor result type is null "+sourceCursor);
        return null;
      }
    }

  }

  protected class SelectionScrollableCursor
    extends UnaryBoundQueryScrollableCursor
  {
    
    protected final Collection<Ti> set;
    
    public SelectionScrollableCursor(ScrollableCursor<Tt> source)
      throws DataException
    { 
      super(source);
      set=new HashSet<Ti>();
      Iterator<Ti> iterator=decorator.iterator();      
      if (iterator!=null)
      { 
        while (iterator.hasNext())
        { set.add(iterator.next());
        }
      }    
    }

    @Override
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
        if (debugFine)
        { log.fine("BoundSetFilter: eod ");
        }
        return false;
      }

      if (set.contains(searchChannel.get()) ^ excludeMatch)
      {  
        if (debugFine)
        { log.fine("BoundSetFilter: passed "+t);
        }
        dataAvailable(t);
        return true;
      }
      else
      { 
        if (debugFine)
        { log.fine("BoundSetFilter: filtered "+t);
        }
        return false;
      }
    }

    @Override
    public Type<?> getResultType()
    { 
      Type<?> ret=scrollableSourceCursor.getResultType();
      if (ret!=null)
      { return ret;
      }
      else
      { 
        log.fine("Source cursor result type is null "+scrollableSourceCursor);
        return null;
      }
    }
  }
}