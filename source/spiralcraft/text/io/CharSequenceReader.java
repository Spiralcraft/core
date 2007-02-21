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

import java.io.Reader;

public class CharSequenceReader
  extends Reader
{
  
  private final CharSequence seq;
  private final int len;
  private int pos=0;
  
  public CharSequenceReader(CharSequence seq)
  { 
    this.seq=seq;
    len=seq.length();
  }
  
  public int read(char[] cbuf, int off, int len)
  { 
    if (this.pos==this.len)
    { return -1;
    }
    int count=Math.min(len,this.len-this.pos);
    for (int i=off;i<len;i++)
    { 
      cbuf[i]=this.seq.charAt(this.pos++);
      if (this.pos==this.len)
      { break;
      }
    }
    return count;        
  }
  
  public void close()
  { this.pos=this.len; 
  }
}
