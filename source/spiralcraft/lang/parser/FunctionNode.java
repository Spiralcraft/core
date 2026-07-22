package spiralcraft.lang.parser;


import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.QName;
import spiralcraft.common.namespace.UnresolvedPrefixException;
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
import spiralcraft.util.refpool.URIPool;

public class FunctionNode
  extends Node
{
  private final Node context;
  private final Node body;
  private final String typeQName;
  private final String namespace;
  private final String suffix;
  private final URI uri;
  
  public FunctionNode(String typeQName,Node context,Node body)
    throws UnresolvedPrefixException  
  { 
    this.typeQName=typeQName;
    this.context=context;
    this.body=body;
    
    int colonPos=typeQName.indexOf(':');
    if (colonPos==0)
    { 
      this.uri=URIPool.create(typeQName.substring(1));    
      this.namespace=null;
      this.suffix=null;
    }
    else if (colonPos>0)
    {
      this.namespace=typeQName.substring(0,colonPos);
      this.suffix=typeQName.substring(colonPos+1);
      this.uri=resolveQName(namespace,suffix);      
    }
    else
    { 
      this.namespace=null;
      this.suffix=typeQName;
      this.uri=resolveQName(namespace,suffix);
    }    
  }

  public FunctionNode(String typeQName, String namespace, String suffix, URI uri, Node context, Node body)
  {
    this.typeQName=typeQName;
    this.namespace=namespace;
    this.suffix=suffix;
    this.context=context;
    this.uri=uri;
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
    { return new FunctionNode(typeQName,namespace,suffix,uri,context,body);
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
      Expression contextExpr=context!=null?Expression.create(context):null;
      Eval functor
        =context!=null
          ?new Eval(Expression.create(context),Expression.create(body))
          :new Eval(Expression.create(body));
      functor.setContextAliasURI(uri);
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

