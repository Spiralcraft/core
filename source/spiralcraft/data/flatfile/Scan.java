//
// Copyright (c) 2009 Michael Toth
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import spiralcraft.common.ContextualException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.io.record.InputStreamRecordIterator;
import spiralcraft.io.record.RecordIterator;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.Chain;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

public class Scan
  extends Chain<Resource,Aggregate<Tuple>>
{

  protected URI resourceURI;
  protected RecordFormat format;
  protected Type<List<Tuple>> aggregateType;
  protected String recordSeparator="\r\n";
  
  protected ThreadLocalChannel<Tuple> resultChannel;
  protected int batchSize;
  private boolean skipBadRecords;
  private int bufferSize=4096;
  
  { storeResults=true;
  }
  
  public Scan()
  {
  }
  
  public Scan(RecordFormat format)
  { this.format=format;
  }

  public class ScanTask
    extends ChainTask
  {

    @Override
    protected void work()
      throws InterruptedException
    { 
      Resource resource=null;
      try
      {
        resource=Scan.this.commandChannel.get().getContext();
       
        if (resource==null)
        { resource=Resolver.getInstance().resolve(resourceURI);
        }
        
        
        RecordIterator iterator
          =new InputStreamRecordIterator
            (new BufferedInputStream(resource.getInputStream(),bufferSize)
            ,recordSeparator.getBytes()
            );
            
        RecordCursor cursor
          =new RecordCursor(iterator,format);
        
        try
        {
          if (debug)
          { log.fine("Got "+cursor);
          }
          boolean done=false;
          int count=0;
          EditableArrayListAggregate<Tuple> aggregate=null;
          if (storeResults)
          {
            aggregate=new EditableArrayListAggregate<Tuple>
              (aggregateType);
          }
          while (!done)
          {
            ++count;
            try
            {
              if (!cursor.next())
              { 
                done=true;
                break;
              }
            }
            catch (ParseException x)
            { 
              if (skipBadRecords)
              { 
                Throwable cause=x;
                while (cause.getCause()!=null)
                { cause=cause.getCause();
                }
                
                log.log(Level.INFO,"Skipping bad record #"+count+" : "+x.toString());
                continue;
              }
              else
              { throw x;
              }
            }
            
            resultChannel.push(cursor.getTuple());
            try
            { 
              super.work();
              if (aggregate!=null)
              { aggregate.add(resultChannel.get().snapshot());
              }
            }
            finally
            { resultChannel.pop();
            }
          }
          if (aggregate!=null)
          { addResult(aggregate);
          }
        }
        finally
        { cursor.close();
        }
      }
      catch (DataException x)
      { 
        ContextualException ex
          =new ContextualException
            ("Error scanning flatfile "+resource.getURI(),x);
        if (debug)
        { log.log(Level.WARNING,"Threw",ex);
        }
        addException(ex);
      }
      catch (IOException x)
      { 
        ContextualException ex
          =new ContextualException
            ("Error scanning flatfile "+resource.getURI(),x);
        if (debug)
        { log.log(Level.WARNING,"Threw",ex);
        }
        addException(ex);
      }  
      
    }
  }
  
  /**
   * Specify the number of results to send down the chain for each
   *   iteration. The default value of 0 sends the entire result.
   * 
   * @param batchSize
   */
  public void setBatchSize(int batchSize)
  { this.batchSize=batchSize;
  }
  
  public void setReadBufferSize(int readBufferSize)
  { this.bufferSize=readBufferSize;
  }
  
  @Override
  protected ScanTask task()
  { return new ScanTask();
  }

  public void setSkipBadRecords(boolean skipBadRecords)
  { this.skipBadRecords=skipBadRecords;
  }
  
  /**
   * The Query to run
   * 
   * @param query
   */
  public void setFormat(RecordFormat format)
  { this.format=format;
  }
  
  public void setURI(URI uri)
  { this.resourceURI=uri;
  }
  
  @Override
  public Focus<?> bindImports(Focus<?> focusChain)
    throws ContextualException
  {

    this.contextReflector=BeanReflector.getInstance(Resource.class);
    return super.bindImports(focusChain);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Focus<?> bindExports(Focus<?> focusChain)
    throws ContextualException
  { 
    if (format==null)
    { throw new ContextualException("Format is required");
    }
    try
    { format.bind(focusChain);
    }
    catch (ContextualException x)
    { throw new BindException("Error binding record format '"+format+"'",x);
    }

    
    this.aggregateType
      =Type.<Tuple>getAggregateType((Type<Tuple>) format.getType());
    this.resultReflector
      =DataReflector.<Aggregate<Tuple>>getInstance
        (aggregateType);
    
    resultChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance
          (format.getType())
        );
    focusChain=focusChain.chain(resultChannel);
    
    return super.bindExports(focusChain);
  }
  



}
