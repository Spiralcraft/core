//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
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
  protected SimpleWidget _friend;
  private SimpleWidget[] _children;
  private String[] _aliases;
  private DateFormat _dateFormat;
  
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

  public int getId()
  { return _id;
  }
  
  public void setOn(boolean val)
  { 
    _on=val;
    say("setOn("+(val?"true":"false")+")");
  }

  public boolean getOn()
  { return _on;
  }
  
  public void setAmount(float val)
  { 
    _amount=val;
    say("setAmount("+val+")");
  }
  
  public float getAmount()
  { return _amount;
  }

  public void setMilliseconds(long val)
  { 
    _milliseconds=val;
    say("setMilliseconds("+val+")");
  }

  public long getMilliseconds()
  { return _milliseconds;
  }
  
  public void setFriend(SimpleWidget val)
  { 
    _friend=val;
    say("setFriend("+val+")");
  }

  public SimpleWidget getFriend()
  { return _friend;
  }
  
  public void setChildren(SimpleWidget[] val)
  { 
    _children=val;
    say("setChildren("+ArrayUtil.format(val,",",null)+")");
  }

  public SimpleWidget[] getChildren()
  { return _children;
  }
  
  public void setAliases(String[] aliases)
  { 
    _aliases=aliases;
    say("setAliases("+ArrayUtil.format(aliases,",","\"")+")");
    
  }

  public String[] getAliases()
  { return _aliases;
  }
  
  public void setDateFormat(DateFormat format)
  {
    _dateFormat=format;
    say("Date is: "+format.format(new Date()));
  }

  public DateFormat getDateFormat()
  { return _dateFormat;
  }
  
  
  public void say(String text)
  { System.err.println(super.toString()+"."+text);
  }
  
  
  
  @Override
  public String toString()
  { 
    return super.toString()
      +":"+_instanceId+":\""+_title+"\"";
  }
}
