package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;

@SuppressWarnings("unchecked")
public class AssignmentChannel
  extends AbstractChannel
{
  private static final ClassLog log
    =ClassLog.getInstance(AssignmentChannel.class);
  
  public final Channel sourceChannel;
  public final Channel targetChannel;

  public AssignmentChannel
    (Channel sourceChannel,Channel targetChannel)
  {
    super(sourceChannel.getReflector());
    this.sourceChannel=sourceChannel;
    this.targetChannel=targetChannel;
    
  }
    
  @Override
  protected Object retrieve()
  {
    Object val=sourceChannel.get();
    if (!targetChannel.set(val))
    { log.warning("Assignment failed");
    }
    return val;
  }

  @Override
  protected boolean store(
    Object val)
    throws AccessException
  {
    if (sourceChannel.set(val))
    { 
      targetChannel.set(val);
      return true;
    }
    return false;
  }
      
  @Override
  public boolean isWritable()
  { return sourceChannel.isWritable() && targetChannel.isWritable();
  }
}