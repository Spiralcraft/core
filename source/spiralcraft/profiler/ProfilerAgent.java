package spiralcraft.profiler;

import java.io.IOException;

import spiralcraft.common.declare.DeclarationInfo;
// import spiralcraft.log.ClassLog;

/**
 * Records the runtime call tree at an application level 
 * 
 * @author mike
 */
public class ProfilerAgent
{
  // private static final ClassLog log=ClassLog.getInstance(ProfilerAgent.class);
  private Call rootCall;
  private volatile Call call=rootCall;

  public void start()
  { 
    rootCall=new Call("ROOT",null);
    call=rootCall;
  }

  public void stop()
  { rootCall.exit("ROOT", null,null);
  }
  
  /**
   * Enter a call, adding a new call node to the call tree
   * @param info
   */
  public void enter(String classname,DeclarationInfo info)
  { 
//    log.fine(this.toString()+": Entering "+classname+":"+info);
    call=call.enter(classname,info);
//    log.fine(this.toString()+": Entered "+classname+":"+info);
  }
  
  /**
   * Exit a call, finalizing the current call node in the tree
   * @param info
   * @param throwable
   */
  public void exit(String classname,DeclarationInfo info,Throwable throwable)
  { 
//    log.fine(this.toString()+": Exiting "+classname+":"+info);
    call=call.exit(classname,info,throwable);
//    log.fine(this.toString()+": Exited "+classname+":"+info);
  }
  
  /**
   * An identifier that will be associated with future children of the current call
   */
  public void setContextIdentifier(String cid)
  { call.contextIdentifier=cid;
  }
  
  public Appendable generateReport(Appendable appendable,String indent)
    throws IOException
  {
    if (indent==null)
    { indent="| ";
    }
    rootCall.report(appendable,indent,0);
    return appendable;
  }
}
