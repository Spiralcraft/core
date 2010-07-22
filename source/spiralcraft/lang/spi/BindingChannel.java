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
  

  private final Expression<T> targetX;
  private Channel<T> targetChannel;
  private Binding<T> sourceBinding;


  public BindingChannel
    (Channel<T> sourceChannel,Expression<T> targetX)
  {
    super(sourceChannel.getReflector(),sourceChannel);

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
            +" is not assignable from "+source.getReflector().getTypeURI()
            +" ("+source.getContentType().getName()+")"
          );
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T retrieve()
  {
    if (targetChannel==null)
    { throw new IllegalStateException("Target '"+targetX+"' not bound");
    }

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
    boolean set=targetChannel.set(val);
    source.set(val);
    return set;
  }

  @Override
  public boolean isWritable()
  { return targetChannel.isWritable();
  }
}
