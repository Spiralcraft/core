//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.util;

import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.log.ClassLog;
import spiralcraft.text.html.URLDataEncoder;
import spiralcraft.text.html.URLEncoder;
import spiralcraft.util.refpool.URIPool;

/**
 * Utility class to manipulate URIs
 * 
 * @author mike
 *
 */
public class URIUtil
{
  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(URIUtil.class);

  /**
   * Creates a new URI, adding the specified pre-encoded String to the
   *   end of the path String.
   * 
   * @param source
   * @param rawSuffix
   * @return
   */
  public static final URI addPathSuffix(URI source,String rawSuffix)
  { 
    return URIPool.create
      (trimToPath(source).toString()
        +rawSuffix
        +(source.getRawQuery()!=null
          ?"?"+source.getRawQuery()
          :""
          )
        +(source.getRawFragment()!=null
          ?"#"+source.getRawFragment()
          :""
         )
      );
  }
  
  
  /**
   * Creates a new URI, removing the specified pre-encoded String from the
   *   end of the path string, if present
   * 
   * @param source
   * @param rawSuffix
   * @return
   */
  public static final URI removePathSuffix(URI source,String rawSuffix)
  { 
    String rawPath=source.getRawPath();
    if (rawPath!=null && rawPath.endsWith(rawSuffix))
    { 
      rawPath=rawPath.substring(0,rawPath.length()-rawSuffix.length());
      return URIPool.create
          ( (source.getScheme()!=null?source.getScheme()+":":"")
            +(source.getAuthority()!=null?"//"+source.getAuthority():"")
            +rawPath
            +(source.getRawQuery()!=null
              ?"?"+source.getRawQuery()
              :""
              )
            +(source.getRawFragment()!=null
              ?"#"+source.getRawFragment()
              :""
             )
          );
    }
    else
    { return source;
    }
  }
  
  /**
   * <p>Add a pre-encoded path segment to the specified URI
   * </p>
   * 
   * @return
   */
  public static final URI addPathSegment(URI in,String segment)
  {
    if (in.getPath().endsWith("/"))
    { return in.resolve(segment);
    }
    else
    { return replaceUnencodedPath(in,in.getPath()+"/").resolve(segment);
    }
  }
  
  /**
   * <p>Add a pre-encoded path segment to the specified URI
   * </p>
   * 
   * @return
   */
  public static final URI addUnencodedPathSegment(URI in,String segment)
  {
    if (in.getPath().endsWith("/"))
    { 
      try
      { return URIPool.get(in.resolve(new URI(null,segment,null)));
      }
      catch (URISyntaxException x)
      { throw new IllegalArgumentException
          ("Path segment '"+segment+"' cannot be encoded",x);
      }
    }
    else
    { return replaceUnencodedPath(in,in.getPath()+"/"+segment);
    }
  }

  /**
   * <p>Ensures that the URI path has a trailing slash. If it does,
   *   simply return the original URI, otherwise rewrite the URI
   * </p>
   * 
   * @return
   */
  public static final URI ensureTrailingSlash(URI in)
  {
    if (in.getPath().endsWith("/"))
    { return in;
    }
    else
    { return replaceUnencodedPath(in,in.getPath()+"/");
    }
  }
  
  /**
   * Replace the path of the specified URI with the 
   *   specified nonencoded path string
   * 
   * @return
   */
  public static final URI replaceUnencodedPath
    (URI source
    ,String unencodedPath
    )
  {
    if (unencodedPath.isEmpty())
    { unencodedPath="/";    
    }
    
    try
    {
      return URIPool.get
        (new URI
          (source.getScheme()
          ,source.getAuthority()
          ,unencodedPath
          ,source.getQuery()
          ,source.getFragment()
          )
        );
    }
    catch (URISyntaxException x)
    { 
      throw new IllegalArgumentException
        ("Synax error in path '"+unencodedPath+"'",x);
    }
    
  }
  
  /**
   * Replace the query part of the specified URI with the 
   *   specified pre-encoded query string
   * 
   * @return
   */
  public static final URI replaceRawQuery
    (URI source
    ,String rawQuery
    )
  {
    return URIPool.create
      (trimToPath(source).toString()
        +"?"
        +rawQuery
        +(source.getRawFragment()!=null
         ?"#"+source.getRawFragment()
         :""
         )
      );
  }
  
