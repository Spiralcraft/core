package spiralcraft.meter;

import java.util.LinkedList;

import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.Path;

public class MeterContext
{
  private static final ClassLog log=ClassLog.getInstance(MeterContext.class);
//  private final String name;
//  private final MeterContext parentContext;
  final Path path;
  private MeterContext[] children=new MeterContext[0];
  private Meter[] meters=new Meter[0];
  
  protected MeterContext(MeterContext parentContext,String name)
  { 
//    this.name=name;
//    this.parentContext=parentContext;
    this.path=parentContext==null
              ?name!=null
                ?Path.ROOT_PATH.append(name)
                :Path.ROOT_PATH
              :parentContext.path.append(name);
    log.fine("Created MeterContext '"+name+"': "+path.format('/')
              +(parentContext!=null
                ?(" in "+parentContext.toString())
                :"as root context"
                )
             );
  }
  
  /**
   * Obtain a MeterContext that is a child of this context, with the given name.
   * 
   * Effectively adds the name as element to the path that prefixes the variable
   *   being reported. 
   * 
   * @param name
   * @return
   */
  public MeterContext subcontext(String name)
  { 
    MeterContext child=new MeterContext(this,name);
    children=ArrayUtil.append(children,child);
    return child;
  }
  
  /**
   * Register a Meter to report a set of runtime stat.
   * 
   * @param contextName
   * @return
   */
  public Meter meter(String meterName)
  { 
    Meter meter=new Meter(this,meterName);
    meters=ArrayUtil.append(meters,meter);
    return meter;
  }
  
  void readMeters(LinkedList<Meter> meterList)
  {
    for (Meter m : meters)
    { meterList.add(m);
    }
    for (MeterContext context: children)
    { context.readMeters(meterList);
    }
  }
  
  public String toString()
  { return super.toString()+": path="+path.format("/");
  }
}
