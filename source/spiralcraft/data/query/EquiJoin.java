//
// Copyright (c) 1998,2008 Michael Toth
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
import spiralcraft.lang.ParseException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.parser.BindingNode;
import spiralcraft.lang.parser.ContextIdentifierNode;
import spiralcraft.lang.parser.CurrentFocusNode;
import spiralcraft.lang.parser.ResolveNode;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;

import java.util.ArrayList;

/**
 * <p>A Query that represents a set of equality associations.
 * </p>
 * 
 * <p>Associates a list of Field values, scoped to a given FieldSet
 *   (eg. values of key fields that identify a business object) with values
 *   from some parameter context (eg. a lookup function in an application, or
 *   a parent Query).
 * </p>
 * 
 * <p>The sequence of Field expressions, or lhsExpressions, uniquely identifies
 *   a single data access pathway. The rhsExpressions represent contextual
 *   bindings and provide a set of values which comprise a key that identifies
 *   a bounded set of query results. 
 * </p>
 * 
 * <p>The Expression is relative to a TeleFocus
 *   where the subject represents the item being searched and is referenced
 *   with a dot prefix (eg. ".name" or ".fooId"), and the context represents
 *   the parameterizing object and is referenced without a dot prefix
 *   (eg. "selectedName" or "bar.fooId"). Ultimately, the equivalent expression
 *   is a conjunction of equality expressions (".name==selectedName 
 *   && .fooId=bar.fooId").
 * </p>
 * 
 * <p>The EquiJoin is used by Keys and Projections to provide a
 *   hash-based Query path for object-style joins to retrieve sets of objects
 *   which share keys.
 * </p>
 * 
 * @author mike
 *
 */
