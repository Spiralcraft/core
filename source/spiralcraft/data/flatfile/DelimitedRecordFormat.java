//
// Copyright (c) 2009,2009 Michael Toth
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.lang.TupleReflector;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;

public class DelimitedRecordFormat
  implements RecordFormat
{

  
  private String fieldSeparator;
  private FieldMapping<?>[] fields;
  private Channel<Tuple> channel;
  private Type<?> type;
  private FieldSet fieldSet;
  private String charset;
  private boolean trim;
  
  public void setCharset(String charsetName)
  { 
    this.charset=charsetName;
    Charset.forName(charsetName);
  }
    
  public void setFields(FieldMapping<?>[] fields)
  { this.fields=fields;
  }
  
  public void setFieldSeparator(String separator)
  { this.fieldSeparator=separator;
  }
  
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  
  public void setFieldSet(FieldSet fieldSet)
  { this.fieldSet=fieldSet;
  }
  
  public Type<?> getType()
  { return type;
  }
  
  public FieldSet getFieldSet()
  { return type!=null?type.getFieldSet():fieldSet;
  }
  
  /** 
   * Trim leading and trailing whitespace from the record
   * 
   * @return
   */
  public void setTrim(boolean trim)
  { this.trim=trim;
  }
  
  @Override
  public byte[] format(Tuple data)
    throws IOException
  {
    channel.set(data);
    StringWriter writer=new StringWriter();
    boolean first=true;
    for (FieldMapping<?> mapping : fields)
    { 
      if (first)
      { first=false;
      }
      else
      { writer.write(fieldSeparator);
      }
      mapping.format(writer);
    }
    String out=writer.toString();
    if (trim)
    { out=out.trim();
    }
    return out.getBytes(charset);
    
  }

  @Override
  public void parse(
    byte[] record,
    Tuple target)
    throws ParseException,IOException
  {
    channel.set(target);
    StringReader reader=new StringReader(new String(record,charset));
    for (FieldMapping<?> mapping : fields)
    { 
      try
      { mapping.parse(reader);
      }
      catch (AccessException x)
      { 
        throw new ParseException
          ("Error reading field "+mapping.getX().getText(),x);
      }
      
    }
    String rest=readRest(reader);
    if (rest!=null)
    { throw new ParseException("Unread data in record: "+rest);
    }
    
    
    
  }

  private String readRest(Reader reader)
    throws IOException
  {
    int read;
    StringBuffer buf=new StringBuffer();
    while ( (read=reader.read())>-1)
    { buf.append((char) read);
    }
    if (buf.length()>0)
    { return buf.toString();
    }
    else
    { return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  private void automapFields()
  {
    FieldSet fieldSet=type!=null?type.getFieldSet():this.fieldSet;
    fields=new FieldMapping[fieldSet.getFieldCount()];
    int i=0;
    for (Field<?> field: fieldSet.fieldIterable())
    { 
      FieldMapping mapping=new FieldMapping();
      mapping.setX(Expression.create(field.getName()));
      fields[i++]=mapping;
    }
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    
    if (type!=null)
    { channel=new SimpleChannel<Tuple>(DataReflector.<Tuple>getInstance(type));
    }
    else
    { 
      channel=new SimpleChannel<Tuple>
        (TupleReflector.<Tuple>getInstance(fieldSet));
    }
    
    if (fields==null)
    { automapFields();
    }
    
    focusChain=focusChain.chain(channel);
    for (FieldMapping<?> mapping : fields)
    {
      mapping.getEncoder().setFieldSeparator(fieldSeparator);
      mapping.bind(focusChain);
    }
    return focusChain;
  }
  
}
