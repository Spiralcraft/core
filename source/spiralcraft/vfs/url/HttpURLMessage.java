package spiralcraft.vfs.url;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class HttpURLMessage
  extends URLMessage
{

  private final int statusCode;
  private final String statusMessage;
  
  public HttpURLMessage(
    InputStream contentStream
    ,int contentLength
    ,Map<String, List<String>> headers
    ,int statusCode
    ,String statusMessage
    )
    throws IOException
  {
    super(contentStream, contentLength, headers);
    this.statusCode=statusCode;
    this.statusMessage=statusMessage;
  }
  
  public int getStatusCode()
  { return statusCode;
  }
  
  public String getStatusMessage()
  { return statusMessage;
  }
  

}
