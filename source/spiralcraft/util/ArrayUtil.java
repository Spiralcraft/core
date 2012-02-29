//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.util;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.Collection;
import java.util.Iterator;

import spiralcraft.common.Immutable;


/**
 * Static methods for array manipulation
 */
public class ArrayUtil
{

  /**
   * Append a value to an array
   */
  public static <T> T[] append(T[] array,T value)
  { 
    array=expandBy(array,1);
    Array.set(array,Array.getLength(array)-1,value);
    return array;
  }

  /**
   * Prepend a value to an array
   */
  public static <T> T[] prepend(T[] array,T value)
  { 
    @SuppressWarnings("unchecked")
    T[] newArray = 
      (T[]) Array.newInstance
        (array.getClass().getComponentType()
        , array.length+1
        );
    System.arraycopy(array, 0, newArray, 1, Array.getLength(array));
    Array.set(newArray,0,value);
    return newArray;
  }

  /** 
   * Return the first element of the specified array. If the array is null
   *   or has no elements, return null
   */
  public static Object getFirstElement(Object array)
  {
    if (array==null || Array.getLength(array)==0)
    { return null;
    }
    else
    { return Array.get(array,0);
    }
  }

  /**
   * Append an array to an array. If one of the arrays is null, return
   *   the non-null array. If both arrays are null, return null.
   */
  public static Object concat(Object array1,Object array2)
  { 
    if (array1==null)
    { return array2;
    }
    else if (array2==null)
    { return array1;
    }
    
    int appendPoint=Array.getLength(array1);
    int appendElements=Array.getLength(array2);
    array1=expandBy(array1,appendElements);
    System.arraycopy(array2, 0, array1, appendPoint, appendElements);
    return array1;
  }  
  
  /**
   * Append an array to an array. If one of the arrays is null, return
   *   the non-null array. If both arrays are null, return null.
   */
  public static <T> T[] concat(T[] array1,T[] array2)
  { 
    if (array1==null)
    { return array2;
    }
    if (array2==null)
    { return array1;
    }
    int appendPoint=array1.length;
    int appendElements=array2.length;
    array1=expandBy(array1,appendElements);
    System.arraycopy(array2, 0, array1, appendPoint, appendElements);
    return array1;
  }

  /**
   * Append an array to an array
   */
  public static <T> T[] concat(T[] array1,Collection<T> collection)
  { 
    int appendPoint=array1.length;
    int appendElements=collection.size();
    array1=expandBy(array1,appendElements);
    int pos=appendPoint;
    for (T val : collection)
    { array1[pos++]=val;
    }
    return array1;
  }

  /**
   * Append an array to an array
   */
  @Deprecated
  public static <T> T[] appendArrays(T[] array1,T[] array2)
  { return concat(array1,array2);
  }

  /**
   * Merge the contents of two arrays, discarding duplicate entries
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] mergeArrays(T[] array1,T[] array2)
  { 
    TreeSet<T> treeSet=new TreeSet<T>();
    addToCollection(treeSet,array1);
    addToCollection(treeSet,array2);
    T[] array3
      =(T[]) Array.newInstance
        (array1.getClass().getComponentType(),treeSet.size());
    treeSet.toArray(array3);
    return array3;
  }

  /**
   * Create single element array of the specified class which contains the
   *   specified value
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] newInstance(Class<T> arrayComponentClass,T value)
  { 
    T[] array=(T[]) Array.newInstance(arrayComponentClass,1);
    array[0]=value;
//    Array.set(array,0,value);
    return array;
  }

  /**
   * Add the contents of the array to a collection
   *@return The number of elements successfully added
   */
  @SuppressWarnings("unchecked")
  public static <T> int addToCollection(Collection<T> c,T[] array)
  {
    // Unchecked generics b/c in meta-land here

    int length=Array.getLength(array);
    int count=0;
    for (int i=0;i<length;i++)
    { 
      if (c.add((T) Array.get(array,i)))
      { count++;
      }
    }
    return count;
  }

