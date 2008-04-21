package spiralcraft.lang;

/**
 * Copies the value of a source channel to a target
 * 
 * @author mike
 */
public class Setter<T>
{
  private Channel<? extends T> source;
  private Channel<T> target;
  
  public Setter(Channel<? extends T> source,Channel<T> target)
  { 
    this.source=source;
    this.target=target;
  }
  
  public boolean set()
  { return target.set(source.get());
  }
  
  public Channel<? extends T> getSource()
  { return source;
  }
  
  public Channel<T> getTarget()
  { return target;
  }
  
  public String toString()
  { return super.toString()+"\r\n    source="+source+"\r\n   target="+target;
  }
}
