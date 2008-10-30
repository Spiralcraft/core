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
  { return BeanReflector.getInstance(BeanInfoCache.<X>getClassForURI(typeURI));
  }

  @Override
  public String getModelId()
  { return "java";
  }
}
