package spiralcraft.data.core;

import java.util.ArrayList;
import java.util.Iterator;

import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.util.IteratorChain;

public class UnifiedFieldSet
  implements FieldSet
{
  private final Type<?> type;
  private final FieldSet base;
  private final int fieldCount;

  
  public UnifiedFieldSet(Type type)
  { 
    this.type=type;
    Type<?> baseType=type.getBaseType();
    if (baseType!=null)
    { this.base=baseType.getFieldSet();
    }
    else
    { this.base=null;
    }
    if (type.getScheme()!=null)
    {
      fieldCount=type.getScheme().getFieldCount()
        +(base!=null?base.getFieldCount():0);
    }
    else
    {
      fieldCount=(base!=null?base.getFieldCount():0);
    }
    
  }
  
  
  @Override
  public Iterable<? extends Field> fieldIterable()
  { 
    return new Iterable<Field>()
    {

      @SuppressWarnings("unchecked")
      @Override
      public Iterator<Field> iterator()
      { 
        if (base!=null)
        { 
          return 
            new IteratorChain<Field>
              ((Iterator<Field>) base.fieldIterable().iterator()
              ,(Iterator<Field>) type.getScheme().fieldIterable().iterator()
              );
        }
        else
        { return (Iterator<Field>) type.getScheme().fieldIterable().iterator();
        }
      }
    };
  }

  @Override
  public Field getFieldByIndex(
    int index)
  { 
    if (index>=fieldCount)
    { throw new IndexOutOfBoundsException("index "+index+" > "+getFieldCount());
    }
    if (index>=base.getFieldCount())
    { return type.getScheme().getFieldByIndex(index-base.getFieldCount());
    }
    else
    { return base.getFieldByIndex(index);
    }
  }

  @Override
  public Field getFieldByName(
    String name)
  { return type.getField(name);
  }

  @Override
  public int getFieldCount()
  { return fieldCount;
  }

  @Override
  public Type<?> getType()
  { return type;
  }

}
