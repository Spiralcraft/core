package spiralcraft.text.io;

import java.io.Reader;
import java.io.IOException;

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
