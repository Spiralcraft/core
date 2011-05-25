package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.log.ClassLog;

public class BindingChannel<T>
  extends SourcedChannel<T,T>
{
  private static final ClassLog log
    =ClassLog.getInstance(BindingChannel.class);
  

  public static final BindingChannel<?>[] bind
    (Expression<?>[] expressions,Focus<?> focus)
    throws BindException
  {
    if (expressions==null)
    { return null;
    }
    
    BindingChannel<?>[] ret=new BindingChannel<?>[expressions.length];
    int i=0;
    for (Expression<?> x:expressions)
    {
      Channel<?> channel=focus.bind(x);
      if (channel instanceof BindingChannel)
      { ret[i++]=(BindingChannel<?>) channel;
      }
      else
      {
        throw new BindException
          ("Expected expression in the form:  target:=sourceExpr");
      }
    }
    return ret;
  }
  
  public static final void bindTarget
    (BindingChannel<?>[] channels,Focus<?> focus)
    throws BindException
  {
    if (channels==null)
    { return;
    } 
    
    for (BindingChannel<?> channel:channels)
    { channel.bindTarget(focus);
    }
  }
  
  public static final void apply(BindingChannel<?>[] channels)
  {
    if (channels==null)
    { return;
    } 

    for (BindingChannel<?> channel:channels)
    { channel.get();
    }
  }
  
  public static final void applyReverse(BindingChannel<?>[] channels)
  {
    if (channels==null)
    { return;
    } 

    for (BindingChannel<?> channel:channels)
    { channel.applyReverse();
    }
  }
  
  public static final Channel<?>[] sources(BindingChannel<?>[] channels)
  { 
    Channel<?>[] sources=new Channel[channels.length];
    int i=0;
    for (BindingChannel<?> channel:channels)
    { sources[i++]=channel.getSource();
    }
    return sources;
  }
  
  private final Expression<T> sourceX;
  private final Expression<T> targetX;
  private Channel<T> targetChannel;
  private Binding<T> sourceBinding;


  public BindingChannel
    (Focus<?> focus,Expression<T> sourceX,Expression<T> targetX)
    throws BindException
  {
    super(focus.bind(sourceX));
    this.sourceX=sourceX;
    this.targetX=targetX;
  }


  @Override
  public <X> Channel<X> 
    resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  { 
    if (targetChannel==null)
    { throw new BindException("Target '"+targetX+"' not bound");
    }
    return super.resolve(focus,name,params);
  }

  @Override
  public Reflector<T> getReflector()
  { return super.getReflector();
  }

  public void bindTarget(Focus<?> targetFocus)
    throws BindException
  { 
    targetChannel=targetFocus.bind(targetX);

// 
//  writable is not a bind-time property
//     
//    if (!targetChannel.isWritable())
//    { 
//      throw new BindException
//        ("Target '"+targetX+"' is not writable: "
//        +targetChannel.toString()
//        );
//    }
    
    if (!targetChannel.getReflector()
          .isAssignableFrom(source.getReflector())
       )
    { 

      if (targetChannel.getContentType().isAssignableFrom(Binding.class))
      { 
        // Provide a dynamic reference to the specified source data.
        sourceBinding=new Binding<T>(source);
      }
      else
      {
      
        throw new BindException
          ("Argument type mismatch: "
            +targetChannel.getReflector().getTypeURI()
            +" ("+targetChannel.getContentType().getName()+")"
            +" is not assignable from `"+sourceX+"` "+source.getReflector().getTypeURI()
            +" ("+source.getContentType().getName()+")"
          );
      }
    }
  }

  public Channel<T> getTarget()
  { 
    assertTarget();
    return targetChannel;
  }
  
  public boolean applyReverse()
  { 
    assertTarget();
    return source.set(targetChannel.get());
  }
  

  
  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  {
    assertTarget();
    T val=sourceBinding!=null?(T) sourceBinding:source.get();
    if (!targetChannel.set(val))
    { log.warning("Bound assignment failed");
    }
    return val;
  }

  @Override
  protected boolean store(
    T val)
  throws AccessException
  { 
    assertTarget();
    if (targetChannel==null)
    { 
      throw new IllegalStateException
        ("Target '"+targetX+"' not bound. The ':=' operator may not be used"
        +" in this scenario"
        );
    }
    boolean set=targetChannel.set(val);
    source.set(val);
    return set;
  }

  private void assertTarget()
  {
    if (targetChannel==null)
    { 
      throw new IllegalStateException
        ("Target '"+targetX+"' not bound. The ':=' operator may not be used"
        +" in this scenario"
        );
    }
  }
  @Override
  public boolean isWritable()
  { return targetChannel.isWritable();
  }
}
