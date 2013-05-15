//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.exec.ExecutionContext;
import spiralcraft.text.html.URLEncoder;


/**
 * <p>Utility functions to handle resources
 * </p>
 * 
 * @author mike
 *
 */
public class ResourceUtil
{

  /**
   * <p>Copy a Resource into a container, generating a unique name from the
   *   supplied baseName if required
   * </p>
   * 
   * @param input
   * @param container
   * @param name
   * @return
   */
  public static Resource addCopy
    (Container container,Resource input,String baseName)
    throws IOException
  {          
    if (container==null)
    { throw new IllegalArgumentException("Container cannot be null");
    }
    Resource targetResource=container.getChild(baseName);
    while (targetResource.exists())
    {
      baseName=nextUniqueName(baseName);
      targetResource=container.getChild(baseName);
    }
    targetResource.copyFrom(input);
    return targetResource;
  }
   
  /**
   * <p>Generate a unique filename using an algorithm that inserts or increments
   *   a parenthesized integer at the end of the filename but before the
   *   last dotted suffix.
   * </p>
   * 
   * <p>Example: foo.txt, foo(1).txt, foo(2).txt, foo.baz.txt, foo.baz(1).txt
   * </p>
   * 
   * @param filename
   * @return
   */
  public static String nextUniqueName(String filename)
  {
    int dotPos=filename.indexOf('.');
    String prefix=(dotPos>0)?filename.substring(0,dotPos):filename;
    String suffix=(dotPos>0)?filename.substring(dotPos):"";
    
    int num=2;
    if (prefix.endsWith(")"))
    {
      int parenPos=prefix.lastIndexOf("(");
      if (parenPos>-1)
      { 
        String numString=prefix.substring(parenPos+1,prefix.length()-1);
        try
        { 
          num=Integer.parseInt(numString)+1;
          prefix=prefix.substring(0,parenPos).trim();
        }
        catch (NumberFormatException x)
        { // Ignore, last paren does not contain a number
        }
      }
    }
    prefix+="("+num+")";
    return prefix+suffix;
  }
  
  public static void writeAsciiString(Resource resource,String content)
    throws IOException
  {
    if (resource==null)
    { throw new IllegalArgumentException("Resource cannot be null");
    }
    OutputStream out=resource.getOutputStream();
    try
    { 
      StreamUtil.writeAsciiString(out,content);
      out.flush();
    }
    finally
    { out.close();
    }
  }
  
  public static String readAsciiString(Resource resource)
    throws IOException
  {
    if (resource==null)
    { throw new IllegalArgumentException("Resource cannot be null");
    }
    InputStream in=resource.getInputStream();
    try
    {
      return StreamUtil.readAsciiString(in,-1);
    }
    finally
    { in.close();
    }
    
  }
  
  /**
   * Resolve a resource relative to the Execution Context default path using an 
   *   unescaped path
   * 
   * @param scheme
   * @param path
   * @return The resource
   * @throws UnresolvableURIException
   * @throws URISyntaxException
   */
  public static Resource resolveInExecutionContext(String path)
    throws UnresolvableURIException,URISyntaxException
  { 
    URI uri=ExecutionContext.getInstance().canonicalize
        (URI.create(URLEncoder.encode(path)));
        
    Resource ret=Resolver.getInstance().resolve(uri);
    if (ret==null)
    { 
      throw new UnresolvableURIException
        (uri,"Could not resolve uri canonicalized from path "+path);
    }
    return ret;
  }
  
  /**
   * Recursively delete a container tree
   * 
   * @param resource
   */
  public static void deleteRecursive(Resource resource)
    throws IOException
  {
    Container container=resource.asContainer();
    if (container!=null)
    {
      for (Resource child: container.listChildren())
      { deleteRecursive(child);
      }
    }
    resource.delete();
  }
}

