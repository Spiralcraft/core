package spiralcraft.builder.test;

/**
 * Java class to use for builder test assemblies
 */
public class SimpleWidget
{

  private String _title;
  private int _id;
  private boolean _on;
  private float _amount;
  private long _milliseconds;
  private SimpleWidget _friend;
  private SimpleWidget[] _children;
  
  public void setTitle(String val)
  { _title=val;
  }
  
  public void setId(int val)
  { _id=val;
  }

  public void setOn(boolean val)
  { _on=val;
  }

  public void setAmount(float val)
  { _amount=val;
  }

  public void setMilliseconds(long val)
  { _milliseconds=val;
  }

  public void setFriend(SimpleWidget val)
  { _friend=val;
  }

  public void setChildren(SimpleWidget[] val)
  { _children=val;
  }
}
