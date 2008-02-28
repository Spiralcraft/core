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
  
  public void set()
  { target.set(source.get());
  }
}
