package spiralcraft.lang.reflect;

import java.net.URI;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TypeModel;

public class BeanTypeModel 
  implements TypeModel
{
  private static final BeanTypeModel instance
    =new BeanTypeModel();
  
  public static final BeanTypeModel getInstance()
  { return instance;
  }
  
  @Override
  public <X> Reflector<X> findType(URI typeURI)
    throws BindException
  { 
    Class<?> clazz=BeanInfoCache.<X>getClassForURI(typeURI);
    if (clazz!=null)
    { return BeanReflector.getInstance(clazz);
    }
    else
    { return null;
    }
  }

  @Override
  public String getModelId()
  { return "java";
  }
}
