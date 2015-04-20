package spiralcraft.text.io;

import java.io.IOException;
import java.io.Reader;

public class ReaderCharSequence
  extends InputStreamCharSequence
{
  public ReaderCharSequence(Reader reader)
    throws IOException
  { 
    super();
    load(reader);
  }
  
}