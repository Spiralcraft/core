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

import java.util.ArrayList;
import java.util.List;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.parser.CurrentFocusNode;
import spiralcraft.lang.parser.EqualityNode;
import spiralcraft.lang.parser.LiteralNode;
import spiralcraft.lang.parser.LogicalAndNode;
import spiralcraft.lang.parser.Node;
import spiralcraft.lang.parser.ParentFocusNode;
import spiralcraft.lang.parser.ContextIdentifierNode;
import spiralcraft.lang.parser.ResolveNode;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;

/**
 * A Query operation which constrains the result of another Query
 */
public class Selection
  extends Query
{
 
  private static final ClassLog log
    =ClassLog.getInstance(Selection.class);
  
  private Expression<Boolean> constraints;
  
  { mergeable=true;
  }
  
  public Selection()
  {
  }
  
  public Selection(Type<?> type,Expression<Boolean> constraints)
  { 
    this.constraints=constraints;
    setSource(new Scan(type));
  
  }
  
  /**
   * Construct a Selection which reads data from the specified source Query and filters
   *   data according to the specified constraints expression.
   */
  public Selection(Query source,Expression<Boolean> constraints)
  { 
    this.constraints=constraints;
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
    
  public Selection
      (Selection baseQuery
      ,Expression<Boolean> constraints
      )
  { 
    super(baseQuery);
    this.constraints=constraints;
  }

  
  /**
   * Specify the Expression which constrains the result
   */
  public void setConstraints(Expression<Boolean> constraints)
  { this.constraints=constraints;
  }
 
  public void setSource(Query source)
  { 
    type=source.getType();
    addSource(source);
  }
  
  /**
   *@return the Expression which constrains the result
   */
  public Expression<Boolean> getConstraints()
  { return constraints;
  }
  

  
  @Override
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new SelectionBinding<Selection,T>(this,focus,store);
   
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
  
  
  /**
   * Recursively factor a node into EquiJoin expressions and a remainder.
   * 
   * @param lhsList
   * @param rhsList
   * @return The remainder
   */
  private Node factorNode
    (Node original,List<Expression<?>> lhsList,List<Expression<?>> rhsList)
  {
    if (original instanceof LogicalAndNode)
    {
      LogicalAndNode andNode=(LogicalAndNode) original;
      Node lhsRemainder
        =factorNode(andNode.getLeftOperand(),lhsList,rhsList);
      Node rhsRemainder
        =factorNode(andNode.getRightOperand(),lhsList,rhsList);
      
      if (lhsRemainder!=null && rhsRemainder!=null)
      { return lhsRemainder.and(rhsRemainder);
      }
      else if (lhsRemainder!=null)
      { return lhsRemainder;
      }
      else
      { return rhsRemainder;
      }
      
    }
    else if (original instanceof EqualityNode<?>
             && !((EqualityNode<?>) original).isNegated()
            )
    {
      // Terminal case- EqualityNode (==)
      return 
        factorPositiveEqualityNode((EqualityNode<?>) original,lhsList,rhsList);
      
    }
    // Add more cases here
    else
    { return original;
    }
        
  }
  
  public Node factorPositiveEqualityNode
    (EqualityNode<?> equalityNode
    ,List<Expression<?>> lhsList
    ,List<Expression<?>> rhsList)
  {
    Node lhs=equalityNode.getLeftOperand();
    boolean factored=false;
    if (lhs instanceof ResolveNode<?>)
    {
      ResolveNode<?> lhsResolve=(ResolveNode<?>) lhs;
      if (lhsResolve.getSource() instanceof CurrentFocusNode)
      { 
        // Test right hand side for legal expressions
          
        // We're doing this by inclusion right now to be safe
        
        // What cannot be allowed is a CurrentFocusNode on the RHS, because
        //  that is the variant node. 
          
        Node rhs=equalityNode.getRightOperand();
        Node validRhs=null;
        if (rhs instanceof ContextIdentifierNode)
        {
          ContextIdentifierNode rhsIdent=(ContextIdentifierNode) rhs;
          if (rhsIdent.getSource()==null
              || rhsIdent.getSource() instanceof CurrentFocusNode
              )
          { validRhs=rhsIdent;
          }
          else
          { 
            if (debugLevel.canLog(Level.DEBUG))
            { 
              log.debug
                ("Ident node source is not current focus "
                +rhsIdent.getSource()+" "+rhsIdent.getSource().reconstruct()
                );
            }
          }
        }
        else if (rhs instanceof LiteralNode<?>)
        { validRhs=rhs;
        }
        else if (!referencesCurrentFocus(rhs))
        { validRhs=rhs;
        }
        else
        { 
          if (debugLevel.canLog(Level.DEBUG))
          { 
            log.debug
              ("Not a valid RHS for refactoring "
              +rhs.reconstruct()
              + " ("+rhs.toString()+")"
              );
          }
        }

        // XXX Some logic in Expressions to determine grounding is in
        //   order.
            
        if (validRhs!=null)
        {
          lhsList.add(new Expression<Object>(lhsResolve));
          rhsList.add(new Expression<Object>(validRhs));
          if (debugLevel.canLog(Level.DEBUG))
          { 
            log.debug
              ("Factored "
              +lhsResolve.reconstruct()+" = "+validRhs.reconstruct()
              );
          }
          factored=true;
        }
      }
      else
      {
        if (debugLevel.canLog(Level.DEBUG))
        { 
          log.debug
            ("Resolve node source is not current focus "
            +lhsResolve.getSource()+" "+lhsResolve.getSource().reconstruct()
            );
        }
      }
    }
    else
    {
      if (debugLevel.canLog(Level.DEBUG))
      { log.debug("lhs is not a resolve node "+lhs.reconstruct());
      }
    }
    if (factored)
    { return null;
    }
    else
    { return equalityNode;
    }
  }
  
  
  /**
   * <p>Factor a base Query into a downstream Query (returned) and 
   *   one or more upstream Queries (from getSources()) that achieve the same
   *   result as this Query. This method is used to provide Queryables with 
   *   fine control when implementing optimized versions of the various Query
   *   operations.
   * </p>
   *   
   * <p>The upstream Queries implement the bulk of the operations in the
   *   original Query, while the downstream Query implements a single operation 
   *   factored out of the base Query.
   * </p>
   * 
   * 
   * @return The downstream Query resulting from factoring this Query, or null if
   *   the Query cannot be factored.
   */
  @Override
  protected Query factor()
  { 
    if (debugLevel.canLog(Level.DEBUG))
    { log.debug("factor()");
    }
    if (constraints!=null)
    {
      // Factor the expression
      
      ArrayList<Expression<?>> lhsExpressions=new ArrayList<Expression<?>>(5);
      ArrayList<Expression<?>> rhsExpressions=new ArrayList<Expression<?>>(5);
      
      Node remainder=factorNode
        (constraints.getRootNode(),lhsExpressions,rhsExpressions);
      
      if (lhsExpressions.isEmpty())
      { 
        // We didn't find anything to refactor
        return null;
      }
      else
      {
        EquiJoin ej=new EquiJoin();
        ej.setLHSExpressions(lhsExpressions.toArray(new Expression<?>[0]));
        ej.setRHSExpressions(rhsExpressions.toArray(new Expression<?>[0]));
        ej.setSource(getSources().get(0));
        
        Query result;
        if (remainder==null)
        { result=ej;
        }
        else
        { result=new Selection(ej,new Expression<Boolean>(remainder));
        }
        
        result.setDebugLevel(debugLevel);
        result.setLogStatistics(logStatistics);
        return result;
        
      }
      
      
    } // Constraints!=null
    
    
    return null;
  }
  
  @Override
  public String toString()
  { return super.toString()
      +"[constraints="+constraints+"]: sources="
      +getSources().toString();
  }

  
}

class SelectionBinding<Tq extends Selection,Tt extends Tuple>
  extends UnaryBoundQuery<Tq,Tt,Tt>
{
  private final Focus<?> paramFocus;
  private Focus<Tt> focus;
  private Channel<Boolean> filter;
  private boolean resolved;
  
  public SelectionBinding
    (Tq query
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
    

      focus= new TeleFocus<Tt>(paramFocus,sourceChannel);
      
      if (debugLevel.canLog(Level.DEBUG))
      { log.debug("Binding constraints "+getQuery().getConstraints());
      }
      
      try
      { 
        filter=focus.<Boolean>bind(getQuery().getConstraints());
        if (debugLevel.canLog(Level.FINE))
        { filter.setDebug(true);
        }
      }
      catch (BindException x)
      { throw new DataException("Error binding constraints "+x,x);
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
    public SelectionSerialCursor(SerialCursor<Tt> source)
      throws DataException
    { super(source);
    }
  
    @Override
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
        if (debugFine)
        { log.fine(toString()+"BoundSelection: eod ");
        }
        return false;
      }
    
      if (filter.get())
      {  
        if (debugFine)
        { log.fine(toString()+"BoundSelection: passed "+t);
        }
        dataAvailable(t);
        return true;
      }
      else
      { 
        if (debugFine)
        { log.fine(toString()+"BoundSelection: filtered "+t);
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
    public SelectionScrollableCursor(ScrollableCursor<Tt> source)
      throws DataException
    { super(source);
    }

    @Override
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
        if (debugFine)
        { log.fine("BoundSelection: eod ");
        }
        return false;
      }

      if (Boolean.TRUE.equals(filter.get()))
      {  
        if (debugFine)
        { log.fine("BoundSelection: passed "+t);
        }
        dataAvailable(t);
        return true;
      }
      else
      { 
        if (debugFine)
        { log.fine("BoundSelection: filtered "+t);
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