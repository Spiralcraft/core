package spiralcraft.data.lang;

import java.util.ArrayList;

import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.session.BufferTuple;
import spiralcraft.data.session.BufferType;
import spiralcraft.data.session.DataSession;
import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Constructs a Tuple and initializes field values to the result of
 *   Field.newExpression
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class TupleConstructorChannel<T extends Tuple>
  extends AbstractChannel<T>
{
  private final ThreadLocalChannel<T> local;
  private final Setter<?>[] setters;
  private final Type<T> type;
  private final boolean buffer;
  private final Channel<DataSession> dataSessionChannel;
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public TupleConstructorChannel(TupleReflector<T> reflector,Focus<?> context)
    throws BindException
  { 
    super(reflector);
    local=new ThreadLocalChannel<T>(reflector);
    this.context=context;
    Focus<T> focus=context.chain(local);
      
    ArrayList<Setter<?>> setterList=new ArrayList<Setter<?>>();
    for (Field<?> field: reflector.getFieldSet().fieldIterable())
    {
      Expression<?> ne=field.getNewExpression();
      if (ne!=null)
      { 
        Setter<?> setter
          =new Setter
            (focus.bind(ne)
            ,field.bindChannel((Channel<Tuple>) local, focus,null)
            );
        
        setterList.add(setter);
        
      }
    }
    if (!setterList.isEmpty())
    { setters=setterList.toArray(new Setter[setterList.size()]);
    }
    else
    { setters=null;
    }
    
    buffer=((Type) reflector.getType()) instanceof BufferType;
    if (buffer)
    { 
      this.type=reflector.getType();
      dataSessionChannel=DataSession.findChannel(context);
      if (dataSessionChannel==null)
      { throw new BindException("Buffer requires a DataSession in context");
      }
    }
    else
    { 
      this.type=reflector.getType();
      dataSessionChannel=null;
    }
    
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  { 
    T tuple =
      buffer
        ?(T) new BufferTuple(dataSessionChannel.get(),type)
        :(T) new EditableArrayTuple(type);
    
    try
    { type.init(tuple);
    }
    catch (DataException x)
    { throw new AccessException("Error initializing "+type.getURI(),x);
    }
    
    if (setters!=null)
    { 
      local.push(tuple);
      try
      { Setter.applyArray(setters);
      }
      finally
      { local.pop();
      }
    }
    return tuple;
  }

  @Override
  protected boolean store(
    T val)
    throws AccessException
  { 
    throw new UnsupportedOperationException
      ("Cannot assign a value to the output of a constructor");
  }

}
