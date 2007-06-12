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
package spiralcraft.codec.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import spiralcraft.codec.CodecException;

public class Base64Codec
{

  private static final byte[] encoding = 
    {'A','B','C','D','E','F','G','H'
    ,'I','J','K','L','M','N','O','P'
    ,'Q','R','S','T','U','V','W','X'
    ,'Y','Z','a','b','c','d','e','f'
    ,'g','h','i','j','k','l','m','n'
    ,'o','p','q','r','s','t','u','v'
    ,'w','x','y','z','0','1','2','3'
    ,'4','5','6','7','8','9','+','/'
    ,'='
    };
  
  private static final int WINDOW_SIZE = 1024; 

  /**
   * Encode byte 1 of output from a 1 byte input chunk (bits 0-5)
   */
  private final byte encode1(byte window[], int pos) 
  { return encoding[(window[pos] & 0xfc) >> 2];
  }

  /**
   * Encode byte 2 of output from a 2 byte input chunk (bits 6-11)
   */
  private final byte encode2(byte window[], int pos) 
  { 
    return encoding[((window[pos]&0x3) << 4) 
               | ((window[pos+1]&0xf0) >>> 4)
              ];
  }

  /**
   * Encode byte 3 of output from a 3 byte input chunk (bits 12-17)
   */
  private final byte encode3(byte window[], int pos) 
  { 
    return encoding[((window[pos+1] & 0x0f) << 2) 
               | ((window[pos+2] & 0xc0) >>> 6) 
              ];
  }

  /**
   * Encode byte 4 of output from a 3 byte input chunk (bits 18-23)
   */
  private final byte encode4(byte window[], int pos) 
  { return encoding[window[pos+2] & 0x3f];
  }

  /**
   * Encode the InputStream to the OutputStream
   * 
   * @throws IOException If one is thrown when reading or writing
   */
  public void encode(final InputStream in, final OutputStream outStream)
    throws IOException,CodecException
  {
    final Base64LineOutput out=new Base64LineOutput(outStream);
    final byte[] window=new byte[WINDOW_SIZE];

    int chunkLen = -1;
    int windowPos = 0;
    
    while ((chunkLen= in.read(window, windowPos, WINDOW_SIZE-windowPos)) > 0) 
    {
      //if ( chunkLen>= 3 ) // Bug- if in.read() returns 1 at a time the loop
      // will never run
      
      if (chunkLen+windowPos>=3) // If we have 3 bytes accumulated
      {
        
        chunkLen+=windowPos; // Adjust for remainder from last iteration
        windowPos=0; 
        
        // Read 3 bytes of input into 4 bytes of output until we are < 3 bytes
        //   from the end of the window
        while (windowPos + 3 <= chunkLen) 
        {
          out.write(encode1(window,windowPos));
          out.write(encode2(window,windowPos));
          out.write(encode3(window,windowPos));
          out.write(encode4(window,windowPos));
          
          windowPos+=3;
        }
        
        // Slide the window over (copy remainder to the beginning)
        for ( int i = 0 ; i < 3 ;i++) 
        {
          window[i] 
            = (i < chunkLen-windowPos) 
            ? window[windowPos+i] 
            : 0 
            ;
        }
        windowPos = chunkLen-windowPos; // Next read-point
      } 
      else
      {
        // Window contains less < 3 bytes 
        windowPos += chunkLen;
      }
    }
    
    // Output the remainder bytes:
    switch (windowPos) 
    {
    case 1:
      out.write(encode1(window,0));
      out.write(encode2(window,0));
      out.write('=');
      out.write('=');
      break;
    case 2:
      out.write(encode1(window,0));
      out.write(encode2(window,0));
      out.write(encode3(window,0));
      out.write('=');
      break;
    default:
      throw new CodecException
        ("Base64Codec: Internal error: "+windowPos+" bytes remaining");
    }
  }
  

  public void decode(final InputStream inStr, final OutputStream out)
    throws IOException,CodecException
  {
    final Base64Input in=new Base64Input(inStr,WINDOW_SIZE);
    
    int readLen;
    byte chunk[]=new byte[4];
    while ( (readLen=in.read(chunk)) > 0)
    {
      switch (readLen)
      {
      case 1:
        throw new CodecException
          ("Premature EOF encountered in BASE64 encoding.");
      case 2:
        out.write(decode1(chunk));
        break;
      case 3:
        out.write(decode1(chunk));
        out.write(decode2(chunk));
        break;
      case 4:
        out.write(decode1(chunk));
        out.write(decode2(chunk));
        out.write(decode3(chunk));
        break;
      }
    }
      
  }
  
  private final int decode1 (byte[] chunk)
  {
    return ((chunk[0] & 0x3f) << 2) 
            | ((chunk[1] & 0x30) >>> 4) 
            ;
  }

  private final int decode2 (byte[] chunk)
  { 
    return ((chunk[1] & 0x0f) << 4) 
            | ((chunk[2] &0x3c) >>> 2)
            ;
  }

  private final int decode3 (byte[] chunk)
  {
    return ((chunk[2] & 0x03) << 6) 
            | (chunk[3] & 0x3f)
            ;
  }


}

/**
 * Inputs Base64, converting encoded chars into raw bytes
 */
class Base64Input
{
  private final InputStream in;
  private final byte[] window;
  private int windowEnd=0;
  private int windowStart=0;
  private boolean done=false;
  private boolean endChunk=false;

  
  public Base64Input(InputStream in,int bufsize)
  { 
    this.window=new byte[bufsize];
    this.in=in;
  }
  
  /**
   * Read up to 4 bytes into the chunk from the stream
   * 
   * @param chunk
   * @return The number of bytes read
   */
  public int read(byte[] chunk)
    throws IOException,CodecException
  {
    int chunkPos=0;
    while (!done && chunkPos<4)
    { 
      if (windowStart==windowEnd)
      { 
        // Rebuffer
        windowStart=0;
        windowEnd=in.read(window,0,window.length);
      }
      if (windowEnd<=0)
      { 
        // End of input
        done=true;
        if (!endChunk)
        {
          throw new CodecException
            ("Premature EOF encountered in BASE64 encoding.");
        }
        return chunkPos;
      }
      int result=translate(window[windowStart++]);
      if (result==65)
      { 
        done=true;
        endChunk=true;
        return chunkPos;
      }
      else if (result>-1)
      { chunk[chunkPos++]=(byte) result;
      }
    }
    return chunkPos;
  }

  private final int translate(int input)
  {
    if ((input>='A') && (input<='Z'))
    { return input-'A';
    } 
    else if ((input>='a') && (input<='z'))
    { return input-'a'+26;
    } 
    else if ((input>='0') && (input<='9')) 
    { return input-'0'+52 ;
    } 
    else
    {
      switch (input) 
      {
      case '+':
        return 62;
      case '/':
        return 63;
      case '=':
        return 65;
      default:
        return -1 ;
      }
    }
  }
  
}

/**
 * Outputs encoded Base64, wrapping lines with CRLF as appropriate
 */
class Base64LineOutput
{
  private final OutputStream out;
  private int col=0;
  
  public Base64LineOutput(OutputStream out)
  { this.out=out;
  }
  
  public void write(int chr)
    throws IOException
  { 
    if (col==76)
    { 
      out.write('\r');
      out.write('\n');
      col=0;
    }
    out.write(chr);
    col++;
    
  }
}
