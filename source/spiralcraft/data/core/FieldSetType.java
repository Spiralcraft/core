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
  
  public FieldSetType(TypeResolver resolver,URI baseURI,FieldSet fieldSet)
  {
    
    super(resolver
        ,URI.create(baseURI+"-"+seq++)
        );
    this.fieldSet=fieldSet;

  }
  
  @Override
  public <X> Field<X> getField(String name)
  { return fieldSet.<X>getFieldByName(name);
  }
  
  @Override
  public FieldSet getFieldSet()
  { return fieldSet;
  }
  
  @Override
  public Scheme getScheme()
  { throw new UnsupportedOperationException("Use getFieldSet(): "+getURI());
  }
  
  @Override
  public void link()
    throws DataException
  {
    getTypeResolver().register(getURI(),this);
    super.link();
  }
}
