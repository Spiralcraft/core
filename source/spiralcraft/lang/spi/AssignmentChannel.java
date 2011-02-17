package spiralcraft.lang.spi;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AssignmentChannel
  extends SourcedChannel
{
  private static final ClassLog log
    =ClassLog.getInstance(AssignmentChannel.class);
  
  public final Channel targetChannel;

  public AssignmentChannel
    (Channel sourceChannel,Channel targetChannel)
    throws BindException
  {
    super(sourceChannel.getReflector(),sourceChannel);
    this.targetChannel=targetChannel;
    if (targetChannel!=null 
        && !targetChannel.getReflector().isAssignableFrom
          (sourceChannel.getReflector())
       )
    { 
      throw new BindException
        ("Cannot assign a "+sourceChannel.getReflector().getTypeURI()
        +" to a location of type "+targetChannel.getReflector().getTypeURI()
        );
    }    
  }
    
  @Override
  protected Object retrieve()
  {
    Object val=source.get();
    if (!targetChannel.set(val))
    { log.warning("Assignment failed to "+targetChannel.toString());
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