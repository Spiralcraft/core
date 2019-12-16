package spiralcraft.meter;

import java.util.LinkedHashMap;

import spiralcraft.log.ClassLog;
import spiralcraft.util.Path;

/**
 * Tracks a set of runtime stats for an instance of a component
 * 
 * @author mike
 *
 */
public class Meter
{
  private static final ClassLog log=ClassLog.getInstance(MeterContext.class);
//  private final String name;
  final Path path;
//  private final MeterContext context;
  final LinkedHashMap<String,Register> registers=new LinkedHashMap<>();
  
  public Meter(MeterContext context,String name)
  { 
//    this.name=name;
//    this.context=context;
    this.path=context.path.append(name);

    log.fine("Created Meter '"+name+"': "+path.format('/')+" in "+context.toString());    
  }

  public Register register(String name)
  { 
  
    Register ret=registers.get(name);
    if (ret!=null)
    { return ret;
    }
    synchronized (this)
    {
      ret=registers.get(name);
      if (ret!=null)
      { return ret;
      }
      ret=new Register(name);
      registers.put(name,ret);
      return ret;
    }
  }
  
  public void update(String name,long value)
  { 
    Register reg=registers.get(name);
    if (reg==null)
    { 
      throw new IllegalArgumentException
        ("No register named '"+name+"' in meter "+path.format("/"));
    }
    reg.setValue(value);
  }
  
  public String toString()
  { return super.toString()+": path="+path.format("/");
  }
}
