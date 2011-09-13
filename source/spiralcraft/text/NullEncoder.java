package spiralcraft.text;

import java.io.IOException;

public class NullEncoder
  implements Encoder
{
  public static final Encoder INSTANCE
    = new NullEncoder();


  @Override
  public Appendable encode(
    CharSequence in,
    Appendable out)
    throws IOException
  { return out.append(in);
  } 
  
  

}
