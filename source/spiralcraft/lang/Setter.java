package spiralcraft.lang;


import spiralcraft.log.ClassLog;

/**
 * Copies the value of a source channel to a target
 * 
 * @author mike
 */
public class Setter<T>
{
  private static final ClassLog log=ClassLog.getInstance(Setter.class);
  
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
  private boolean debug;
  
  public Setter(Channel<? extends T> source,Channel<T> target)
    throws BindException
  { 
    this.source=source;
    this.target=target;
    if (target!=null && !target.getReflector().isAssignableFrom(source.getReflector()))
    { 
      throw new BindException
        ("Cannot assign a "+source.getReflector().getTypeURI()
        +" to a location of type "+target.getReflector().getTypeURI()
        );
    }
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public boolean set()
  { 
    if (target!=null)
    { 
      if (debug)
      { log.fine("setting "+source.get()+" from "+source+" to "+target);
      }
      return target.set(source.get());
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
  
  @Override
  public String toString()
  { return super.toString()+"\r\n    source="+source+"\r\n   target="+target;
  }
}
