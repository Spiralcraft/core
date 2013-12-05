//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.time;

import java.text.ParseException;
import java.text.ParsePosition;

/**
 * ISO 8601 duration format
 * 
 * @author mike
 *
 */
public class StandardDurationFormat
{
  
  public Duration parse(String iso8601input)
    throws ParseException
  {
    int years=0;
    int months=0;
    int weeks=0;
    int days=0;
    int hours=0;
    int minutes=0;
    int seconds=0;
    long millis=0;
    
    boolean inTime=false;
    int datePos=0;
    int timePos=0;
    
    ParsePosition pos=new ParsePosition(0);
    char[] input=iso8601input.toCharArray();
    
    while (pos.getIndex()<input.length)
    { 
      int i=pos.getIndex();
      if (input[i]=='T')
      { 
        if (!inTime)
        {
          inTime=true;
          pos.setIndex(pos.getIndex()+1);
          continue;
        }
        else
        { throw new ParseException("Token 'T' cannot be used here",i);
        }
      }

      // Read number
      Integer num=null;
      Double decimal=null;
      int exp=0;
      if (Character.isDigit(input[i]))
      { num=readInteger(input,pos);
      }
      
      if (num==null)
      { throw new ParseException("Expected a number",i);
      }
      
      i=pos.getIndex();
      if (input[i]=='.')
      { 
        // Read decimal part
        i++;
        pos.setIndex(i);
        Integer decimalPart=null;
        if (Character.isDigit(input[i]))
        { decimalPart=readInteger(input,pos);
        }
        if (decimalPart==null)
        { throw new ParseException("Expected a number",i);
        }
        exp=pos.getIndex()-i;
        decimal=((double) decimalPart) * Math.pow(10,-exp);
        i=pos.getIndex();
      }
      
      // Read field indicator
      if (!inTime)
      { 
        if (input[i]=='Y' && datePos==0)
        { 
          
          years=num;
          datePos=1;
        }
        else if (input[i]=='M' && datePos<=1)
        { 
          months=num;
          datePos=2;
        }
        else if (input[i]=='W' && datePos<=2)
        { 
          weeks=num;
          datePos=3;
        }        
        else if (input[i]=='D' && datePos<=3)
        { 
          days=num;
          datePos=4;
        }
        else
        { throw new ParseException("Unexpected symbol '"+input[i]+"'",i);
        }
        
      }
      else
      {
        if (input[i]=='H' && timePos==0)
        { 
          hours=num;
          timePos=1;
        }
        else if (input[i]=='M' && timePos<=1)
        { 
          minutes=num;
          timePos=2;
        }
        else if (input[i]=='S' && timePos<=2)
        { 
          seconds=num;
          timePos=3;
          if (decimal!=null && decimal>0)
          { millis= Math.round(decimal * 1000);
          }
        }        
        else
        { throw new ParseException("Unexpected symbol '"+input[i]+"'",i);
        }
      }
      i++;
      pos.setIndex(i);

      
        
        
    }
    
    Duration duration=null;
    if (millis>0)
    { duration=new Duration(millis,Chronom.MILLISECOND,duration);
    }      
    if (seconds>0)
    { duration=new Duration(seconds,Chronom.SECOND,duration);
    }
    if (minutes>0)
    { duration=new Duration(minutes,Chronom.MINUTE,duration);
    }
    if (hours>0)
    { duration=new Duration(hours,Chronom.HOUR,duration);
    }
    if (days>0)
    { duration=new Duration(days,Chronom.DAY,duration);
    }
    if (weeks>0)
    { duration=new Duration(weeks,Chronom.WEEK,duration);
    }
    if (months>0)
    { duration=new Duration(months,Chronom.MONTH,duration);
    }
    if (years>0)
    { duration=new Duration(years,Chronom.YEAR,duration);
    }
    return duration;
  }
  
  public String format(Duration input)
  {
    
    
    if (input==null)
    { return null;
    }
    
    StringBuilder buf=new StringBuilder();
    boolean inTime=false;
    while (input!=null)
    {
      long count=input.getCount();
      switch (input.getUnit())
      {
        case YEAR:
          buf.append(count).append("Y");
          break;
        case MONTH:
          buf.append(count).append("M");
          break;
        case WEEK:
          buf.append(count).append("W");
          break;
        case DAY:
          buf.append(count).append("D");
          break;
        case HOUR:
          if (!inTime)
          { 
            buf.append("T");
            inTime=true;
          }
          buf.append(count).append("H");
          break;
        case MINUTE:
          if (!inTime)
          { 
            buf.append("T");
            inTime=true;
          }
          buf.append(count).append("M");
          break;
        case SECOND:
          if (!inTime)
          { 
            buf.append("T");
            inTime=true;
          }
          
          
          if (input.getRest()!=null 
              && input.getRest().getUnit()==Chronom.MILLISECOND
              )
          { 
            input=input.getRest();
            buf.append( ((double) count) + ((double) input.getCount()) *0.001 );
          }
          else
          { buf.append(count);
          }
          buf.append("S");
          break;
        case MILLISECOND:
          if (!inTime)
          { 
            buf.append("T");
            inTime=true;
          }
          buf.append( ((double) count) *0.001).append("S");
          break;
        case CENTURY:
          buf.append(100*count).append("Y");
          break;
        case DECADE:
          buf.append(10*count).append("Y");
          break;
        case HALFDAY:
          if (!inTime)
          { 
            buf.append("T");
            inTime=true;
          }
          buf.append(12*count).append("H");
          break;
        case MILLENIUM:
          buf.append(1000*count).append("Y");
          break;
        case NANOSECOND:
          if (!inTime)
          { 
            buf.append("T");
            inTime=true;
          }
          buf.append( ((double) count) *0.000000001).append("S");
          break;
        case QUARTER:
          buf.append(3*count).append("M");
          break;
      }
      input=input.getRest();
    }
    return buf.toString();
  }
  

  
  private Integer readInteger(char[] input,ParsePosition pos)
  {
    StringBuilder number=new StringBuilder();
    int i=pos.getIndex();
    while (Character.isDigit(input[i]))
    { 
      number.append(input[i]);
      i++;
    }
    if (i==pos.getIndex())
    { return null;
    }
    else
    { 
      pos.setIndex(i);
      return Integer.valueOf(number.toString());
      
    }
    
  }
}
