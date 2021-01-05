//
// Copyright (c) 2020 Michael Toth
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IteratorUtil
{
  public static <T> List<T> toList(Iterator<T> iterator)
  {
    LinkedList<T> list=new LinkedList<T>();
    return toList(iterator,list);
  }
  
  public static <T> List<T> toList(Iterator<? extends T> iterator,List<T> list)
  {
    while (iterator.hasNext())
    { list.add(iterator.next());
    }
    return list;
  }
  
}