  /**
   * Find an object in a target array using the equals() method and
   *   return the found objects.
   */
  public static <T> T find(T[] array,T target)
  {
    int index=indexOf(array,target);
    if (index>-1)
    { return array[index];
    }
    return null;
  }

  /**
   * Find an object in a target array using the equals() method and
   *   return the array index where it was found
   */
  public static <T> int indexOf(T[] array,final T target)
  { 
    int length=Array.getLength(array);
    for (int i=0;i<length;i++)
    { 
      T val=array[i];
      if (val==target)
      { return i;
      }
      if (target!=null && target.equals(val))
      { return i;
      }
    }
    return -1;
  }

  public static <T> boolean contains(T[] array,final T target)
  { return indexOf(array,target)>=0;
  }

  /**
   * Expand an array by adding the specified number of elements
   */
  public static Object expandBy(Object array,int expandBy)
  {
    Object newArray = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array)+expandBy);
    System.arraycopy(array, 0, newArray, 0, Array.getLength(array));
    return newArray;
  }
  
  /**
   * Expand an array by adding the specified number of elements
   * 
   * @throws NullPointerException if the array is null due to missing
   *   type information
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] expandBy(T[] array,int expandBy)
  {
    T[] newArray = 
       (T[]) Array.newInstance
         (array.getClass().getComponentType()
         , array.length+expandBy
         );
    System.arraycopy(array, 0, newArray, 0, Array.getLength(array));
    return newArray;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] remove(T[] array,T val)
  {
    int index=indexOf(array,val);
    if (index==-1)
    { return array;
    }
    T[] newArray 
      = (T[]) Array.newInstance
        (array.getClass().getComponentType(), array.length-1);
    System.arraycopy(array,0,newArray,0,index);
    System.arraycopy(array,index+1,newArray,index,newArray.length-index);
    return newArray;
  }

  /**
   * Truncate an array by removing the specified number of elements from
   *   the head of the array.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] truncateBefore(T[] array,int numElements)
  {
    T[] newArray 
      = (T[]) Array.newInstance
        (array.getClass().getComponentType()
        , array.length-numElements
        );
    System.arraycopy(array, numElements, newArray, 0, newArray.length);
    return newArray;
  }
  
  /**
   * Truncate an array to the specified number of elements.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] truncate(T[] array,int numElements)
  {
    T[] newArray 
      = (T[]) Array.newInstance
        (array.getClass().getComponentType(), numElements);
    System.arraycopy(array, 0, newArray, 0, numElements);
    return newArray;
  }

  /**
   * <p>Format an array into a String using the specified separator and
   * delimiter.
   * </p>
   * 
   * <p>No escape processing is performed by this method.
   * </p>
   */
  public static String format(Object[] array,String separator,String delimiter)
  {
    StringBuilder buf=new StringBuilder();
    if (array==null)
    { return null;
    }
    for (int i=0;i<array.length;i++)
    { 
      if (i>0)
      { buf.append(separator);
      }
      if (array[i]!=null)
      { 
        if (delimiter!=null)
        { buf.append(delimiter).append(array[i]).append(delimiter);
        }
        else
        { buf.append(array[i]);
        }
      }
    }
    return buf.toString();
    
  }

  /**
   * <p>Format an Array, using reflection, into a String using the 
   *  specified separator and delimiter.
   * </p>
   * 
   * <p>No escape processing is performed by this method.
   * </p>
   */
  public static String format
    (Object array,String separator,String beginDelimiter,String endDelimiter)
  {
    StringBuilder buf=new StringBuilder();
    int len=Array.getLength(array);
    for (int i=0;i<len;i++)
    { 
      Object val=Array.get(array,i);
      if (i>0)
      { buf.append(separator);
      }
      if (val!=null)
      { 
        if (beginDelimiter!=null || endDelimiter!=null)
        { 
          buf.append(beginDelimiter!=null?beginDelimiter:"")
            .append(val)
            .append(endDelimiter!=null?endDelimiter:"");
        }
        else
        { buf.append(val);
        }
      }
    }
    return buf.toString();
  }  

  /**
   * <p>Format an Array, using reflection, into a String using the 
   *  specified separator and delimiter.
   * </p>
   * 
   * <p>No escape processing is performed by this method.
   * </p>
   */
  public static String format(Object array,String separator,String delimiter)
  { return format(array,separator,delimiter,delimiter);
  }  
  
  /**
   * Format a String array into a String using the specified separator and delimiter.
   * No escape processing is performed by this method.
   * 
   * @deprecated Use format instead
   */
  @Deprecated
  public static String formatToString(Object[] array,String separator,String delimiter)
  {
    StringBuilder buf=new StringBuilder();
    for (int i=0;i<array.length;i++)
    { 
      if (i>0)
      { buf.append(separator);
      }
      if (array[i]!=null)
      { 
        if (delimiter!=null)
        { buf.append(delimiter).append(array[i]).append(delimiter);
        }
        else
        { buf.append(array[i]);
        }
      }
    }
    return buf.toString();
    
  }
  /**
   * Format an Object array into a String using the specified separator
   *   and displaying the class of each object.
   * No escape processing is performed by this method.
   */
  public static String formatWithClassNames(Object[] array,String separator)
  {
    StringBuilder buf=new StringBuilder();
    for (int i=0;i<array.length;i++)
    { 
      if (i>0)
      { buf.append(separator);
      }
      if (array[i]!=null)
      { 
        buf.append("(")
          .append(array[i].getClass().getName())
          .append(") \"")
          .append(array[i])
          .append("\"");
      }
    }
    return buf.toString();
    
  }

  public static final <T extends Object & Comparable<? super T>>
    int arrayCompare(final T[] a,final T[] b)
  {
    // Supress unchecked generics- we're in meta-land here
    
    if (a==b)
    { return 0;
    }
    if (a==null && b!=null)
    { return -1;
    }
    if (b==null && a!=null)
    { return 1;
    }

    for (int i=0;i<a.length;i++)
    {
      if (i==b.length)
      { return 1;
      }

      if (a[i]!=b[i])
      {
        if (a[i]==null && b[i]!=null)
        { return -1;
        }
        if (b[i]==null && a[i]!=null)
        { return 1;
        }
        int result=a[i].compareTo(b[i]);
        if (result!=0)
        { return result;
        }
      }
    }
    if (b.length>a.length)
    { return -1;
    }
    return 0;
  }

  /**
   * <p>Compute a deep array comparison
   * </p>
   *  
   * @param a
   * @return Whether the array and all recursively referenced arrays
   *   and object meet the .equals() test.
   */
  public static final boolean arrayEquals(final Object[] a,final Object[] b)
  { return Arrays.deepEquals(a,b);
  
//    if (a==b)
//    { return true;
//    }
//    if (a==null || b==null || a.length!=b.length)
//    { return false;
//    }
//    for (int i=0;i<a.length;i++)
//    {
//      if (a[i]!=b[i]
//          && (a[i]==null
//              || b[i]==null
//              || !a[i].equals(b[i])
//             )
//         )
//      { return false;
//      }
//    }
//    return true;
  }
  
  /**
   * <p>Compute a deep array hash code
   * </p>
   *  
   * @param a
   * @return The hash code computed for the array and all recursively
   *   referenced arrays.
   */
  public static final int arrayHashCode(final Object[] a)
  { return Arrays.deepHashCode(a);
  }
  
  public static final <X> Iterator<X> iterator(final X[] array)
  { return new ArrayIterator<X>(array);
  }
  
  public static final <X> Iterator<X> iterator(final X[] array,int start,int len)
  { return new ArrayIterator<X>(array,start,len);
  }

  public static final <X> Iterator<X> primitiveArrayIterator(final Object array)
  {
    return new Iterator<X>()
    {
      private int index=0;
      private final int length=Array.getLength(array);

      @Override
      public boolean hasNext()
      { return index<length;
      }
      
      @SuppressWarnings("unchecked")
      @Override
      public X next()
      { 
        if (index>=length)
        { throw new NoSuchElementException();
        }
        return (X) Array.get(array,index++);
      }
      
      @Override
      public void remove()
      { 
        throw new UnsupportedOperationException
          ("Can't remove from array");
      }
    };
    
  }
  
  
  public static final <T> Iterable<T> iterable(final T[] array)
  {
    return new Iterable<T>()
    {
      @Override
      public Iterator<T> iterator()
      {
        return new Iterator<T>()
        {
          private int index=0;
          private int length=Array.getLength(array);

          @Override
          public boolean hasNext()
          { return index<length;
          }
          
          @Override
          @SuppressWarnings("unchecked") // Cast from not-generic method
          public T next()
          { return (T) Array.get(array,index++);
          }
          
          @Override
          public void remove()
          { 
            throw new UnsupportedOperationException
              ("Can't remove from array");
          }
        };
      }
    };
  }
  
  public static final Object reverse(Object array)
  {
    int len=Array.getLength(array);
    Object ret=Array.newInstance(array.getClass().getComponentType(),len);
    for (int i=0;i<len;i++)
    { Array.set(ret,len-i-1,Array.get(array,i));
    }
    return ret;
  }
  
  public static final Object asKey(final Object[] array)
  { return new ArrayKey(array);
  }

  /**
   * Copy the contents of an Iterable to an array of the specified component
   *   type.
   * @param <T>
   * @param componentType
   * @param iterable
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] toArray
    (Class<T> componentType,Iterable<? extends T> iterable)
  {
    ArrayList<T> list=new ArrayList<T>();
    for (T item : iterable)
    { list.add(item);
    }
    return list.toArray((T[]) Array.newInstance(componentType,list.size()));

  }
  
  @SuppressWarnings("unchecked")
  public static <B,T extends B> T[] convert
    (Class<T> componentType,B[] original)
  {
    T[] ret=(T[]) Array.newInstance(componentType,original.length);
    System.arraycopy(original,0,ret,0,original.length);
    return ret;
    
  }

  @SuppressWarnings("unchecked")
  public static <T> T head(T input,int split)
  { 
    T result
      =(T) Array.newInstance
        (input.getClass().getComponentType()
        ,split
        );
    System.arraycopy(input, 0, result, 0, split);
    return result;
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T tail(T input,int split)
  { 
    int rest=Array.getLength(input)-split;
    T result
      =(T) Array.newInstance
        (input.getClass().getComponentType()
        ,rest
        );
    System.arraycopy(input, split, result, 0, rest);
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] intersection(T[] sorted1,T[] sorted2)
  {
    HashSet<T> set1=new HashSet<T>();
    for (T t:sorted1)
    { set1.add(t);
    }
    
    ArrayList<T> ret=new ArrayList<T>();
    for (T t:sorted2)
    { 
      if (set1.contains(t))
      { ret.add(t);
      }
    }
    return ret.toArray
      ((T[]) Array.newInstance
        (sorted1.getClass().getComponentType()
        ,ret.size()
        )
      );
  }
}

class ArrayIterator<X>
  implements Iterator<X>
{
  private final X[] array;
  private int index=0;
  private final int length;

  ArrayIterator(X[] array)
  { this(array,0,array.length);
  }
  
  ArrayIterator(X[] array,int start,int len)
  { 
    this.array=array;
    this.index=start;
    this.length=len;
  }
  
  @Override
  public boolean hasNext()
  { return index<length;
  }
  
  @Override
  public X next()
  { 
    if (index>=length)
    { throw new NoSuchElementException();
    }
    return array[index++];
  }
  
  @Override
  public void remove()
  { 
    throw new UnsupportedOperationException
      ("Can't remove from array");
  }
  
}

@Immutable
class ArrayKey
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