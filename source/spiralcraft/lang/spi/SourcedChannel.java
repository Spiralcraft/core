package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

public abstract class SourcedChannel<Tinput,Toutput>
  extends AbstractChannel<Toutput>
{

  protected final Channel<Tinput> source;
  
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
  
  @Override
  public String toString()
  { 
    return super.toString()
      +":"+getReflector()
      +"\r\n   <-- "
      +(source!=null?source.toString():"(no source)");
  }

}
