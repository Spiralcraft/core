package spiralcraft.data.lang;

import java.net.URI;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.TypeResolver;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TypeModel;
//import spiralcraft.log.ClassLog;
// import spiralcraft.log.Level;
import spiralcraft.util.refpool.URIPool;

public class DataTypeModel
  extends TypeModel
{
//  private static final ClassLog log
//    =ClassLog.getInstance(DataTypeModel.class);
//  private static final Level debugLevel
//    =ClassLog.getInitialDebugLevel(DataTypeModel.class,null);
  
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
    String scheme=typeURI.getScheme();
    
    if (scheme!=null && scheme.equals("data"))
    { typeURI=URIPool.create(typeURI.getRawSchemeSpecificPart());
    }
    
    Type<?> type=null;
    try
    { type=TypeResolver.getTypeResolver().resolve(typeURI);
    }
    catch (TypeNotFoundException x)
    {
    }
    catch (DataException x)
    { 
      if (x.getCause() instanceof TypeNotFoundException)
      {
      
      }
      else
      { throw new BindException("Error resolving type "+typeURI,x);
      }
    }
    
    if (type!=null)
    { return DataReflector.<X>getInstance(type);
    }
    else
    { return null;
    }

  }

  @Override
  public <X> Reflector<X> typeOf(X object)
    throws BindException
  {
    if (object==null)
    { return null;
    }
    if (!(object instanceof DataComposite))
    { return null;
    }
    return DataReflector.getInstance(((DataComposite) object).getType());
        
  }

  @Override
  public String getModelId()
  { return "spiralcraft";
  }
    


}
