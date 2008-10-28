package spiralcraft.data.util;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.util.string.StringConverter;

public class TypeStringConverter<T>
  extends StringConverter<T>
{

  private Type<T> type;
  
  public TypeStringConverter(Type<T> type)
  { this.type=type;
  }
  
  @Override
  public T fromString(
    String val)
  {
    try
    { return type.fromString(val);
    }
    catch (DataException x)
    { throw new IllegalArgumentException(val,x);
    }
  }
  
  @Override
  public String toString(T val)
  { return type.toString(val);
  }

}
