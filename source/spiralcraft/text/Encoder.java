package spiralcraft.text;

import java.io.Writer;
import java.io.IOException;

/**
 * An interface which supports the transformation of text 
 *   to a specific encoding.
 */
public interface Encoder
{
  /**
   * Encode the specified CharSequence to
   *   the specified Writer.
   */
  void encode(CharSequence in,Writer out)
    throws IOException;
  
}
