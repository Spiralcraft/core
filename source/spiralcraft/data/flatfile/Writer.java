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

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.DataException;
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
  private java.io.Writer out;
//  private DecimalFormat _format
//    =new DecimalFormat("##############################0.#####################");
  private boolean autoFlush=true;
  private String recordSeparator=System.getProperty("line.separator");
//  private FieldSet fields;
  private boolean writeHeader;
  
  private DelimitedRecordFormat recordFormat;

  public Writer(OutputStream out)
  { 
    this.out=new BufferedWriter(new OutputStreamWriter(out),524288);
  }
      
  public Writer(OutputStream out,boolean autoFlush)
  { 
    this.out=new BufferedWriter(new OutputStreamWriter(out),524288);
    this.autoFlush=autoFlush;
  }
  
  public Writer()
  {
  }
  
  public void setRecordSeparator(String recordSeparator)
  { this.recordSeparator=recordSeparator;
  }
  
  @Override
  public void setDebug(boolean debug)
  {
  }
  
  public void setAutoFlush(boolean autoFlush)
  { this.autoFlush=autoFlush;
  }
  
  public void setOutputStream(OutputStream out)
  {
    this.out=new BufferedWriter(new OutputStreamWriter(out),524288);
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
    { out.flush();
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
      catch (ContextualException x)
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
        out.append(new String(recordFormat.formatHeader()));
        out.append(recordSeparator);
        if (autoFlush)
        { out.flush();
        }
      }
      catch (IOException x)
      { throw new DataException("IOException writing data",x);
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
      out.append(new String(recordFormat.format(data)));
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
      out.append(recordSeparator);
      if (autoFlush)
      { out.flush();
      }
    }
    catch (IOException x)
    { throw new DataException("IOException writing data",x);
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
