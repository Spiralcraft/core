package spiralcraft.lang.spi;

import spiralcraft.common.Coercion;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.kit.CoercionChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.lang.NumericCoercion;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AssignmentChannel
  extends SourcedChannel
{
  private static final ClassLog log
    =ClassLog.getInstance(AssignmentChannel.class);
  
  public final Channel targetChannel;
  public final Channel translator;

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
      Coercion coercion
        =NumericCoercion.instance(targetChannel.getContentType());
    
      if (coercion==null)
      {
        throw new BindException
          ("Cannot assign a "+sourceChannel.getReflector().getTypeURI()
          +" to a location of type "+targetChannel.getReflector().getTypeURI()
          );
      }
      else
      { 
        translator
          =new CoercionChannel
            (targetChannel.getReflector()
            ,sourceChannel
            ,coercion
            );
        
      }
    }
    else
    { translator=sourceChannel;
    }
  }
    
  @Override
  protected Object retrieve()
  {
    Object val=translator.get();
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
    if (translator.set(val))
    { 
      targetChannel.set(val);
      return true;
    }
    return false;
  }
      
  @Override
  public boolean isWritable()
  { return translator.isWritable() && targetChannel.isWritable();
  }
}