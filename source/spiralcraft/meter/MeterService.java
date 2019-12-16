package spiralcraft.meter;

import java.util.Date;
import java.util.LinkedList;


import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanFocus;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.LogService;
import spiralcraft.service.Service;
import spiralcraft.service.ThreadService;
import spiralcraft.util.string.DateToString;

public class MeterService
  extends ThreadService
  implements Service
{

  { 
    setRunIntervalMs(5000);
    setAutoStart(true);
  }

  private String logName="telemetry";
  private LogService logService;
  private final MeterContext rootContext=new MeterContext(null,null);

  private DateToString dateConverter=new DateToString("yyyy-MM-dd'T'hh:mm:ss");
  private long fullReportIntervalMs=600000;
  private long lastFullReport=0;
  
  @Override
  public Focus<?> bindImports(Focus<?> focus) 
    throws ContextualException
  {
    
    this.logService=LangUtil.findInstance(LogService.class,focus);
    // Make sure it is available in the focus chain of the parent
    focus.addFacet
      (new BeanFocus<MeterContext>(rootContext));
    
    return super.bindImports(focus);
  }
  
  
  @SuppressWarnings({ "rawtypes", "unchecked"})
  @Override
  protected void runOnce()
  {
    Date time=new Date();
    LinkedList<Meter> meters=new LinkedList<>();
    rootContext.readMeters(meters);
    StringBuilder out=new StringBuilder();
    out.append("{")
      .append("t:\"")
      .append(dateConverter.toString(time))
      .append("\", m:[");
    boolean firstm=true;
    boolean fullReport=time.getTime()-lastFullReport >= fullReportIntervalMs;
    if (fullReport)
    { lastFullReport=time.getTime();
    }
    for (Meter meter:meters)
    {
      String meterOut=formatMeter(meter,fullReport);
      if (meterOut!=null)
      {
        if (!firstm)
        { out.append(",");
        }
        else
        { firstm=false;
        }
        out.append(meterOut);
      }
      
    }  
    out.append("]");  
    out.append("}");
    this.logService.write(this.logName, out.toString());
    
  }

  private String formatMeter(Meter meter,boolean fullReport)
  {
    boolean hasReport=false;
    StringBuilder mout=new StringBuilder();
    mout.append("{@path:\"")
      .append(meter.path.format("/"))
      .append("\"");
    for (Register reg:meter.registers.values())
    {
      boolean changed;
      long value;
      synchronized(reg)
      {
        changed=reg.hasChanged();
        value=reg.readValue();
      }
      if (fullReport || changed)
      {
        hasReport=true;
        mout.append(",");
        mout.append(reg.getName())
          .append(":\"")
          .append(value)
          .append("\"");
      }
    }
    mout.append("}");
    return hasReport?mout.toString():null;
  }
}
