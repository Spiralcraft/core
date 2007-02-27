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
package spiralcraft.data.flatfile;

import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;
import spiralcraft.data.DataException;

import spiralcraft.data.pipeline.DataConsumer;

import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.IOException;

import java.text.DecimalFormat;

public class Writer
  implements DataConsumer
{
  private BufferedWriter _out;
  private DecimalFormat _format
    =new DecimalFormat("##############################0.#####################");
  private boolean _autoFlush=true;
  private String _newLine=System.getProperty("line.separator");
  private FieldSet fields;

  public Writer(OutputStream out)
  { _out=new BufferedWriter(new OutputStreamWriter(out),524288);
  }
      
  public Writer(OutputStream out,boolean autoFlush)
  { 
    _out=new BufferedWriter(new OutputStreamWriter(out),524288);
    _autoFlush=autoFlush;
  }

  public void dataFinalize()
    throws DataException
  { 
    try
    { _out.flush();
    }
    catch (IOException x)
    { throw new DataException("Error flushing output: "+x,x);
    }
  }

  public void dataInitialize(FieldSet fields)
  { 
    this.fields=fields;
    try
    {
      boolean first=true;
      for (Field field: fields.fieldIterable())
      {
        if (!first)
        { _out.write(",");
        }
        else
        { first=false;
        }
        
        _out.write(field.getName());
        if (field.getType()!=null)
        { 
          _out.write("(");
          _out.write(field.getType().getURI().toString());
          _out.write(")");
        }
      }
      _out.write(_newLine);
      if (_autoFlush)
      { _out.flush();
      }
    }
    catch (IOException x)
    { throw new RuntimeException("IOException writing header",x);
    }
  }
  
  public void dataAvailable(Tuple data)
    throws DataException
  {
    try
    {
      boolean first=true;
      for (Field field: fields.fieldIterable())
      {
        if (!first)
        { _out.write(",");
        }
        else
        { first=false;
        }

        Object val=field.getValue(data);
  
        if (val!=null)
        {
          //XXX Deal with inputstreams
          if (val instanceof String)
          {
            _out.write("\"");
            _out.write(escape(val.toString()));
            _out.write("\"");
          }
          else if (val instanceof Number)
          { _out.write(_format.format(val));
          }
          else if (val instanceof Boolean)
          { _out.write( ((Boolean) val).booleanValue()?"true":"false");
          }
        }
  
      }
      _out.write(_newLine);
      if (_autoFlush)
      { _out.flush();
      }
    }
    catch (IOException x)
    { throw new RuntimeException("IOException writing data",x);
    }
  }

  public static String escape(String input)
  {
    StringBuffer out=new StringBuffer();
    int len=input.length();
    char[] chars=input.toCharArray();
    for (int i=0;i<len;i++)
    {
      switch (chars[i])
      {
      case '\r':
        out.append("\\r");
        break;
      case '\n':
        out.append("\\n");
        break;
      case '\t':
        out.append("\\t");
        break;
      case '"':
        out.append("\\\"");
        break;
      case '\\':
        out.append("\\\\");
        break;
      default:
        out.append(chars[i]);
        break;
      }
    }
    return out.toString();
  }

}  
