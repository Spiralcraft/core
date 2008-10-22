//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.access;

import java.util.ArrayList;

import spiralcraft.data.Type;


/**
 * The collection of Tables associated with a unit of data storage.
 */
public class Schema
{
  private final ArrayList<Table> tables=new ArrayList<Table>();
  
  
  public void setTypes(Type<?>[] types)
  { 
    for (Type<?> type : types)
    { 
      Table table=new Table();
      table.setType(type);
      tables.add(table);
    }
    
  }


  
  public Table[] getTables()
  { return tables.toArray(new Table[tables.size()]);
  }
  
  public void setTables(Table[] tables)
  { 
    this.tables.clear();
    for (Table table:tables)
    { this.tables.add(table);
    }
  }
}
