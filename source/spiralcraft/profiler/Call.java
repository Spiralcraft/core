package spiralcraft.profiler;

import java.io.IOException;

import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.log.ClassLog;
import spiralcraft.util.string.DoubleToDecimalString;
import spiralcraft.util.tree.AbstractNode;

public class Call
  extends AbstractNode<Call,Object>
{
  private static final ClassLog log=ClassLog.getInstance(Call.class);
  static DoubleToDecimalString timingFormat
    =new DoubleToDecimalString("######0.###");
  final long inTime;
  long outTime;
  final DeclarationInfo info;
  final String classname;
  Throwable throwable;
  String contextIdentifier;
  
  Call(String classname,DeclarationInfo info)
  { 
    this.info=info;
    this.classname=classname;
    this.inTime=System.nanoTime();
  }
  
  Call enter(String classname,DeclarationInfo info)
  { 
    final Call child=new Call(classname,info);
    child.contextIdentifier=contextIdentifier;
    addChild(child);
    return child;
  }
  
  Call exit(String classname,DeclarationInfo info,Throwable throwable)
  {
    if (info!=this.info || classname!=this.classname)
    { log.fine
        ("Mismatched profile frame: "+classname+":"+info
          +" != "+this.classname+":"+this.info
        );
    }
    this.outTime=System.nanoTime();
    this.throwable=throwable;
    return this.getParent();
  }
  
  void report(Appendable appendable,String prefix,int level)
    throws IOException
  { 
    long subtime=0;
    for (Call call:this)
    { subtime+=(call.outTime-call.inTime);
    }

    double totalTime=(outTime-inTime) / 1000D /1000D;
    double selfTime=(outTime-inTime-subtime) / 1000D / 1000D;
    appendable.append(prefix.repeat(level));
    appendable.append( 
      "S="+timingFormat.toString(selfTime) +"ms : "
      +"T="+timingFormat.toString(totalTime)+"ms "
      );
    if (contextIdentifier!=null)
    { appendable.append("[").append(contextIdentifier).append("] ");
    }
    if (classname!=null)
    { appendable.append(classname.toString()).append(":");
    }
    if (info!=null)
    { appendable.append(info.toString());
    }
    appendable.append("\r\n");
    for (Call call:this)
    { call.report(appendable, prefix, level+1);
    }
  }
}
