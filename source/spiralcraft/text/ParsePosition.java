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
package spiralcraft.text;

import java.net.URI;

/**
 * Encapsulates information related to the position of a text parsing operation.
 */
public class ParsePosition
{

  private int line=-1;
  private int column=-1;
  private int index;
  private CharSequence context;
  private URI contextURI;
  
  public ParsePosition()
  { }
  
  ParsePosition(int line,int column,int index,CharSequence context,URI contextURI)
  {
    this.line=line;
    this.column=column;
    this.index=index;
    this.context=context;
    this.contextURI=contextURI;
  }
  
  
  /**
   * 
   * @return The current line, or -1 if lines are not tracked.
   */
  public int getLine()
  { return line;
  }
  
  /**
   * 
   * @return The current column, or -1 if lines or columns are not tracked.
   */
  public int getColumn()
  { return column;
  }
  
  /**
   * 
   * @return The current position.
   */
  public int getIndex()
  { return index;
  }
  
  /**
   * 
   * @return The content near the current position.
   */
  public CharSequence getContext()
  { return context;
  }
  
  public void setLine(int line)
  { this.line=line;
  }
  
  public void setColumn(int column)
  { this.column=column;
  }
  
  public void setIndex(int position)
  { this.index=position;
  }
  
  public void setContext(CharSequence context)
  { this.context=context;
  }
  
  public void incIndex(int count)
  { index+=count;
  }
  
  public void incLine(int count)
  { line+=count;
  }

  public void incColumn(int count)
  { column+=count;
  }

  public void setContextURI(URI contextURI)
  { this.contextURI=contextURI;
  }
  
  public URI getContextURI()
  { return contextURI;
  }
  
  @Override
  public String toString()
  {
    StringBuilder str=new StringBuilder();
    if (line>=0)
    { str.append("Line "+line);
    }
    if (str.length()>0)
    { str.append(", ");
    }
    if (column>=0)
    { str.append("Column "+column);
    }
    if (str.length()>0)
    { str.append(", ");
    }
    str.append("Index "+index);
    
    if (context!=null)
    {
      if (str.length()>0)
      { str.append(",");
      }
      str.append(" at \""+context+"\"");
    }
    
    if (contextURI!=null)
    {
      if (str.length()>0)
      { str.append(",");
      }
      str.append(" in \""+contextURI+"\"");
    }
    return str.toString();
  }
 
  @Override
  public ParsePosition clone()
  { return new ParsePosition(line,column,index,context,contextURI);
  }
}
