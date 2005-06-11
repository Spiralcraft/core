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
package spiralcraft.stream.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

import spiralcraft.stream.ResourceFilter;
import spiralcraft.stream.Resource;

/**
 * A ResourceFilter which accepts resources where the last path element
 *   (the 'name') matches the specified glob expression. A glob expression
 *   is a normal regular expression with the '?','*', and '.' characters
 *   redefined (non recursively) as follows, in order to conform
 *   to end user's expectations regarding shell wildcards.
 *
 *   '?' -> '.'
 *   '.' -> '\\.'
 *   '*' -> '.*'
 */
public class NameGlobFilter
  implements ResourceFilter
{
  private Pattern _pattern;

  public NameGlobFilter(String expression)
    throws PatternSyntaxException
  { _pattern=globToPattern(expression);
  }

  public boolean accept(Resource resource)
  {
    String path=resource.getURI().getPath();
    String filePart=path.substring(path.lastIndexOf('/')+1);
    if (filePart.length()>0)
    { return _pattern.matcher(filePart).matches();
    }
    else
    { return false;
    }
  }

	/**
   * Convert a glob expression to
   *   a regexp pattern.
   */
	private Pattern globToPattern(String orig)
    throws PatternSyntaxException
	{
		StringBuffer out=new StringBuffer();
		for (int i=0;i<orig.length();i++)
		{ 
			char chr=orig.charAt(i);
			switch (chr)
			{
			case '?':
				out.append('.');
				break;
			case '*':
				out.append(".*");
				break;
			case '.':
				out.append("\\.");
				break;
			default:
				out.append(chr);
			}
		}
		return Pattern.compile(out.toString());
	}

}

