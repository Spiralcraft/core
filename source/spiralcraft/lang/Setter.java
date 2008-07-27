package spiralcraft.lang;

/**
 * Copies the value of a source channel to a target
 * 
 * @author mike
 */
public class Setter<T>
{
  public static final void applyArray(Setter<?>[] setters)
  {
    if (setters!=null)
    { 
      for (Setter<?> setter:setters)
      { setter.set();
      }
    }
  }

  public static final void applyArrayIfNull(Setter<?>[] setters)
  {
    if (setters!=null)
    { 
      for (Setter<?> setter: setters)
      { 
        if (setter.getTarget().get()==null)
        { setter.set();
        }
      }
    }
  }

  private Channel<? extends T> source;
  private Channel<T> target;
  
  public Setter(Channel<? extends T> source,Channel<T> target)
  { 
    this.source=source;
    this.target=target;
  }
  
  public boolean set()
  { 
    if (target!=null)
    { return target.set(source.get());
    }
    else
    { 
      source.get();
      return true;
    }
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
