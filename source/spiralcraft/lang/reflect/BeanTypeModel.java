package spiralcraft.lang.reflect;

import java.io.IOException;
import java.net.URI;

import spiralcraft.beans.BeanInfoCache;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TypeModel;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Package;
import spiralcraft.vfs.Resource;

public class BeanTypeModel 
  extends TypeModel
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
    try
    {
      if (typeURI.getScheme()!=null && typeURI.getScheme().equals("context"))
      { 
        Resource classResource=Package.findResource(typeURI+".class");
        if (classResource!=null)
        {
        
          if (classResource.getURI().getScheme().equals("class"))
          { typeURI=URIUtil.removePathSuffix(classResource.getURI(),".class");
          }
        }
        else
        { return null;
        }
      }
    }
    catch (IOException x)
    { throw new BindException("Error resolving context URI "+typeURI);
    }
    
    Class<?> clazz=BeanInfoCache.<X>getClassForURI(typeURI);
    if (clazz!=null)
    { return BeanReflector.getInstance(clazz);
    }
    else
    { return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> Reflector<X> typeOf(X object)
  { 
    if (object==null)
    { return (Reflector<X>) BeanReflector.getInstance(Void.TYPE);
    }
    else
    { return BeanReflector.<X>getInstance(object.getClass());
    }
  }

  
  @Override
  public String getModelId()
  { return "java";
  }
  
  
}
