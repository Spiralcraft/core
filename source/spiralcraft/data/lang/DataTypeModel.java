package spiralcraft.data.lang;

import java.net.URI;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TypeModel;

public class DataTypeModel
  implements TypeModel
{
  private static final DataTypeModel instance
    =new DataTypeModel();
  
  public static final DataTypeModel getInstance()
  { return instance;
  }
  
  @Override
  public <X> Reflector<X> findType(URI typeURI)
    throws BindException
  { 
    try
    { return DataReflector.getInstance(Type.resolve(typeURI));
    }
    catch (DataException x)
    { throw new BindException("Error resolving type "+typeURI);
    }
  }

  @Override
  public String getModelId()
  { return "spiralcraft.data";
  }

}
