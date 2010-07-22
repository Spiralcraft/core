package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;

@SuppressWarnings("unchecked")
public class AssignmentChannel
  extends SourcedChannel
{
  private static final ClassLog log
    =ClassLog.getInstance(AssignmentChannel.class);
  
  public final Channel targetChannel;

  public AssignmentChannel
    (Channel sourceChannel,Channel targetChannel)
  {
    super(sourceChannel.getReflector(),sourceChannel);
    this.targetChannel=targetChannel;
    
  }
    
  @Override
  protected Object retrieve()
  {
    Object val=source.get();
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
    if (source.set(val))
    { 
      targetChannel.set(val);
      return true;
    }
    return false;
  }
      
  @Override
  public boolean isWritable()
  { return source.isWritable() && targetChannel.isWritable();
  }
}