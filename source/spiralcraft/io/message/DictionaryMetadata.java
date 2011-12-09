//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.io.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spiralcraft.util.ListMap;

/**
 * Metadata that is represented by name/value String pairs
 * 
 * @author mike
 *
 */
public class DictionaryMetadata
  implements Metadata
{
  
  private final ListMap<String,String> data;
  
  public DictionaryMetadata(ListMap<String,String> data)
  { this.data=data;
  }
  
  public DictionaryMetadata(Map<String,List<String>> data)
  { this.data=new ListMap<String,String>(data);
  }
  
  public List<String> getValues(String name)
  { return data.get(name);
  }

  public String getValue(String name)
  { return data.getFirst(name);
  }
  
  public Set<String> getNames()
  { return Collections.unmodifiableSet(data.keySet());
  }
}
