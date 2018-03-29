package spiralcraft.text.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Appends text to a file. 
 * 
 * @author mike
 *
 */
public class FileAppender
{
  private File file;
  private Writer writer;
  
  public FileAppender(File file)
  { this.file=file;
  }
  
  public void open()
    throws IOException
  { 
    if (writer!=null)
    { writer=new OutputStreamWriter(new FileOutputStream(file,true));
    }
  }
  
  public synchronized void close()
    throws IOException
  {
    if (writer!=null)
    { 
      try
      { writer.close();
      }
      finally
      { writer=null;
      }
    }
  }
  
  /**
   * Append the text to the file. 
   * 
   * If open() has not been called, the file will
   *   be opened before and closed after the text is appended.
   * 
   * @param text
   * @throws IOException
   */
  public synchronized void append(CharSequence text)
      throws IOException
  { 
    Writer writer=this.writer;
    if (writer==null)
    { writer=new OutputStreamWriter(new FileOutputStream(file,true));
    }
    
    try
    { 
      writer.append(text);
      writer.flush();
    }
    finally
    { 
      if (this.writer==null)
      { writer.close();
      }
    } 
  }
}
