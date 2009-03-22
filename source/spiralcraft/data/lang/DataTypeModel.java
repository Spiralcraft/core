package spiralcraft.data.lang;

import java.net.URI;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TypeModel;

public class DataTypeModel
  extends TypeModel
{
  private static final DataTypeModel instance
    =new DataTypeModel();
  
  public static final DataTypeModel getInstance()
  { return instance;
  }

  @Override
  public <X> Reflector<X> findType(
    URI typeURI)
    throws BindException
  { 
    Type<?> type=null;
    try
    { type=TypeResolver.getTypeResolver().resolve(typeURI);
    }
    catch (DataException x)
    {
    }
    if (type!=null)
    { return DataReflector.<X>getInstance(type);
    }
    else
    { return null;
    }

  }

  @Override
  public String getModelId()
  { return "spiralcraft";
  }
    


}