public class EquiJoin
  extends Query
{
  private static final ClassLog log
    =ClassLog.getInstance(EquiJoin.class);
  
  private ArrayList<Expression<?>> rhsExpressions;
  private ArrayList<Expression<?>> lhsExpressions;
  private String[] assignments;
  
  { mergeable=true;
  }
  
  public EquiJoin()
  { 
  }
  
  /**
   * <p>Construct an EquiJoin which returns entities of the specified type
   *   where the expression "lhs == rhs" evaluates to true.
   * </p> 
   *   
   * 
   * @param type
   * @param lhs An expression, normally in the form ".field"
   * @param rhs An expression, normally referencing the parent context
   */
  public EquiJoin(Type<?> type,Expression<?> lhs,Expression<?> rhs)
  {
    setSource(new Scan(type));
    setLHSExpressions(new Expression<?>[] {lhs});
    setRHSExpressions(new Expression<?>[] {rhs});
  }
  
  /**
   * <p>Construct an EquiJoin which returns entities of the specified type
   *   where the expression "lhs[i] == rhs[i]" evaluates to true for each index
   *   "i" of lhs and rhs. The lengths of the lhs and rhs must be the same.
   * </p> 
   *   
   * 
   * @param type
   * @param lhs An expression, normally in the form ".field"
   * @param rhs An expression, normally referencing the parent context
   */
  public EquiJoin(Type<?> type,Expression<?>[] lhs,Expression<?>[] rhs)
  {
    setSource(new Scan(type));
    setLHSExpressions(lhs);
    setRHSExpressions(rhs);
  }
  
  /**
   * <p>Construct an EquiJoin which returns entities of the specified type
   *   that correspond to key defined by the bound expressions
   * </p> 
   *   
   * 
   * @param type
   * @param bindings An array of expressions in the form "field := value"
   */
  public EquiJoin(Type<?> type,Expression<?>[] bindings)
  {
    setSource(new Scan(type));
    setBindings(bindings);
  }
  
//  
//  public EquiJoin subtypeCopy(Type<?> subtype)
//  { 
//    EquiJoin ret=new EquiJoin();
//    ret.setSource(new Scan(subtype));
//    
//    if (lhsExpressions!=null)
//    {
//      ret.setLHSExpressions
//        (lhsExpressions.toArray(new Expression<?>[lhsExpressions.size()])
//        );
//    }
//    
//    if (rhsExpressions!=null)
//    {
//      ret.setRHSExpressions
//        (rhsExpressions.toArray(new Expression<?>[rhsExpressions.size()])
//        );
//    }
//    if (assignments!=null)
//    { ret.setAssignments(assignments);
//    }
//    return ret;
//  }

  
//  /**
//   * <p>Construct an EquiJoin, which queries rows by specifying the values of
//   *   a set of fields that define a relation.
//   * </p>
//   * 
//   * <p>An EquiJoin permits the use of a set of field values as a hashable key
//   *   to identify a result.
//   * </p> 
//   *   
//   */
//  public EquiJoin(Query source,String[] assignments)
//  { 
//    setAssignments(assignments);
//    addSource(source);
//  }
//  
  
  /**
   * <p>Use a set of Binding Expressions to specify field values.
   * </p>
   * 
   */
  public void setBindings(Expression<?> ... bindings)
    throws IllegalArgumentException
  { 
    if (lhsExpressions==null)
    { lhsExpressions=new ArrayList<Expression<?>>();
    }
    
    if (rhsExpressions==null)
    { rhsExpressions=new ArrayList<Expression<?>>();
    }

    for (Expression<?> binding : bindings)
    { 
      if (binding.getRootNode() instanceof BindingNode)
      {
        BindingNode<?,?> bnode=(BindingNode<?,?>) binding.getRootNode();
        if (bnode.getTarget() instanceof ContextIdentifierNode)
        {
          lhsExpressions.add(Expression.<Object>create(bnode.getTarget()));
          rhsExpressions.add(Expression.<Object>create(bnode.getSource()));
        }
        else
        { 
          throw new IllegalArgumentException
            ("Binding '"+binding.getText()
            +"' is not in the form:  field:=expression "
            );
        }
      }
      else
      { 
        throw new IllegalArgumentException
          ("Binding '"+binding.getText()+"' is not in the form:  field:=expression");
      }
      
    }
  }
  
  /**
   * <p>Set assignments shorthand method. Each string in the specified
   *   array holds an 'expression' in the form 
   *   "fieldExpression = valueExpression", were fieldExpression is an 
   *   expression that resolves to a Field of the Query target, and 
   *   valueExpression resides in the parameter context of the Query.
   * </p>
   * 
   */
  public void setAssignments(String ... assignments)
    throws IllegalArgumentException
  {
  
    if (lhsExpressions==null)
    { lhsExpressions=new ArrayList<Expression<?>>();
    }
    
    if (rhsExpressions==null)
    { rhsExpressions=new ArrayList<Expression<?>>();
    }

    for (String assignment : assignments)
    { 
      int eqPos=assignment.indexOf('=');
      if (assignment.length()==eqPos+1
            || assignment.charAt(eqPos+1)=='='
         )
      { 
        throw new IllegalArgumentException
          ("Expression not of the form '.x=y...': '"+assignment+"'");
      }
      
      String text=null;
      try
      { 
        text=assignment.substring(0,eqPos);
        lhsExpressions.add(normalizeLHS(Expression.parse(text)));
        text=assignment.substring(eqPos+1);
        rhsExpressions.add(Expression.parse(text));
      }
      catch (ParseException x)
      { throw new IllegalArgumentException(text,x);
      }
    }
    
  }
  
  private Expression<?> normalizeLHS(Expression<?> expr)
  {
    if (expr.getRootNode() instanceof ResolveNode)
    {
      ResolveNode<?> resolver=(ResolveNode<?>) expr.getRootNode();
      if (resolver.getSource() instanceof CurrentFocusNode)
      { return Expression.create(resolver.getIdentifierName());
      }
    }
    return expr;
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
    

 
  public void setSource(Query source)
  { 
    type=source.getType();
    addSource(source);
  }
  
  /**
   *@return the Expression which constrains the result
   */
  public String[] getAssignments()
  { return assignments;
  }

  
  @Override
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding
     (Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new EquiJoinBinding<EquiJoin,T>(this,focus,store);
   
  }
  
  /**
   * <p>The LHSExpressions correspond to the data being queried. They are 
   *   dot-prefixed, and do not reference any non dot-prefixed names or
   *   anything else in the Focus chain.
   * </p>
   * 
   * @return a list of the LHSExpressions.
   */
  public ArrayList<Expression<?>> getLHSExpressions()
  { return lhsExpressions;
  }
  
  public void setLHSExpressions(Expression<?>[] lhsExpressions)
  { 
    this.lhsExpressions=new ArrayList<Expression<?>>(lhsExpressions.length);
    for (Expression<?> expr :lhsExpressions)
    { 
      expr=normalizeLHS(expr);
      if (debugLevel.canLog(Level.DEBUG))
      { log.debug("Equijoin: lhs+="+expr.toString());
      }
      
      this.lhsExpressions.add(expr);
    }
  }
  
  /**
   * <p>The RHSExpressions supply the data to be compared against their
   *   respective LHSExpressions and represent relative query parameters. They
   *   are not dot-prefixed and may reference the Focus chain.
   * </p>
   * 
   * @return a list of the LHSExpressions.
   */
  public ArrayList<Expression<?>> getRHSExpressions()
  { return rhsExpressions;
  }
  
  public void setRHSExpressions(Expression<?>[] rhsExpressions)
  { 
    this.rhsExpressions=new ArrayList<Expression<?>>(rhsExpressions.length);
    for (Expression<?> expr :rhsExpressions)
    { 
      if (debugLevel.canLog(Level.DEBUG))
      { log.debug("Equijoin: rhs+="+expr.toString());
      }
      this.rhsExpressions.add(expr);
    }
  }
  

  
  public void setExpressions
    (Expression<?>[] lhsExpressions,Expression<?>[] rhsExpressions)
  {
    setLHSExpressions(lhsExpressions);
    setRHSExpressions(rhsExpressions);
  }

    
}

class EquiJoinBinding<Tq extends EquiJoin,Tt extends Tuple>
  extends UnaryBoundQuery<Tq,Tt,Tt>
{
  private TeleFocus<Tt> focus;
  private Channel<Boolean>[] filter;
  private boolean resolved;
  
  public EquiJoinBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query,query.getSources(),paramFocus,store);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void resolve() throws DataException
  { 
    super.resolve();
    if (!resolved)
    {
      source.resolve();
      focus=new TeleFocus<Tt>(paramFocus,sourceChannel);
      try
      { 
        int i=0;
        ArrayList<Expression<?>> rhsExpressions=getQuery().getRHSExpressions();
        filter=new Channel[rhsExpressions.size()];
        for (Expression lhsExpression : getQuery().getLHSExpressions() )
        { 
          if (lhsExpression.getRootNode() instanceof ContextIdentifierNode)
          { 
            lhsExpression
              =Expression.create
                (new ResolveNode
                  (new CurrentFocusNode()
                  ,((ContextIdentifierNode) lhsExpression.getRootNode())
                    .getIdentifier()
                  )
                );
          }

          Expression<Boolean> comparison
            =Expression.<Boolean>create
              (lhsExpression.getRootNode()
                .isEqual(rhsExpressions.get(i).getRootNode())
              ,lhsExpression.getText()+"=="+rhsExpressions.get(i).getText()
              );
        
          filter[i++]=focus.<Boolean>bind(comparison);
          if (debugLevel.canLog(Level.DEBUG))
          { 
            log.debug("Added filter "+comparison);
            filter[i-1].setDebug(true);
          }
        }
        resolved=true;
      }
      catch (BindException x)
      { throw new DataException("Error binding constraints "+x,x);
      }
    }
  }
  
  
  @Override
  protected SerialCursor<Tt> newSerialCursor(SerialCursor<Tt> source)
    throws DataException
  { return new EquiJoinSerialCursor(source);
  }
  
  @Override
  protected ScrollableCursor<Tt> newScrollableCursor(ScrollableCursor<Tt> source)
    throws DataException
  { return new EquiJoinScrollableCursor(source);
  }

  protected class EquiJoinSerialCursor
    extends UnaryBoundQuerySerialCursor
  {
    public EquiJoinSerialCursor(SerialCursor<Tt> source)
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
        { log.fine("BoundEquiJoin: eod ");
        }
        return false;
      }
      
      boolean result=true;
      for (Channel<Boolean> element: filter)
      { 
        Boolean val=element.get();
        if (val==null || !val)
        { 
          if (debugFine)
          { log.fine("BoundEquiJoin: failed "+element+" "+t);
          }
          
          result=false;
          break;
        }
      }
       
      if (result)
      { 
        if (debugFine)
        { log.fine("BoundEquiJoin: passed "+t);
        }
        dataAvailable(t);
        return true;
      }
      else
      { 
//        System.err.println("BoundEquiJoin: filtered "+t);
        return false;
      }
    }
  }

  protected class EquiJoinScrollableCursor
    extends UnaryBoundQueryScrollableCursor
  {
    public EquiJoinScrollableCursor(ScrollableCursor<Tt> source)
      throws DataException
    { super(source);
    }

    @Override
    protected boolean integrate()
    { 
      Tt t=sourceChannel.get();
      if (t==null)
      { 
//        System.err.println("BoundEquiJoin: eod ");
        return false;
      }
      
      boolean result=true;
      for (Channel<Boolean> element: filter)
      { 
        Boolean val=element.get();
        if (val==null || !val)
        { 
          result=false;
          break;
        }
      }
       
      if (result)
      { 
//        System.err.println("BoundEquiJoin: passed "+t);
        dataAvailable(t);
        return true;
      }
      else
      { 
//        System.err.println("BoundEquiJoin: filtered "+t);
        return false;
      }
    }
  }
  
  
}
