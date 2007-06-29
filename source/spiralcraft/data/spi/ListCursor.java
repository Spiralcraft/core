//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.spi;

import spiralcraft.data.access.ScrollableCursor;


import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;

/**
 * <P>A ScrollableCursor which navigates a List of Tuples.
 * 
 * <P>The Cursor is initially positioned before the first row when it is
 *   created.
 */
public class ListCursor<T extends Tuple>
  implements ScrollableCursor<T>
{
  protected final FieldSet fieldSet;
  protected List<T> data;
  protected int pointer=-1;
  
  public ListCursor(Aggregate<T> aggregate)
  {
    this.fieldSet=aggregate.getType().getContentType().getScheme();
    this.data=new ArrayList<T>(aggregate.size());
    for (T t:aggregate)
    { this.data.add(t);
    }
  }
  
  public ListCursor(FieldSet fieldSet,Iterable<T> data)
  { 
    this.fieldSet=fieldSet;
    this.data=new ArrayList<T>();
    for (T t:data)
    { this.data.add(t);
    }
    
  }

  public ListCursor(FieldSet fieldSet,T ... data)
  { 
    this.fieldSet=fieldSet;
    this.data=Arrays.<T>asList(data);
  }
  
  public ListCursor(FieldSet fieldSet,List<T> data)
  {
    this.fieldSet=fieldSet;
    this.data=data;
  }
  
  /**
   *@return The FieldSet common to all the Tuples that will be returned by this Cursor
   */
  public FieldSet dataGetFieldSet()
  { return fieldSet;
  }
  
  
  /**
   *@return The Tuple currently positioned under the Cursor
   */
  public T dataGetTuple()
    throws DataException
  {
    if (pointer<0 || pointer>=data.size())
    { return null;
    }
    return data.get(pointer);
  }


  public void dataMoveAfterLast() throws DataException
  { pointer=data.size();
  }


  public void dataMoveBeforeFirst() throws DataException
  { pointer=-1;
  }


  public boolean dataMoveFirst() throws DataException
  { 
    pointer=0;
    return pointer<data.size();
  }


  public boolean dataMoveLast() throws DataException
  {
    pointer=data.size()-1;
    return pointer>=0;
  }


  public boolean dataNext() throws DataException
  { 
    pointer++;
//    System.err.println
//      ("ListCursor.dataNext(): #"+pointer+"/"+data.size()+": "+dataGetTuple());
    if (pointer>=data.size())
    { 
      pointer=data.size();
      return false;
    }
    else
    { return true;
    }
  }


  public boolean dataPrevious() throws DataException
  {
    pointer--;
    if (pointer<0)
    { 
      pointer=-1;
      return false;
    }
    else
    { return pointer<data.size();
    }
  }
}
