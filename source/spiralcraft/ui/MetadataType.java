package spiralcraft.ui;

import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.lang.Reflector;

public enum MetadataType
{
  FIELD(URI.create("class:/spiralcraft/ui/FieldMetadata"))
  ;
  
  public final URI uri;
  public final Type<Tuple> type;
  public final Reflector<Tuple> reflector;
  
  private MetadataType(URI uri)
  { 
    try
    {
      this.uri=uri;
      this.type=Type.resolve(uri);
      this.reflector=DataReflector.getInstance(type);
    }
    catch (ContextualException x)
    { throw new RuntimeException(x);
    }
    
  }
  
  
  
}
