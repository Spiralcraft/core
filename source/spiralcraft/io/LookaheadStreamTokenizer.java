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
package spiralcraft.io;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

public class LookaheadStreamTokenizer
{

  public final StreamTokenizer lookahead;
  public double nval;
  public String sval;
  public int ttype;
  
  private int lineno;  
  private boolean pushback;
  private boolean started;
  
  public LookaheadStreamTokenizer(Reader reader)
  { lookahead=new StreamTokenizer(reader);
  }
  
  public int nextToken()
    throws IOException
  {
    if (!started)
    { 
      // Initial priming
      started=true;
      lookahead.nextToken();
    }
    if (pushback)
    {
      pushback=false;
      return ttype;
    }
    else
    {
      nval=lookahead.nval;
      sval=lookahead.sval;
      ttype=lookahead.ttype;
      lineno=lookahead.lineno();
      lookahead.nextToken();
      return ttype;
    }
  }
  
  public int lineno()
  { return lineno;
  }
  
  public void pushBack()
  { pushback=true;
  }
  
  @Override
  public String toString()
  { 
    return super.toString()
      +" ["+(char) ttype+"] @line "+lineno+": lookahead="+lookahead;
  }
}
