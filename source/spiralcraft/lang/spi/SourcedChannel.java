package spiralcraft.lang.spi;

import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.util.tree.LinkedTree;

/**
 * 
 * An AbstractChannel bound to a source channel, which may provide access to
 *   source-specific metadata.
 * 
 * @author mike
 *
 * @param <Tinput> The type of the source channel
 * @param <Toutput> The type of this channel
 */
public abstract class SourcedChannel<Tinput,Toutput>
  extends AbstractChannel<Toutput>
{

  protected final Channel<Tinput> source;
  
  @SuppressWarnings("unchecked")
  public SourcedChannel(Channel<Tinput> source)
  { 
    super((Reflector<Toutput>) source.getReflector());
    this.source=source;
    this.context=source.getContext();
  }
  
  public SourcedChannel(Reflector<Toutput> reflector,Channel<Tinput> source)
  { 
    super(reflector);
    this.source=source;
    if (source!=null)
    { this.context=source.getContext();
    }
  }

  public SourcedChannel
    (Reflector<Toutput> reflector
    ,Channel<Tinput> source
    ,boolean isStatic
    )
  { 
    super(reflector);
    this.source=source;
    if (source!=null)
    { this.context=source.getContext();
    }
  }
  
  public Channel<Tinput> getSource()
  { return source;
  }
  
  /**
   * Resolve contextual metadata for this Channel from an appropriate provider
   *   in the Channel graph
   * 
   * @param <X>
   * @param focus
   * @param metadataTypeURI
   * @return
   * @throws BindException 
   */
  @Override
  public <X> Channel<X> resolveMeta(Focus<?> focus,URI metadataTypeURI) 
    throws BindException
  { return source.resolveMeta(focus,metadataTypeURI);
  }

  @SuppressWarnings("unchecked")
  @Override
  public LinkedTree<Channel<?>> trace(Class<Channel<?>> stop)
  { return new LinkedTree<Channel<?>>(this,source.trace(stop));
  }

}
