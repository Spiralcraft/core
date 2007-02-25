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


import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StreamTokenizer;
import java.io.IOException;


import java.net.URI;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.exec.SystemExecutionContext;


import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.SchemeImpl;
import spiralcraft.data.core.FieldImpl;

import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.data.pipeline.DataConsumer;
import spiralcraft.data.pipeline.DataConsumerChain;

/**
 * Parse a tabular file and feed the results to a DataHandler
 */
public class Parser
  implements Executable
{
	private DataConsumer consumer;
	private InputStream _in;
  private boolean _readHeader=true;
  private boolean _notypes=false;
  private char _delimiter=',';
  private char _quoteChar='"';
  private boolean _useQuotes=true;
  private EditableTuple tuple;
  private FieldSet _fields;
  private boolean _haltOnErrors=false;
  private boolean _useEscapes=true;
  private DataConsumerChain[] _filters;
  private String _charsetName;
  private ExecutionContext _context;
  
	public void execute(final ExecutionContext context,String[] args)
	{
    _context=context;
		try
		{
      String filename=null;
      String headerFile=null;
      for (int i=0;i<args.length;i++)
      {
        if (args[i].equals("-noheader"))
        { setReadHeader(false);
        }
        else if (args[i].equals("-notypes"))
        { setNoTypes(true);
        }
        else if (args[i].equals("-delimiter"))
        { setDelimiter(args[++i].charAt(0));
        }
        else if (args[i].equals("-header"))
        { 
          headerFile=args[++i];
          setReadHeader(false);
        }
        else if (args[i].equals("-haltOnErrors"))
        { setHaltOnErrors(true);
        }
        else
        { filename=args[i];
        }
      }
      
      if (headerFile!=null)
      { 
        InputStream headerIn=new FileInputStream(headerFile);
        readHeaderFromStream(headerIn);
        headerIn.close();
      }
      
			parse(new FileInputStream(filename)
							,new DataConsumer()
								{
									public void dataInitialize(FieldSet fieldSet)
									{ context.out().println(fieldSet);
									}	
									public void dataAvailable(Tuple data)
									{ context.out().println(data);
									}
                  public void dataFinalize()
                  {}
								}
							);
		}
		catch (Exception x)
		{ x.printStackTrace();
		}
	}
	
  /**
   * Indicate whether the parser should halt on
   *   recoverable data errors. If false, the parser will report
   *   the errors, ignore the record, and and attempt to continue processing
   *   data..
   */
  public void setHaltOnErrors(boolean val)
  { _haltOnErrors=val;
  }

  /**
   * A set of filters that will be applied before the results are sent to
   *   the DataHandler
   */
  public void setFilters(DataConsumerChain[] filters)
  { _filters=filters;
  }

  /**
   * Indicate whether the file has a header with
   *   field names and optionally types.
   */
  public void setReadHeader(boolean readHeader)
  { _readHeader=readHeader;
  }

  /**
   * Whether the parser will expect to read a header from the data file
   *   with field names and optionally types
   */
  public boolean getReadHeader()
  { return _readHeader;
  } 
  
  /**
   * Treat all data as Strings- relaxes the 
   *   constraint to quote strings.
   */
  public void setNoTypes(boolean notypes)
  { _notypes=notypes;
  }

  /**
   * Specify the field delimiter. The default is
   *   a comma.
   */
  public void setDelimiter(char delimiter)
  { _delimiter=delimiter;
  }

  public void setQuote(char quoteChar)
  { _quoteChar=quoteChar;
  }

  public void setUseQuotes(boolean val)
  { _useQuotes=val;
  }
  
  public void setUseEscapes(boolean val)
  { _useEscapes=val;
  }
  
  public void setCharsetName(String val)
  { _charsetName=val;
  }
  
  /**
   * Read the parser field information from the specified input stream
   */
  public void readHeaderFromStream(InputStream in)
    throws IOException,ParseException,DataException
  {
 		StreamTokenizer st
      =new StreamTokenizer(new BufferedReader(new InputStreamReader(in)));
    
    setFieldInfo(readHeader(st));
    setReadHeader(false);
  }
  

	public void parse(InputStream in,DataConsumer sink)
		throws IOException,ParseException,DataException
	{
    if (_context==null)
    { _context=new SystemExecutionContext();
    }
    
		consumer=sink;
		_in=in;
    
    if (_filters!=null && _filters.length>0)
    { 
      for (int i=0;i<_filters.length-1;i++)
      { _filters[i].setDataConsumer(_filters[i+1]);
      }
      
      _filters[_filters.length-1].setDataConsumer(sink);
      consumer=_filters[0];
    }

		StreamTokenizer st;
    if (_charsetName!=null)
    { 
      st=new StreamTokenizer
        (new BufferedReader(new InputStreamReader(_in,_charsetName))
        );
    }
    else
    {
      st=new StreamTokenizer
        (new BufferedReader(new InputStreamReader(_in))
        );
    }

    if (_readHeader)
    {
      if (_fields==null)
      { setFieldInfo(readHeader(st));
      }
      else
      { readHeader(st);
      }
    }

    if (_fields!=null)
    { consumer.dataInitialize(_fields);
    }
    else
    { 
      throw new ParseException
        ("No field info supplied");
    }
		
		readData(st);
	}
	
	private FieldSet readHeader(StreamTokenizer st)
		throws IOException,ParseException,DataException
	{
    SchemeImpl fields=new SchemeImpl();
    
    st.resetSyntax();
    st.eolIsSignificant(true);
    st.parseNumbers();
    st.quoteChar('"');
    st.whitespaceChars(' ',' ');
    if (_delimiter!='\t')
    { st.whitespaceChars('\t','\t');
    }
    st.whitespaceChars('\r','\r');
    st.wordChars('a','z');
    st.wordChars('A','Z');
    st.wordChars('.','.');
    st.wordChars('_','_');
		
		while (st.ttype!=StreamTokenizer.TT_EOF && st.ttype!=StreamTokenizer.TT_EOL)
		{
      FieldImpl field=new FieldImpl();
			String name=readWord("field name",st);
      field.setName(name);
      
      String type=null;
      st.nextToken();
      if ((char) st.ttype=='(')
      { 
        type=readWord("field type",st);
        st.nextToken();
        if ((char) st.ttype!=')')
        { throw new ParseException("Missing ')' in header for field '"+name+"'");
        }
        st.nextToken();

      }
      URI typeUri;
      if (type==null)
      { typeUri=URI.create("java:/spiralcraft/data/types/standard/String");
      }
      else
      { 
        typeUri=URI.create(type);
        if (!typeUri.isAbsolute())
        { 
          typeUri
            =URI.create("java:/spiralcraft/data/types/standard/").resolve(typeUri);
        }
      }
      field.setType(TypeResolver.getTypeResolver().resolve(typeUri));
      
			ensureDelim(st);
      fields.addField(new FieldImpl());
		}
    return fields;
	}

	private void ensureDelim(StreamTokenizer st)
		throws ParseException
	{
		if ( st.ttype!=_delimiter
				 && st.ttype!=StreamTokenizer.TT_EOF
				 && st.ttype!=StreamTokenizer.TT_EOL
				)
		{ throw new ParseException("Found '"+(char) st.ttype+"' ("+st.ttype+"), Expected delimiter ("+_delimiter+") or end of line at line "+st.lineno());
		}
	}

  public void setFieldInfo(FieldSet fieldSet)
  { 
    _fields=fieldSet;
    clearBuffer();
  }

  private void clearBuffer()
  { tuple=new EditableArrayTuple(_fields);
  }
  
	private void readData(StreamTokenizer st)
		throws IOException,ParseException,DataException
	{
		st.resetSyntax();
    if (!_notypes)
    { st.parseNumbers();
    }
    if (!_useQuotes || _quoteChar!='"')
    { st.wordChars('"','"');
    }
    if (!_notypes)
    { 
      st.whitespaceChars(' ',' ');
      if (_delimiter!='\t')
      { st.whitespaceChars('\t','\t');
      }
    }
    else
    {
      st.wordChars(' ','!');
      st.wordChars('#','+');
      if (_delimiter!=',')
      { st.wordChars(',',',');
      }
      st.wordChars('-','/');
      if (_delimiter!='\t')
      { st.wordChars('\t','\t');
      }
      st.wordChars('0','9');
      st.wordChars(':','@');
      st.wordChars('[','`');
      
    }
		st.wordChars('a','z');
		st.wordChars('A','Z');
		st.wordChars('.','.');
		st.wordChars('_','_');
		st.wordChars(':',':');
		st.wordChars('/','/');
		st.wordChars('+','+');
		st.wordChars('=','=');
		st.wordChars('{','~');

    st.wordChars((char) 0x7F,(char) 0xFFFF);
    st.wordChars((char) 1,(char) 9);
    st.wordChars((char) 11,(char) 12);
    st.wordChars((char) 14,(char) 25);
    st.wordChars((char) 0xFF,(char) 0xFF);
    st.whitespaceChars('\r','\r');

    if (_useQuotes)
    { st.quoteChar(_quoteChar);
    }

    if (!_useEscapes)
    { st.wordChars('\\','\\');
    }
 
    st.ordinaryChar(_delimiter);
		st.eolIsSignificant(true);
    
    clearBuffer();
    int dataPos=0;
    int inputRows=0;
    boolean errorFlag=false;
    
    Object dataObject=null;
		while (st.ttype!=StreamTokenizer.TT_EOF)
		{
      st.nextToken();
      if (st.ttype==StreamTokenizer.TT_EOF)
      { 
        // Handle last line or end of file
        if (dataObject!=null || (dataObject==null && dataPos>0))
        {
          // If current line is not empty
          tuple.set(dataPos++,dataObject);
        }
        if (dataPos>0)
        { 
          if (!errorFlag)
          { 
            try
            { consumer.dataAvailable(tuple);
            }
            catch (Exception x)
            { 
              throw new ParseException
                ("line "+(inputRows+1)+": Exception handling data"
                +": buffer is "+tuple.toString()
                ,x
                );
            }
          }
          errorFlag=false;
          inputRows++;
          dataPos=0;
          clearBuffer();
        }
        break;
      }

      if (st.ttype==StreamTokenizer.TT_EOL || st.ttype==_delimiter)
      {
        // Encountered end of field or end of record
        try
        { tuple.set(dataPos++,dataObject);
        }
        catch (ArrayIndexOutOfBoundsException x)
        { 
          if (_haltOnErrors)
          {
            throw new ParseException
              ("line "+(inputRows+1)+": Read too many fields (>"+_fields.getFieldCount()+")"
              +" at "+dataObject+" after '"+st.ttype+"' ("+(int) st.ttype+")"
              +": buffer is "+tuple
              );
          }
          else
          {
            System.err.println
              ("line "+(inputRows+1)+": Read too many fields (>"+_fields.getFieldCount()+")"
              +" at "+dataObject+" after '"+st.ttype+"' ("+(int) st.ttype+")"
              +": buffer is "+tuple
              );
            if (st.ttype==StreamTokenizer.TT_EOL)
            { inputRows++;
            }
            errorFlag=true;
          }
        }
        dataObject=null;
        if (st.ttype==StreamTokenizer.TT_EOL)
        { 
          if (!errorFlag)
          { 
            try
            { consumer.dataAvailable(tuple);
            }
            catch (DataException x)
            { throw x;
            }
            catch (Exception x)
            { 
              throw new ParseException
                ("line "+(inputRows+1)+": Exception handling data"
                +": buffer is "+tuple
                ,x
                );
              
            }
          }
          errorFlag=false;
          inputRows++;
          dataPos=0;
          clearBuffer();
        }
      }
      else if (st.ttype=='"')
			{ 
        if (dataObject==null)
        { dataObject=st.sval;
        }
        else
        { 
          if (_notypes)
          { dataObject=((String) dataObject).concat(st.sval);
          }
          else
          { 
            String err=
              "Found '"
              +st.sval
              +"' in an unexpected place at line "
              +inputRows
              +" after "
              +dataObject
              ;
            if (_haltOnErrors)  
            { throw new ParseException(err);
            }
            else
            { _context.err().println("WARNING: "+err);
            }
          }
        }
      }
      else if (st.ttype==StreamTokenizer.TT_WORD)
      { 
        if (dataObject==null)
        { 
          if (!_notypes)
          { 
            // XXX Re-handle base64
            // XXX Re-handle textfile
            if (st.sval.equals("true"))
            { dataObject=Boolean.TRUE;
            }
            else if (st.sval.equals("false"))
            { dataObject=Boolean.FALSE;
            }
            else
            { 
              String err=
                "Unrecognized token '"+st.sval+"' at line "+inputRows
                ;
              throw new ParseException(err);
            }
          }
          else
          { dataObject=st.sval;
          }
        }
        else
        { 
          String err=
            "Found '"
            +st.sval
            +"' in an unexpected place at line "
            +inputRows
            +" after "
            +dataObject
            ;

          if (_haltOnErrors)  
          { throw new ParseException(err);
          }
          else
          { _context.err().println("WARNING: "+err);
          }
        }
      }
      else if (st.ttype==StreamTokenizer.TT_NUMBER)
			{ 
        if (dataObject==null)
        { 
          if (!_notypes)
          { dataObject=new Double(st.nval);
          }
          else
          { dataObject=String.valueOf(st.nval);
          }
        }
        else
        { 
          String err=
            "Found '"
            +st.nval
            +"' in an unexpected place at line "
            +inputRows
            +" after "
            +dataObject
            ;
            
          if (_haltOnErrors)  
          { throw new ParseException(err);
          }
          else
          { _context.err().println("WARNING: "+err);
          }
        }
			}
			else
			{ 
        String err=
          "Found '"
          +(char) st.ttype
          +"' ("
          + (st.ttype & 0xFFFF)
          +") at line "
          +inputRows
          ;
          
        if (_haltOnErrors)  
        { throw new ParseException(err);
        }
        else
        { _context.err().println("WARNING: "+err);
        }
			}
		}
	}
	
		
	private String readWord(String errorContext,StreamTokenizer st)
		throws ParseException,IOException
	{
		st.nextToken();
		if (st.ttype==StreamTokenizer.TT_WORD || st.ttype=='"')
		{ return st.sval;	
		}
		else
		{ throw new ParseException("Found '"+(char) st.ttype+"', Expected "+errorContext+" at line "+st.lineno());
		}
	}
	



} 

