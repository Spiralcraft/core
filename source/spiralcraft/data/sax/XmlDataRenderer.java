package spiralcraft.data.sax;

import java.io.IOException;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.log.ClassLog;
import spiralcraft.text.Renderer;

public class XmlDataRenderer<T>
  implements Renderer,Contextual
{
  private static final ClassLog log
    =ClassLog.getInstance(XmlDataRenderer.class);
  
  private Binding<T> x;
  private Type<T> type;

  public void setX(Binding<T> x)
  { this.x=x;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void render(
    Appendable out)
    throws IOException
  { 
    DataWriter dataWriter=new DataWriter();
    
    Object val=x.get();
    if (val!=null)
    {
      try
      { 
        DataComposite data
          =(val instanceof DataComposite)
          ?(DataComposite) val
          :type.toData( (T) val);
          
        if (data!=null)
        { dataWriter.writeToWriter(out,data);
        }
        else
        { log.warning("Type.toData() returned null: "+type+" object="+val+" class="+val.getClass());
        }
      }
      catch (DataException x)
      { throw new RuntimeDataException("Error rendering '"+val+"'",x);
      }
    }

  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    if (x==null)
    { x=new Binding(focusChain.getSubject());
    }
    else
    { x.bind(focusChain);
    }
    
    if (x.getReflector() instanceof DataReflector<?>)
    { type=((DataReflector) x.getReflector()).getType(); 
    }
    else
    { 
      try
      { type=ReflectionType.<T>canonicalType(x.getReflector().getContentType());
      }
      catch (DataException ex)
      { 
        throw new BindException
          ("Error resolving ReflectionType for "
          +x.getReflector().getContentType()
          ,ex
          );
      }
    }
    return focusChain;
  }

}
