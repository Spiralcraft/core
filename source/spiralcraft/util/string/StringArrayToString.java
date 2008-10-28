package spiralcraft.util.string;

import spiralcraft.util.ArrayUtil;

public class StringArrayToString
  extends StringConverter<String[]>
{
  private boolean trim;
  
  public void setTrim(boolean trim)
  { this.trim=trim;
  }
  
  @Override
  public String toString(String[] val)
  { return ArrayUtil.format(val,",","");
  }

  @Override
  public String[] fromString(String val)
  {
    if (trim)
    {
      String[] vals=StringUtil.tokenize(val,",");
      for (int i=0;i<vals.length;i++)
      { vals[i]=vals[i].trim();
      }
      return vals;
    }
    else
    { return StringUtil.tokenize(val,",");
    }
  }
}