  public static final URI addQueryParameter
    (URI source
    ,String parameterName
    ,String value
    )
  {
    String oldQuery=source.getRawQuery();
    String newQuery;
    if (oldQuery==null)
    { newQuery=encodeParameter(parameterName,value);
    }
    else
    { newQuery=oldQuery+"?"+encodeParameter(parameterName,value);
    }
    
    return replaceRawQuery(source,newQuery);
  }
  
  /**
   * Trim the query and fragment from the specified URI.  
   * 
   * @param input
   * @return
   */
  public static final URI trimToPath(URI input)
  { 
    try
    {
      if (input==null)
      { return null;
      }
      else if (input.getPath()!=null)
      {  
        if (input.getQuery()!=null || input.getFragment()!=null)
        {
          return URIPool.get
            (new URI
              (input.getScheme()
              ,input.getAuthority()
              ,input.getPath()
              ,null
              ,null
              )
            );
        }
        else
        { return input;
        }
      }
      else
      {
        if (input.getFragment()!=null)
        {
          return
            URIPool.get
              (new URI
                (input.getScheme()
                ,input.getSchemeSpecificPart()
                ,null
                )
              );
        }
        else
        { return input;
        }
      }
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x);
    }
      
  }
  
  /**
   * Return the URI with the last element of the path removed and the path
   *   ending in a slash
   * 
   * @param input
   * @return
   */
  public static final URI toParentPath(URI input)
  { 
    // log.fine(""+input);
    URI ret;
    try
    {
      if (input==null)
      { ret=null;
      }
      else if (input.getPath()!=null 
              && !input.getPath().isEmpty()
              && !input.getPath().equals("/")
              )
      {  
        ret=URIPool.get
          (new URI
            (input.getScheme()
            ,input.getAuthority()
            ,new Path(input.getPath(),'/').parentPath().format('/')
            ,input.getQuery()
            ,input.getFragment()
            )
          );
      }
      else
      { ret=null;
      }
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x);
    }
    // log.fine(input+" -> "+ret);
    return ret;
  }
  
  /**
   * Return the last element of the path in the specified URI
   * 
   * @param input
   * @return
   */
  public static final String unencodedLocalName(URI input)
  { return new Path(input.getPath()).lastElement();
  }


  /**
   * Replace the scheme of a URI with the specified scheme
   * 
   * @param uri
   * @param scheme
   * @return
   */
  public static URI replaceScheme(
    URI uri,
    String scheme)
  {
    try
    {
      return URIPool.get
        (new URI
          (scheme
          ,uri.getAuthority()
          ,uri.getPath()
          ,uri.getQuery()
          ,uri.getFragment()
          )
        );
    }
    catch (URISyntaxException x)
    { throw new IllegalArgumentException(x);
    }
  }
  
  /**
   * Encode a string representing a path into a format suitable for inclusion 
   *   in the path portion of a URI. All valid path characters are preserved.
   *   
   * @param path
   * @return
   */
  public static String encodeURIPath(String path)
  { return URLEncoder.encode(path);
  }
  
  /**
   * Decodes a percent-encoded path string as encoded by the URLEncoder
   * 
   * @param raw
   * @return
   */
  public static String decodeURIPath(String raw)
  { return URLEncoder.decode(raw);
  }
  
  /**
   * Encode a string into an application/x-www-form-urlencoded format suitable
   *   for a parameter value in the query portion of a URI
   */
  public static String encodeParameterData(String data)
  { return URLDataEncoder.encode(data);
  }
  
  /**
   * Encode a named parameter value into an 
   *   application/x-www-form-urlencoded format suitable for the query portion 
   *   of a URI
   */
  public static String encodeParameter(String parameter,String data)
  { return parameter+"="+URLDataEncoder.encode(data);
  }

  /**
   * Encode a named array of parameter values into an 
   *   application/x-www-form-urlencoded
   *   format suitable for the query portion of a URI
   *   
   * @param parameter
   * @param data
   * @return
   */
  public static String encodeParameter(String parameter,String[] data)
  { 
    if (data==null)
    { return "";
    }
    else
    {
      StringBuilder builder=new StringBuilder();
      for (String str:data)
      {
        if (builder.length()>0)
        { builder.append("&");
        }
        builder
          .append(parameter)
          .append("=")
          .append(URLDataEncoder.encode(str))
          ;
      }
      return builder.toString();
    }
  }
  
}
