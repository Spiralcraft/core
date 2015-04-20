package spiralcraft.data.xml;

import java.util.ArrayList;
import spiralcraft.data.DataException;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.EquiJoin;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayUtil;

public class BoundIndexScan
  extends BoundQuery<EquiJoin,Tuple>
{
  private final XmlQueryable source;
  private final Projection<Tuple> projection;
  private final Channel<?>[] parameters;
  private final boolean debugTrace;
  
  public BoundIndexScan(EquiJoin ej,Focus<?> context,XmlQueryable source)
    throws DataException
  { 
    super(ej,context);
    this.source=source;
    // Create a focus to resolve all the RHSExpressions
    Focus<?> focus=new TeleFocus<Void>(context,null);
    
    ArrayList<Expression<?>> lhsExpressions=ej.getLHSExpressions();

    // TODO: Check keys here: This should really be 
    //   getResultType().getProjection
    //       (lhsExpressions.toArray(new Expression[0]))
    
    projection
      =source.getResultType().getScheme().getProjection
        (lhsExpressions.toArray(new Expression<?>[0]));

    ArrayList<Expression<?>> rhsExpressions=ej.getRHSExpressions();
    parameters=new Channel<?>[rhsExpressions.size()];
    int i=0;
    for (Expression<?> expr : rhsExpressions)
    { 
      try
      { parameters[i]=focus.bind(expr);
      }
      catch (BindException x)
      { 
        throw new DataException
          ("Error binding EquiJoin parameter expression "+expr,x);
      }
      
      Reflector<?> paramReflector=parameters[i].getReflector();
      Reflector<?> fieldReflector
        =projection.getFieldByIndex(i).getContentReflector();
      if (paramReflector!=fieldReflector
          && paramReflector!=null
          && fieldReflector!=null
          && !paramReflector.isAssignableFrom(fieldReflector)
          && !fieldReflector.isAssignableFrom(paramReflector)
          )
      { 
        throw new DataException
          ("Types are not comparable: "
            +expr.toString()+"("+paramReflector.getTypeURI()+")"
            +" cannot be compared to "
            +projection.getFieldByIndex(i).getURI()
            +"("+fieldReflector.getTypeURI()+")"
          );
      }
      i++;
    }
    
    debugTrace=debugLevel.canLog(Level.TRACE);
  }
  
  
  @Override
  public String toString()
  { return super.toString()+" projection="+projection.toString();
  }
  
  @Override
  public SerialCursor<Tuple> doExecute() throws DataException
  { 
    
    Object[] parameterData=new Object[parameters.length];
    for (int i=0;i<parameters.length;i++)
    { parameterData[i]=parameters[i].get();
    }
    KeyTuple key=new KeyTuple(projection,parameterData,true);
    
    
    if (debugTrace)
    { 
      log.trace
        (toString()+": Executing BoundIndexScan of "+getType().getURI()+"#"
          +projection.toString()
          +" with ["+ArrayUtil.format(parameterData,",","")+"]"
        );
    }      
    return source.getCursor(projection,key);

  } 
}
