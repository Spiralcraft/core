//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.vfs.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;
import spiralcraft.vfs.StreamUtil;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;

import java.nio.charset.Charset;

import java.lang.CharSequence;

/**
 * A ResourceFilter which accepts resources with content 
 *   which contains the specified pattern.
 */
public class ContentRegexFilter
  implements ResourceFilter
{
  private Pattern _pattern;

  public ContentRegexFilter(String expression)
    throws PatternSyntaxException
  { _pattern=Pattern.compile(expression);
  }

  @Override
  public boolean accept(Resource resource)
  {
    InputStream in=null;
    try
    {
      in=resource.getInputStream();
      if (in==null)
      { return false;
      }
      CharSequence charSequence=null;
  
      if (in instanceof FileInputStream)
      { 
        FileChannel channel=((FileInputStream) in).getChannel();
        ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
        charSequence=Charset.forName("8859_1").newDecoder().decode(bbuf);
      }
      else
      {
        byte[] bytes=StreamUtil.readBytes(in);
        ByteBuffer bbuf = ByteBuffer.allocate(bytes.length).put(bytes);
        charSequence=Charset.forName("8859_1").newDecoder().decode(bbuf);
      }
  
      return _pattern.matcher(charSequence).find();
    }
    catch (IOException x)
    { System.err.println(x.toString());
    }
    finally
    {
      if (in!=null)
      { 
        try
        { in.close();
        }
        catch (IOException x)
        { }
      }
    }
    return false;
  }

}

