package spiralcraft.lang.parser;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Functor;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.kit.ConstantChannel;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.FocusChannel;
import spiralcraft.task.Eval;
import spiralcraft.task.Scenario;

public class FunctionNode
  extends Node
{
  private final Node context;
  private final Node body;
  private String typeQName;
  
  public FunctionNode(String typeQName,Node context,Node body)
  { 
    this.typeQName=typeQName;
    this.context=context;
    this.body=body;
  }

  @Override
  public Node copy(
    Object visitor)
  { 
    Node context=this.context.copy(visitor);
    Node body=this.body.copy(visitor);
    if (context==this.context && body==this.body)
    { return this;
    }
    else
    { return new FunctionNode(typeQName,context,body);
    }
  }

  @Override
  public Node[] getSources()
  { return new Node[] {context,body};
  }

  @Override
  public String reconstruct()
  { return "[#"+typeQName+context.reconstruct()+"]"+body.reconstruct();
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes"
    })
  @Override
  public Channel<?> bind(Focus<?> focus)
    throws BindException
  {
    try
    {
      Scenario functor
        =context!=null
          ?new Eval(Expression.create(context),Expression.create(body))
          :new Eval(Expression.create(body));
      Focus<?> functorFocus=functor.bind(focus);
      Channel<?> ret=new ConstantChannel(functor.reflect(),functor);
      ret.setContext(functorFocus);
      return new FocusChannel(ret,functorFocus);
    }
    catch (ContextualException x)
    { throw new BindException("Error binding function",x); 
    }
  }

  @Override
  public void dumpTree(
    StringBuffer out,
    String prefix)
  {
    out.append(prefix+"[#"+typeQName);
    context.dumpTree(out,prefix+"  ");
    out.append(prefix+"]");
    body.dumpTree(out,prefix+"  ");
    
  }

}

class FunctionChannel<T>
  extends AbstractChannel<Functor<T>>
{
  public FunctionChannel(Reflector<Functor<T>> reflector)
  { 
    
    super(reflector);
  }

  @Override
  protected Functor<T> retrieve()
  {
    
    
    return null;
  }

  @Override
  protected boolean store(
    Functor<T> val)
    throws AccessException
  {
    
    return false;
  }
}

