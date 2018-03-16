//
// Copyright (c) 2018 Michael Toth
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
package spiralcraft.vfs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import spiralcraft.log.ClassLog;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.StreamUtil;
import spiralcraft.vfs.TranslationException;
import spiralcraft.vfs.Translator;

/**
 * Translates a collection of multiple resources into a single resource
 */
public class ConcatTranslator
  implements Translator
{ 
  private static final ClassLog log
    =ClassLog.getInstance(ConcatTranslator.class);
  private boolean debug;
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }

  
  @Override
  public Resource translate(
    Resource original,
    URI translatedURI)
    throws IOException,
    TranslationException
  { return translate(original,translatedURI,null);
  }
  
  @Override
  public Resource translate(
    Resource original,
    URI translatedURI,
    Resource previousTranslation
    )
    throws IOException,
    TranslationException
  {
    if (original.asContainer()!=null)
    {
      Container container=original.asContainer();
      String ext=URIUtil.getPathExtension(translatedURI);
      long sourceLastModified=0;
      for (Resource child: container.listChildren())
      {
        if (child.getLocalName().endsWith(ext) && child.asContainer()==null)
        { sourceLastModified=Math.max(sourceLastModified,child.getLastModified());
        }
      }
      if (previousTranslation!=null 
          && previousTranslation.getLastModified()>=sourceLastModified
         )
      { return previousTranslation;
      }
      
      ByteArrayResource ret=new ByteArrayResource();
      OutputStream out=ret.getOutputStream();
      for (Resource child: container.listChildren())
      {
        if (child.getLocalName().endsWith(ext) && child.asContainer()==null)
        { 
          if (debug)
          { log.fine("Processing "+child.getLocalName()+" in "+original.getURI());
          }
          InputStream in = child.getInputStream();
          StreamUtil.copyRaw(in,out,32768);
          in.close();
        }
      }
      out.flush();
      out.close();
      ret.setLastModified(sourceLastModified);
      return ret;
    }
    return null;
  }
}