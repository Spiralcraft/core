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
package spiralcraft.meta;

import java.util.HashMap;
import java.util.LinkedList;

import spiralcraft.common.Immutable;
import spiralcraft.text.LookaheadParserContext;
import spiralcraft.text.ParseException;

/**
 * A version identifier
 * 
 * @author mike
 *
 */
@Immutable
public class Version
  implements Comparable<Version>
{

  private static final HashMap<String,Integer> statusMap
    =new HashMap<String,Integer>();
  static
  {
  
    statusMap.put("dev",1);
    statusMap.put("alpha",2);
    statusMap.put("beta",3);
    statusMap.put("rc",4);
    statusMap.put(null,5);
    statusMap.put("update",6);
  };
  
  
  private int[] release;
  private String status;
  private Integer statusVal;
  private Integer statusBuild;
  private String extra;
  
  public Version(String versionString)
    throws ParseException
  { 
    LookaheadParserContext parserContext
      =new LookaheadParserContext(versionString);
    parse(parserContext);
  }
  
  
  
  @Override
  public String toString()
  {
    StringBuilder str=new StringBuilder();
    boolean first=true;
    for (int i:release)
    { 
      if (first)
      { first=false;
      }
      else
      { str.append('.');
      }
      str.append(i);
    }
    if (status!=null && statusBuild!=null)
    {
      str.append("-");
      if (status!=null)
      { str.append(status);
      }
      if (statusBuild!=null)
      { str.append(statusBuild);
      }
    }
    if (extra!=null)
    { 
      str.append("-");
      str.append(extra);
    }
    return str.toString();
  }
  
  
  private void parse(LookaheadParserContext pc)
    throws ParseException
  { 
    
    LinkedList<Integer> releaseList
      =new LinkedList<Integer>();
    while (!pc.isEof() && Character.isDigit(pc.getCurrentChar()))
    { 
      releaseList.add(readNumber(pc));
      if (pc.getCurrentChar()=='.')
      { pc.advance();
      }
    }
    release=new int[releaseList.size()];
    int i=0;
    for (Integer val:releaseList)
    { release[i++]=val;
    }
    
    if (!pc.isEof())
    { 
    
      if (pc.getCurrentChar()=='-')
      { 
        pc.advance();
        if (Character.isDigit(pc.getCurrentChar()))
        { statusBuild=readNumber(pc); 
        }
        else
        { 
          status=readString(pc);
          if (Character.isDigit(pc.getCurrentChar()))
          { statusBuild=readNumber(pc); 
          }
          
          if (!pc.isEof())
          { 
            if (pc.getCurrentChar()=='-')
            { 
              pc.advance();
              extra=readRest(pc);
            }
            else
            {
              throw new ParseException
                ("Expected '-' or EOF, not '"+pc.getCurrentChar()+"'"
                ,pc.getPosition()
                );
            }
          }
         
        }
      }
      else
      { 
        throw new ParseException
          ("Expected '-' or EOF, not '"+pc.getCurrentChar()+"'"
          ,pc.getPosition()
          );
      }
    }
    
    statusVal=statusMap.get(status);
    if (statusVal==null)
    { statusVal=-1;
    }
    
    if (statusBuild==null)
    { statusBuild=0;
    } 
  }
  
  
  private int readNumber(LookaheadParserContext pc)
    throws ParseException
  {
    StringBuilder str
      =new StringBuilder();
    while (!pc.isEof() 
           && Character.isDigit(pc.getCurrentChar())
           )
    { 
      str.append(pc.getCurrentChar());
      pc.advance();
    }
    return Integer.parseInt(str.toString());
    
  }

  private String readString(LookaheadParserContext pc)
    throws ParseException
  {
    StringBuilder str
      =new StringBuilder();
    while (!pc.isEof() 
            && Character.isAlphabetic(pc.getCurrentChar())
          )
    { 
      str.append(pc.getCurrentChar());
      pc.advance();
    }
    return str.toString();
    
  }

  private String readRest(LookaheadParserContext pc)
    throws ParseException
  {
    StringBuilder str
      =new StringBuilder();
    while (!pc.isEof())
    { 
      str.append(pc.getCurrentChar());
      pc.advance();
    }
    return str.toString();
    
  }



  @Override
  public int compareTo(Version o)
  {
    for (int i=0;i<release.length;i++)
    {
      if (i==o.release.length)
      { return 1;
      }
      else if (release[i]<o.release[i])
      { return -1;
      }
      else if (release[i]>o.release[i])
      { return 1;
      }
    }
    
    if (statusVal<o.statusVal)
    { return -1;
    }
    else if (statusVal>o.statusVal)
    { return 1;
    }
    
    if (statusBuild<o.statusBuild)
    { return -1;
    }
    else if (statusBuild>o.statusBuild)
    { return 1;
    }
    
    return 0;
  }
}
