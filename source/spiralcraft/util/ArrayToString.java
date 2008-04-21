package spiralcraft.util;

import java.lang.reflect.Array;

public final class ArrayToString<Tdata>
  extends StringConverter<Tdata[]>
{
  private final StringConverter<Tdata> converter;
  private final Class<Tdata> componentClass;
  
  @SuppressWarnings("unchecked")
  public ArrayToString(Class<Tdata> componentClass)
  { 
    this.converter
      =(StringConverter<Tdata>) StringConverter.getInstance(componentClass);
    this.componentClass=componentClass;
  }
  
  public String toString(Tdata[] val)
  { 
    if (val==null)
    { return null;
    }
    
    StringBuilder buf=new StringBuilder();
    for (Tdata item : val)
    { 
      if (buf.length()>0)
      { buf.append(",");
      }
      buf.append(converter.toString(item));
    }
    return buf.toString();
  }

  public Tdata[] fromString(String val)
  { 
    String[] strings=StringUtil.tokenize(val,",");
    Tdata[] data=(Tdata[]) Array.newInstance(componentClass,strings.length);
    for (int i=0;i<strings.length;i++)
    { data[i]=converter.fromString(strings[i]);
    }
    return data;
  }
}