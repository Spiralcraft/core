package spiralcraft.text.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

/**
 * Represents an File as a CharSequence
 */

//
// XXX Support constructors for non-default Character conversion
// 

public class FileCharSequence
  extends InputStreamCharSequence
{
  public FileCharSequence(String fileName)
    throws IOException
  { 
    FileInputStream in=new FileInputStream(fileName);
    load(in);
    in.close();
  }
  
  
}
