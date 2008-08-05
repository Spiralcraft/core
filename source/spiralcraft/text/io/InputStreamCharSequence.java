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
package spiralcraft.text.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;

/**
 * Represents an InputStream as a CharSequence by reading the entire
 *   contents of the InputStream into an internal buffer.
 */

//
// XXX Support constructors for java.io.InputStreamReader
// 

public class InputStreamCharSequence
  implements CharSequence
{
  private final static int _BUFSIZE=16*1024;
  
  private final StringBuffer _buffer=new StringBuffer();
  
  protected InputStreamCharSequence()
  { // No-op constructor- subclasses should call load()
  }
  
  public InputStreamCharSequence(InputStream in)
    throws IOException
  { load(in); 
  }
  
  protected void load(InputStream in)
    throws IOException
  { load(new InputStreamReader(in));
  }
  
  protected void load(Reader reader)
    throws IOException
  {
    char[] bucket=new char[_BUFSIZE];
    for (int count=0;(count=reader.read(bucket,0,_BUFSIZE))>-1;)
    { _buffer.append(bucket,0,count);
    }
  }
  
  public CharSequence subSequence(int start,int end)
  { return _buffer.subSequence(start,end);
  }
  
  public int length()
  { return _buffer.length();
  }
  
  public char charAt(int index)
  { return _buffer.charAt(index);
  }
  
  @Override
  public String toString()
  { return _buffer.toString();
  }
}
