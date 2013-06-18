package spiralcraft.util;

import spiralcraft.common.Immutable;

/**
 * Encapsulates an array into a representation suitable for use as a map key.
 * 
 * @author mike
 */
@Immutable
public class ArrayKey
{
  private final Object[] array;
  private final int hashCode;
  
  
  public ArrayKey(Object[] array)
  { 
    this.array=new Object[array.length];
    System.arraycopy(array,0,this.array,0,array.length);
    
    this.hashCode=ArrayUtil.arrayHashCode(array);
  }

  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public boolean equals(Object obj)
  { 
    return obj instanceof ArrayKey 
      && ArrayUtil.arrayEquals(array,((ArrayKey) obj).array);
  }
  
  @Override
  public String toString()
  { return super.toString()+": ["+ArrayUtil.format(array,",","[","]")+"]";
  }

}
