package spiralcraft.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

import spiralcraft.util.string.StringUtil;

public class HexDump
{

  public static void dump(InputStream in,Writer out)
    throws IOException
  { new HexDump().dumpStream(in,out);
  }

  public static String dump(byte[] in,int offset,int length)
      throws IOException
  { return new HexDump().dumpArrayToString(in,offset,length);
  }

  public static String dump(byte[] in)
      throws IOException
  { return new HexDump().dumpArrayToString(in,0,in.length);
  }
  
  public HexDump()
  {
  }

  public String dumpArrayToString(byte[] in,int offset,int length)
  {
    StringWriter out=new StringWriter();
    ByteArrayInputStream ins = new ByteArrayInputStream(in,offset,length);
    try
    {
      dumpStream(ins,out);
      out.flush();
      return out.toString();
    }
    catch (IOException x)
    { throw new UncheckedIOException(x);
    }
  }
  
  public void dumpStream(InputStream in,Writer out)
    throws IOException
  { 
    byte[] buff = new byte[16];
    int bytesRead;
    long pos=0;
    while (true)
    { 
      bytesRead=in.read(buff,0,16);
      if (bytesRead == -1)
      { break;
      }
      
      out.append(StringUtil.prepad(Long.toHexString(pos),'0',8)).append("  ");
      for (int i=0; i<16; i++)
      { 
        if (i<bytesRead)
        {
          out.append
            (StringUtil.prepad(Integer.toHexString(Byte.toUnsignedInt(buff[i])),'0',2))
              .append(" ");
          
        }
        else
        {
          out.append("   ");
        }
        if (i==7 || i==15)
        { out.append(" ");
        }
      }
      out.append("|");
      for (int i=0; i<16; i++)
      { 
        if (i<bytesRead)
        {
          
          if (buff[i] >= 32 && buff[i] < 127)
          { out.append( (char) buff[i] );
          }
          else
          { out.append(".");
          }
        }
        else
        { out.append(" ");
        }
      }
      out.append("|\r\n");
      pos+=bytesRead;
    }
  }
  
}
