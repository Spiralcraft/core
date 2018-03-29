package spiralcraft.io;

import java.io.IOException;
import java.io.InputStream;

import spiralcraft.time.Scheduler;
import spiralcraft.vfs.util.ByteArrayResource;

/**
 * Captures the output of a process into a buffer
 * 
 * @author mike
 *
 */
public class ProcessCapture
{
  private ProcessBuilder builder;
  private ByteArrayResource outBuff;
  private ByteArrayResource errBuff;

  public ProcessCapture(ProcessBuilder builder)
  { this.builder=builder;
  }
  
  public ByteArrayResource run(ByteArrayResource input)
    throws IOException,InterruptedException
  {
    Process process=builder.start();
    InputStream out=process.getInputStream();
    InputStream err=process.getErrorStream();
    outBuff=new ByteArrayResource();
    errBuff=new ByteArrayResource();
    StreamPump outPump=new StreamPump(out,outBuff.getOutputStream());
    Scheduler.instance().scheduleNow(outPump);
    StreamPump errPump=new StreamPump(err,errBuff.getOutputStream());
    Scheduler.instance().scheduleNow(errPump);
    process.waitFor();
    outPump.drainAndJoin(1000);
    errPump.drainAndJoin(1000);
    return outBuff;
    
  }
  
  public ByteArrayResource getOut()
  { return outBuff;
  }
  
  public ByteArrayResource getErr()
  { return errBuff;
  }
}