//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.data.flatfile;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import spiralcraft.text.KmpMatcher;



/**
 * Format of an individual field in a flat file record
 * 
 * @author mike
 *
 */
public class FieldEncoder
{
 
  private char startDelimiter='\"';
  private char endDelimiter='\"';
  private boolean writeDelimiter=true;
  private boolean delimiterOptional=true;
  private String nullValue;
  private KmpMatcher separatorMatcher;
  private String separator;
  
  /**
   * The opening delimiter
   * 
   * @return The delimiter character
   */
  public char getStartDelimiter()
  { return startDelimiter;
  }
  
  /**
   * Whether to write the delimiter to the output. Defaults to true.
   * 
   * @param writeDelimiter
   */
  public void setWriteDelimiter(boolean writeDelimiter)
  { this.writeDelimiter=writeDelimiter;
  }
  
  /**
   * Specify the delimiter, as 0,1 or 2 characters. A null or empty string
   *   indicates that no delimiter is to be used.
   * 
   * @param delimiter
   */
  public void setDelimiter(String delimiter)
  {
    if (delimiter==null)
    { writeDelimiter=false;
    }
    else if (delimiter.length()==0)
    { writeDelimiter=false;
    }
    else if (delimiter.length()==1)
    { 
      startDelimiter=delimiter.charAt(0);
      endDelimiter=startDelimiter;
      writeDelimiter=true;
    }
    else if (delimiter.length()==2)
    {
      startDelimiter=delimiter.charAt(0);
      endDelimiter=delimiter.charAt(1);
      writeDelimiter=true;
    }
    else
    { 
      throw new IllegalArgumentException
        ("Delimiter setting must 2 characters or less");
    }
  }
  
  /**
   * The opening delimiter
   * 
   */
  public void setStartDelimiter(
    char startDelimiter)
  { this.startDelimiter = startDelimiter;
  }

  
  /**
   * The closing delimiter 
   * 
   */
  public char getEndDelimiter()
  { return endDelimiter;
  }


  /**
   * The closing delimiter 
   * 
   * @param endDelimiter
   */
  public void setEndDelimiter(
    char endDelimiter)
  { this.endDelimiter = endDelimiter;
  }


  /**
   * Whether the delimiter is optional
   * 
   */
  public boolean isDelimiterOptional()
  { return delimiterOptional;
  }

  /**
   * Whether the delimiter is optional
   * 
   * @param delimiterOptional
   */
  public void setDelimiterOptional(
    boolean delimiterOptional)
  { this.delimiterOptional = delimiterOptional;
  }

  /**
   * The value that signifies 'null', or undefined/unspecified, if any. 
   * 
   */
  public void setNullValue(String nullValue)
  { this.nullValue=nullValue;
  }
  
  /**
   * The value that signifies 'null', or undefined/unspecified, if any. 
   * 
   * @return The nullValue
   */
  public String getNullValue()
  { return nullValue;
  }


  
  void setFieldSeparator(String fieldSeparator)
  { 
    separator=fieldSeparator;
    separatorMatcher=new KmpMatcher(fieldSeparator);
  }
  
  public String parse(Reader reader)
    throws IOException
  { 
    StringBuffer data=new StringBuffer();
    char chr;
    boolean inDelimiter=false;
    boolean done=false;
    separatorMatcher.reset();
    
    // TO
    while (!done)
    {
      int read=reader.read();
      chr=(char) read;
      
      if (read==-1)
      { done=true;
      }
      else if (!inDelimiter) 
      {
        if (separatorMatcher.match(chr))
        { 
          done=true;
          data.setLength(data.length()-(separator.length()-1));
        }
        else if (chr==startDelimiter)
        { inDelimiter=true;
        }
        else
        { data.append(chr);
        }
      }
      else
      {
        if (chr==endDelimiter)
        { inDelimiter=false;
        }
        else
        { data.append(chr);
        }
      }
    }
    if (data.length()>0)
    { 
      String ret=data.toString();
      if (ret.equals(nullValue))
      { return null;
      }
      else
      { return ret;
      }
    }
    else
    { return null;
    }
  }
  
  public void format(Writer writer,String data)
    throws IOException
  { 
    // TODO: Escape delimiter
    if (data==null)
    { data=nullValue;
    }
    
    if (data==null)
    { 
      if (writeDelimiter)
      {
        writer.write(startDelimiter);
        writer.write(endDelimiter);
      }
    }
    else if (!writeDelimiter && !data.contains(separator))
    { writer.write(data);
    }
    else
    { 
      // Always write the delimiter if the data contains an unescaped 
      //   field separator, otherwise we definitely break things
      writer.write(startDelimiter);
      writer.write(data);
      writer.write(endDelimiter);
    }
  }

  
  
}
