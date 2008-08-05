package spiralcraft.data.core;

import java.net.URI;

import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Scheme;
import spiralcraft.data.Tuple;
import spiralcraft.data.TypeResolver;


public class FieldSetType
  extends TypeImpl<Tuple>
{

  private static int seq=0;
  private FieldSet fieldSet;
  
  public FieldSetType(URI baseURI,FieldSet fieldSet)
  {
    
    super(TypeResolver.getTypeResolver()
        ,URI.create(baseURI+"-"+seq++)
        );
    this.fieldSet=fieldSet;

  }
  
  @Override
  public Field getField(String name)
  { return fieldSet.getFieldByName(name);
  }
  
  @Override
  public FieldSet getFieldSet()
  { return fieldSet;
  }
  
  @Override
  public Scheme getScheme()
  { throw new UnsupportedOperationException("Use getFieldSet()");
  }
  
  @Override
  public void link()
    throws DataException
  {
    TypeResolver.getTypeResolver().register(getURI(),this);
    super.link();
  }
}
