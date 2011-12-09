//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.vfs.url;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import spiralcraft.io.message.DictionaryMetadata;
import spiralcraft.io.message.Message;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;
import spiralcraft.vfs.util.ByteArrayResource;

/**
 * A Message associated with a URL 
 * 
 * @author mike
 *
 */
public class URLMessage
  implements Message<DictionaryMetadata>
{

  private DictionaryMetadata metadata;
  private Resource content;
  
  public URLMessage
    (InputStream contentStream
    ,int contentLength
    ,Map<String,List<String>> headers
    )
    throws IOException
  {
    this.metadata=new DictionaryMetadata(headers);
    this.content
      =new ByteArrayResource(StreamUtil.readBytes(contentStream,contentLength));
  }
  
  @Override
  public DictionaryMetadata getMetadata()
  { return metadata;
  }
  
  @Override
  public InputStream getInputStream()
    throws IOException
  { return content.getInputStream();
  }
  
  @Override
  public String toString()
  { 
    StringBuilder buf=new StringBuilder();
    if (metadata!=null)
    {
      for (String name:metadata.getNames())
      { 
        for (String value:metadata.getValues(name))
        { buf.append(name).append(": ").append(value).append("\r\n");
        }
      }
    }
    buf.append("\r\n");
    try
    {
      buf.append(StreamUtil.readAsciiString(content.getInputStream(),-1));
    }
    catch (IOException x)
    {
    }
    return buf.toString();
  }
  
}
