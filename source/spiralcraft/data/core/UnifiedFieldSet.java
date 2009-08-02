package spiralcraft.data.core;

import java.util.Iterator;

import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.EmptyIterator;
import spiralcraft.util.IteratorChain;

public class UnifiedFieldSet
  implements FieldSet
{
  private static final ClassLog log
    =ClassLog.getInstance(UnifiedFieldSet.class);
  
  private static final Level debugLevel
    =ClassLog.getInitialDebugLevel(UnifiedFieldSet.class,null);
  
  private final Type<?> type;
  private final FieldSet base;
  private final int fieldCount;

  
  public UnifiedFieldSet(Type<?> type)
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
    
    if (debugLevel.canLog(Level.DEBUG))
    { 
      log.fine
        ("Created UnifiedFieldSet for "+type.getURI()
        +" base="+(base!=null?base.getType().getURI():"NULL")
        );
    }
        
  }
  
  
  @Override
  public Iterable<? extends Field<?>> fieldIterable()
  { 
    return new Iterable<Field<?>>()
    {

      @SuppressWarnings("unchecked")
      @Override
      public Iterator<Field<?>> iterator()
      { 
        if (base!=null)
        { 
          return 
            new IteratorChain<Field<?>>
              ((Iterator<Field<?>>) base.fieldIterable().iterator()
              ,(Iterator<Field<?>>) type.getScheme().fieldIterable().iterator()
              );
        }
        else
        { 
          if (type.getScheme()!=null)
          { return (Iterator<Field<?>>) type.getScheme().fieldIterable().iterator();
          }
          else 
          { return new EmptyIterator<Field<?>>();
          }
        }
      }
    };
  }

  @Override
  public <X> Field<X> getFieldByIndex(
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
  public <X> Field<X> getFieldByName(String name)
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
