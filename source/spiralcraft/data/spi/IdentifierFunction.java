package spiralcraft.data.spi;

import spiralcraft.data.DataException;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.core.DeltaType;
import spiralcraft.data.session.BufferType;
import spiralcraft.lang.BindException;

public class IdentifierFunction<T>
  extends DataKeyFunction<KeyTuple,T>
{
  private final Type<T> masterType;
  private final Type<T> identifiedType;
  
  @SuppressWarnings("unchecked")
  public IdentifierFunction(Projection<T> projection)
      throws BindException
  { 
    super(projection);
    this.masterType=(Type<T>) projection.getSource().getType();
    if (masterType instanceof BufferType
        || masterType instanceof DeltaType
        )
    { identifiedType=(Type<T>) masterType.getArchetype();
    }
    else
    { identifiedType=masterType;
    }
  }  
 
  @Override
  public KeyIdentifier<T> createKeyTuple(Tuple projectionValue)
    throws DataException
  { return new KeyIdentifier<T>(identifiedType,projectionValue);
  }
}