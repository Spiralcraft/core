package spiralcraft.data.lang;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.GenericReflector;
import spiralcraft.log.ClassLog;
import spiralcraft.data.Type;

/**
 * Reflects a Type. 
 * 
 * @author mike
 *
 * @param <T>
 */
public class TypeReflector<Z>
  extends GenericReflector<Type<Z>>
{

  private static final ClassLog log
    =ClassLog.getInstance(TypeReflector.class);
  private final Type<Z> type;
  private Reflector<Z> dataReflector;
  
  public TypeReflector(Type<Z> type)
  { 
    super(BeanReflector.<Type<Z>>getInstance(type.getClass()));
    this.type=type;
  }
  
  public <X> Channel<X> resolve
    (Channel<Type<Z>> source,Focus<?> focus,String name,Expression<?>[] args)
  throws BindException
  {
    Channel<X> ret;
//    log.fine("Resolving "+name+" in ("+this.toString()+")");
    ret=super.resolve(source,focus,name,args);
//    log.fine("Result of "+name+" is "+ret);
    return ret;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <X> Channel<X> resolveMeta
      (Channel<Type<Z>> source,Focus<?> focus,String name,Expression<?>[] args)
    throws BindException
  {
    String metaName=name.substring(1);
    Channel<X> ret=null;
    if (metaName.startsWith("@"))
    {
      if (dataReflector==null)
      { dataReflector=DataReflector.getInstance(type);
      }
//      log.fine("Resolving meta "+metaName+" in "+dataReflector+" ("+this.toString()+")");
      ret=dataReflector.resolve
         (dataReflector.getStaticChannel(focus)
         , focus
         , metaName.substring(1)
         , args
         );
//      log.fine("Result of "+metaName+" is "+ret);
      if (ret!=null)
      { return ret;
      }
    }

//    log.fine("Resolving meta "+metaName+" in bean ("+this.toString()+")");
    ret=(Channel<X>) super.resolveMeta(source, focus, name, args);
//    log.fine("Result of +"+metaName+" is "+ret);
    if (ret!=null)
    { return ret;
    }
    
    return null;
    
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+type.getURI();
  }
}
