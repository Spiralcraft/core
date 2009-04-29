package spiralcraft.data.core;


import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Scan;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;

import spiralcraft.rules.AbstractRule;
import spiralcraft.rules.RuleChannel;
import spiralcraft.rules.Violation;


public class UniqueRule<T extends Tuple>
  extends AbstractRule<Type<T>,T>
{

  private static final ClassLog log
    =ClassLog.getInstance(UniqueRule.class);
  
  private Query query;
  private Field<?> field;
  
  public UniqueRule(Type<T> type,Field<?> field)
  { 
    
    setContext(type);
    this.field=field;
    
    EquiJoin ej=new EquiJoin();
    
    Expression<?>[] rhsExpressions=new Expression<?>[1];
    rhsExpressions[0]=Expression.create(field.getName());
    
    Expression<?>[] lhsExpressions=new Expression<?>[1];
    lhsExpressions[0]=Expression.create("."+field.getName());
    
    ej.setExpressions
      (lhsExpressions
      ,rhsExpressions
      );
    ej.setSource(new Scan(type));
    ej.setDebug(debug);
    query=ej;
    
  }    
  
  
  @Override
  public Channel<Violation<T>> bindChannel(Focus<T> focus)
    throws BindException
  { return new UniqueRuleChannel(focus);
  }
    
  class UniqueRuleChannel
    extends RuleChannel<T>
  {

    private final Channel<T> source;
    private final Channel<?> fieldChannel;
    
    private BoundQuery<?,T> boundQuery;
    
    @SuppressWarnings("unchecked")
    public UniqueRuleChannel(Focus<T> focus)
      throws BindException
    { 
      source=focus.getSubject();
      fieldChannel=field.bindChannel((Focus<Tuple>) focus);
      
      try
      { 
        boundQuery
          =(BoundQuery<?,T>) query.bind(focus);
      }
      catch (DataException x)
      { 
        throw new BindException
          ("Error binding field unique query for "+context.getURI(),x);
      }
    }      
          
    @Override
    protected Violation<T> retrieve()
    {
      if (fieldChannel.get()==null)
      { 
        if (debug)
        { log.fine("Unique field "+field.getURI()+" = null");
        }
        return null;
      }
      
      if (debug)
      {
        log.fine
          ("Checking unique field "+field.getURI()
          +" = ["+fieldChannel.get()+"]"
          );
      }
      try
      {
        SerialCursor<T> cursor=boundQuery.execute();
        try
        {
          while (cursor.next())
          {
            if (debug)
            { 
              log.fine("Checking against "+cursor.getTuple().toText("| "));
              log.fine(source.get().getId()+" ? "+cursor.getTuple().getId());
            }
            if (!cursor.getTuple().getId().equals(source.get().getId()))
            { 
              return new Violation<T>
                (UniqueRule.this,field.getTitle()+" must be unique");
            }
          }
        }
        finally
        { cursor.close();
        }
      }
      catch (DataException x)
      { 
        throw new AccessException
          ("Error querying for unique "+field.getName(),x);
      }
      return null;
      
    }
  }
 
}
