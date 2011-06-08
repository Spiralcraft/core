package spiralcraft.data.spi;

import spiralcraft.data.DataException;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.BindException;

public class IdentifierFunction<T>
  extends DataKeyFunction<KeyTuple,T>
{
  private final Type<T> masterType;
  
  @SuppressWarnings("unchecked")
  public IdentifierFunction(Projection<T> projection)
      throws BindException
  { 
    super(projection);
    this.masterType=(Type<T>) projection.getSource().getType();
    
  }  
 
  @Override
  public KeyIdentifier<T> createKeyTuple(Tuple projectionValue)
    throws DataException
  { return new KeyIdentifier<T>(masterType,projectionValue);
  }
}