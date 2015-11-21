package spiralcraft.data.core;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.util.InstanceResolver;

import java.net.URI;

/**
 * A Prototype is a type that is based on a defined data object. The archetype
 *   a Prototype is the type of the data object that defines the Prototype.
 * 
 * @author mike
 *
 * @param <T>
 */
public class Prototype<T>
  extends TypeImpl<T>
{
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <T> Type<T> create(TypeResolver resolver,URI uri,Object object)
    throws DataException
  {
    Prototype proto=new Prototype<T>(resolver,uri);
    if (object instanceof DataComposite)
    {
      proto.setArchetype(((DataComposite) object).getType());
      proto.nativeClass=proto.getArchetype().getNativeClass();
      proto.protoComposite=(DataComposite) object;
      proto.constructed();
    }
    else
    { 
      proto.setArchetype(ReflectionType.canonicalType(object.getClass()));
      proto.nativeClass=object.getClass();
      proto.protoObject=object;
    }
    return proto;
  }
  
    
  private DataComposite protoComposite;
  private T protoObject;
  
  public Prototype(TypeResolver resolver,URI uri)
  { super(resolver,uri);
  }
  
  private void constructed()
  {
  }
  
  @Override
  public void init(DataComposite composite)
    throws DataException
  { 
    if (protoComposite==null)
    { return;
    }
    
    if (composite instanceof EditableTuple)
    { ((EditableTuple) composite).copyFrom((Tuple) protoComposite);
    }
  
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public T fromData(DataComposite composite,InstanceResolver resolver) 
    throws DataException
  { 
    // XXX For now, this ignores the actual composite. We need to overlay
    //   the incoming data onto a copy of the protoComposite for this to
    //   work when additional definition is included at the referencing
    //   location.
    if (protoComposite!=null)
    { 
      T ret = (T) archetype.fromData(protoComposite,resolver);
      return ret;
    }
    else
    { 
      // TODO: Make a copy so we don't inadvertently share.
      return protoObject;
    }
  }
  
}