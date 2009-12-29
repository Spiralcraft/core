package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.log.ClassLog;

@SuppressWarnings("unchecked")
public class BindingChannel
  extends AbstractChannel
{
  private static final ClassLog log
    =ClassLog.getInstance(BindingChannel.class);
  
  public final Channel sourceChannel;
  public final Expression targetX;
  public Channel targetChannel;


  public BindingChannel
  (Channel sourceChannel,Expression targetX)
  {
    super(sourceChannel.getReflector());
    this.sourceChannel=sourceChannel;
    this.targetX=targetX;
  }


  @Override
  public Channel<?> resolve(Focus focus,String name,Expression[] params)
  throws BindException
  { 
    if (targetChannel==null)
    { throw new BindException("Target '"+targetX+"' not bound");
    }
    return super.resolve(focus,name,params);
  }

  @Override
  public Reflector getReflector()
  {
    if (targetChannel==null)
    { throw new IllegalStateException("Target '"+targetX+"' not bound");
    }
    return super.getReflector();
  }

  public void bindTarget(Focus targetFocus)
  throws BindException
  { targetChannel=targetFocus.bind(targetX);
  }

  @Override
  protected Object retrieve()
  {
    if (targetChannel==null)
    { throw new IllegalStateException("Target '"+targetX+"' not bound");
    }

    Object val=sourceChannel.get();
    if (!targetChannel.set(val))
    { log.warning("Bound assignment failed");
    }
    return val;
  }

  @Override
  protected boolean store(
    Object val)
  throws AccessException
  { 
    boolean set=targetChannel.set(val);
    sourceChannel.set(val);
    return set;
  }

  @Override
  public boolean isWritable()
  { return targetChannel.isWritable();
  }
}
