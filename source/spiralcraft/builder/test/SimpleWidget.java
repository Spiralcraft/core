package spiralcraft.builder.test;

import spiralcraft.util.ArrayUtil;

import java.text.DateFormat;
import java.util.Date;

/**
 * Java class to use for builder test assemblies
 */
public class SimpleWidget
{
  private static int INSTANCE_ID=0;
  private final int _instanceId=INSTANCE_ID++;
  
  private String _title;
  private int _id;
  private boolean _on;
  private float _amount;
  private long _milliseconds;
  private SimpleWidget _friend;
  private SimpleWidget[] _children;
  private String[] _aliases;
  
  public SimpleWidget()
  { say("<init>");
  }

  public void setTitle(String val)
  { 
    _title=val;
    say("setTitle(\""+val+"\")");
  }
  
  public String getTitle()
  {
    say("getTitle()");
    return _title;
  }
  
  public void setId(int val)
  { 
    _id=val;
    say("setId("+val+")");
  }

  public void setOn(boolean val)
  { 
    _on=val;
    say("setOn("+(val?"true":"false")+")");
  }

  public void setAmount(float val)
  { 
    _amount=val;
    say("setAmount("+val+")");
  }

  public void setMilliseconds(long val)
  { 
    _milliseconds=val;
    say("setMilliseconds("+val+")");
  }

  public void setFriend(SimpleWidget val)
  { 
    _friend=val;
    say("setFriend("+val+")");
  }

  public void setChildren(SimpleWidget[] val)
  { 
    _children=val;
    say("setChildren("+ArrayUtil.formatToString(val,",",null)+")");
  }

  public void setAliases(String[] aliases)
  { 
    _aliases=aliases;
    say("setAliases("+ArrayUtil.formatToString(aliases,",","\"")+")");
    
  }
  
  public void setDateFormat(DateFormat format)
  {
    say("Date is: "+format.format(new Date()));
  }
  
  public void say(String text)
  { System.err.println(super.toString()+"."+text);
  }
  
  
  
  public String toString()
  { return super.toString()+":"+_instanceId+":\""+_title+"\"";
  }
}
