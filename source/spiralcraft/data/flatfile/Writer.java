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
package spiralcraft.data.flatfile;

import spiralcraft.data.DataConsumer;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;
import spiralcraft.data.DataException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.SimpleFocus;


import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.IOException;

//import java.text.DecimalFormat;

/**
 * 
 * <p>Writes the data stream to a .csv formatted file
 * </p>
 * @author mike
 *
 */
public class Writer
  implements DataConsumer<Tuple>
{
  private BufferedWriter _out;
//  private DecimalFormat _format
//    =new DecimalFormat("##############################0.#####################");
  private boolean _autoFlush=true;
  private String _newLine=System.getProperty("line.separator");
//  private FieldSet fields;
  private boolean writeHeader;
  
  private DelimitedRecordFormat recordFormat;

  public Writer(OutputStream out)
  { _out=new BufferedWriter(new OutputStreamWriter(out),524288);
  }
      
  public Writer(OutputStream out,boolean autoFlush)
  { 
    _out=new BufferedWriter(new OutputStreamWriter(out),524288);
    _autoFlush=autoFlush;
  }
  
  public Writer()
  {
  }
  
  @Override
  public void setDebug(boolean debug)
  {
  }
  
  public void setAutoFlush(boolean autoFlush)
  { this._autoFlush=autoFlush;
  }
  
  public void setOutputStream(OutputStream out)
  { this._out=new BufferedWriter(new OutputStreamWriter(out),524288);
  }
  
  public void setWriteHeader(boolean writeHeader)
  { this.writeHeader=writeHeader;
  }

  public void setRecordFormat(DelimitedRecordFormat recordFormat)
  { this.recordFormat=recordFormat;
  }
  
  @Override
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

  @Override
  public void dataInitialize(FieldSet fields)
    throws DataException
  { 
    // TODO: Make sure that the supplied FieldSet is compatible with
    //   that in the RecordFormat. 
    
    
//    this.fields=fields;
    if (recordFormat==null)
    { 
      DelimitedRecordFormat recordFormat
        =new DelimitedRecordFormat();
      recordFormat.setFieldSeparator(",");
      recordFormat.setFieldSet(fields);
      this.recordFormat=recordFormat;
      
      try
      { recordFormat.bind(new SimpleFocus<Void>());
      }
      catch (BindException x)
      { throw new DataException("Error setting up record format",x);
      }
      
    }
    else
    { 
      if (!(recordFormat.getFieldSet()==fields))
      { 
        throw new DataException
          ("FieldSet must be identical to that provided "
          +" by the supplied DelimitedRecordFormat");
      }
    }

    // TODO: Provide a generic way to have RecordFormat write a header.
    
    
    if (writeHeader)
    {
      try
      {
        boolean first=true;
        for (Field<?> field: fields.fieldIterable())
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
  }
  
//  @SuppressWarnings("unchecked")
  @Override
  public void dataAvailable(Tuple data)
    throws DataException
  {
    try
    {
      _out.write(new String(recordFormat.format(data)));
/*
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
          if (val instanceof Number)
          { _out.write(_format.format(val));
          }
          else if (val instanceof Boolean)
          { _out.write( ((Boolean) val).booleanValue()?"true":"false");
          }
          else
          { 
            if (field.getType().isStringEncodable())
            {
              // Let the Type turn it into a string
              _out.write("\"");
              _out.write
                (escape
                  (((Type<Object>) field.getType()).toString(val)
                  )
                );
              _out.write("\"");
            }
          }
        }
  
      }
*/      
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
