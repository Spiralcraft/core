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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.task.Collect;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.task.Chain;
import spiralcraft.task.Task;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;


/**
 * <p>Manages the lifecycle of an DataConsumer that consumes data output by 
 *   subtasks.
 * </p>
 * 
 * 
 * <p>Initializes and finalizes the DataConsumer once per-execution
 * </p>

 * 
 * @author mike
 *
 */
public class Output<Titem extends Tuple>
  extends Chain<Void,Void>
{

  private ThreadLocalChannel<Writer> consumerChannel;
  private URI resourceURI;
  protected DelimitedRecordFormat format;
  protected String recordSeparator="\r\n";  
  private boolean writeHeader;
  
  { 
    storeResults=true;
  }

  /**
   * Command that is referenceable from subtasks to add an item.
   * 
   * @param item
   * @return
   */
  public Command<Collect<Titem>,Void,Void> commandOutput(final Titem item)
  { 
    if (debug)
    { log.debug("Returning command "+item);
    }
    
    return new CommandAdapter<Collect<Titem>,Void,Void>()
    {
      @Override
      protected void run()
      { 
        if (debug)
        { log.debug("Adding "+item);
        }
        try
        { consumerChannel.get().dataAvailable(item);
        }
        catch (DataException x)
        { setException(x);
        }
      } 
    };
  }

  public void setFormat(DelimitedRecordFormat format)
  { this.format=format;
  }
  
  public void setWriteHeader(boolean writeHeader)
  { this.writeHeader=writeHeader;
  }
  
  public void setURI(URI uri)
  { this.resourceURI=uri;
  }
  
  public void setRecordSeparator(String recordSeparator)
  { this.recordSeparator=recordSeparator;
  }
  
  @Override
  public Focus<?> bindImports(Focus<?> focus)
    throws ContextualException
  { 
    resultReflector=BeanReflector.getInstance(byte[].class);
    return super.bindImports(focus);
  }
  
  @Override
  public void bindChildren(
    Focus<?> focusChain)
    throws ContextualException
  {

    format.bind(focusChain);
    consumerChannel
      =new ThreadLocalChannel<Writer>
        (BeanReflector.<Writer>getInstance(Writer.class)
        ,true
        );
    super.bindChildren(focusChain.chain(consumerChannel));
  }

  
  @Override
  protected Task task()
  {
    return new ChainTask()
    {
        
      @Override
      public void work()
        throws InterruptedException
      {
        try
        {
          OutputStream out;
          if (resourceURI!=null)
          {
            Resource resource=Resolver.getInstance().resolve(resourceURI);
            out=resource.getOutputStream();
          }
          else
          { out=new ByteArrayOutputStream();
          }
          
          try
          { 
            Writer writer=new Writer(out);
            writer.setRecordSeparator(recordSeparator);
            writer.setRecordFormat(format);
            writer.setWriteHeader(writeHeader);
            writer.dataInitialize(format.getType().getFieldSet());
            consumerChannel.push(writer);
            try
            { super.work();
            }
            finally
            { 
              consumerChannel.pop();
              writer.dataFinalize();
            }
          }
          finally
          {
            out.flush();
            if (resourceURI==null)
            { addResult(((ByteArrayOutputStream) out).toByteArray());
            }
            out.close();
          }
        }
        catch (IOException x)
        { addException(x);
        }
        catch (DataException x)
        { addException(x);
        }
      
      }
    };
  }
  
}
