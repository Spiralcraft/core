//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.text.translator;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

/**
 * <p>Removes a fixed prefix and suffix from the incoming String, such as
 *   quotes, brackets, or other known literals.
 * </p>
 * 
 * <p>Clip is reversible.
 * </p>
 * 
 * @author mike
 */
public class Clip
  implements Translator
{
  
  private String prefix;
  private String suffix;
  private boolean strict;
  
  public void setPrefix(String prefix)
  { this.prefix=prefix;
  }
  
  public void setSuffix(String suffix)
  { this.suffix=suffix;
  }

  /**
   * <p>Causes the incoming translation to throw an exception if the prefix or
   *   suffix is not present.
   * </p>
   * 
   * @param strict
   */
  public void setStrict(boolean strict)
  { this.strict=strict;
  }
  
  @Override
  public String translateIn(
    CharSequence foreign)
    throws ParseException
  {
    
    if (foreign==null)
    { return null;
    }
    String in=foreign.toString();
    if (prefix!=null)
    {
      if (in.startsWith(prefix))
      { in=in.substring(prefix.length());
      }
      else if (strict)
      { 
        throw new ParseException
          ("Input ["+in+"] did not start with prefix ["+prefix+"]"
          ,ParsePosition.atIndex(0,in)
          );
      }
    }
    if (suffix!=null)
    {
      if (in.endsWith(suffix))
      { in=in.substring(0,in.length()-suffix.length());
      }
      else if (strict)
      {
        throw new ParseException
          ("Input ["+in+"] did not end with suffix ["+suffix+"]"
          ,ParsePosition.atIndex(0,in)
          );
      }
      
    }
    return in;
  }

  @Override
  public String translateOut(
    CharSequence local)
  {
    StringBuilder out=new StringBuilder();
    if (prefix!=null)
    { out.append(prefix);
    }
    out.append(local);
    if (suffix!=null)
    { out.append(suffix);
    }
    return out.toString();

  }

  @Override
  public Channel<String> bindChannel(
    Focus<String> focus)
    throws BindException
  { return new TranslatorChannel(this,focus.getSubject());
  }

}
