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

import java.io.IOException;
import java.net.URI;

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.io.record.InputStreamRecordIterator;
import spiralcraft.io.record.RecordIterator;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.Scenario;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

public class Scan
  extends Scenario
{

  protected URI resourceURI;
  protected RecordFormat format;
  protected String recordSeparator="\r\n";
  
  protected ThreadLocalChannel<Tuple> resultChannel;
  protected int batchSize;
  private boolean skipBadRecords;
  

  public class ScanTask
    extends ChainTask
  {

    @Override
    protected void work()
      throws InterruptedException
    { 
      try
      {
        Resource resource=Resolver.getInstance().resolve(resourceURI);
        
        RecordIterator iterator
          =new InputStreamRecordIterator
            (resource.getInputStream()
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
                
                log.log(Level.INFO,"Skipping bad record #"+count,x);
                continue;
              }
              else
              { throw x;
              }
            }
            
            resultChannel.push(cursor.getTuple());
            try
            { super.work();
            }
            finally
            { resultChannel.pop();
            }
          }

        }
        finally
        { cursor.close();
        }
      }
      catch (DataException x)
      { 
        if (debug)
        { log.log(Level.WARNING,"Threw",x);
        }
        addException(x);
      }
      catch (IOException x)
      { 
        if (debug)
        { log.log(Level.WARNING,"Threw",x);
        }
        addException(x);
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
  public void bindChildren(
    Focus<?> focusChain)
    throws BindException
  {
    
    format.bind(focusChain);
    
    resultChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance
          (format.getType())
        );
    focusChain=focusChain.chain(resultChannel);
    super.bindChildren(focusChain);
  }


}
