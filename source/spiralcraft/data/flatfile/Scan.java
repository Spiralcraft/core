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
import java.util.Iterator;
import java.util.List;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.io.record.InputStreamRecordIterator;
import spiralcraft.io.record.RecordIterator;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.kit.Computation;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.Level;
import spiralcraft.task.Chain;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
import spiralcraft.vfs.UnresolvableURIException;

public class Scan<C,R>
  extends Chain<Resource[],R>
{

  protected URI resourceURI;
  protected RecordFormat format;
  protected Type<List<Tuple>> aggregateType;
  protected String recordSeparator="\r\n";
  
  protected ThreadLocalChannel<Tuple> resultChannel;
  protected int batchSize;
  private boolean skipBadRecords;
  private int bufferSize=4096;
  private Expression<C> computeX;
  private Binding<Boolean> filterX;
  private Computation<Tuple,R,C> computation;
  private IterationDecorator<?,Resource> resourceIter;
  private int progressInterval;

  
  public Scan()
  {
  }
  
  public Scan(RecordFormat format)
  { this.format=format;
  }

  public Scan(RecordFormat format,Expression<C> computeX)
  { 
    this.format=format;
    this.computeX=computeX;
  }
  

  public class ScanTask
    extends ChainTask
  {

    
    @Override
    protected void work()
      throws InterruptedException
    {
      if (computation!=null)
      { computation.push();
      }
      
      int count=0;
      try
      {
        if (resourceURI==null && resourceIter!=null)
        {
          Iterator<Resource> it=resourceIter.iterator();
          while (it.hasNext())
          { 
            Resource resource=it.next();
            if (debug)
            { log.fine("Scanning "+resource.getURI());
            }
            count+=workOne(resource);
          }
        }
        else if (resourceURI!=null)
        { 
          try
          { count+=workOne(Resolver.getInstance().resolve(resourceURI));
          }
          catch (UnresolvableURIException x)
          {
            ContextualException ex
              =new ContextualException
                ("Error scanning flatfile "+resourceURI,x);
            if (debug)
            { log.log(Level.WARNING,"Threw",ex);
            }
            addException(ex);
          }
        }
        
        if (computation!=null)
        { 
          computation.checkpoint();
          addResult(computation.getResultChannel().get());
        }  
        
        if (progressInterval!=0)
        { log.info("Completed scanning "+count+" records");
        }
      }
      finally
      { 
        if (computation!=null)
        { computation.pop();
        }
      }
      
    }
    

    protected int workOne(Resource resource)
      throws InterruptedException
    { 
      int count=0;
      try
      {
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
            
            resultChannel.push(cursor.getTuple().snapshot());
            try
            { 
              if (filterX==null || Boolean.TRUE.equals(filterX.get()))
              {
                super.work();
                if (computation!=null)
                { computation.update();
                }
              }
            }
            finally
            { resultChannel.pop();
            }
            
            if (progressInterval!=0 && count%progressInterval==0)
            { log.info("Scanned "+count+" in "+resource.getURI());
            }
          }
          
          if (progressInterval!=0)
          { log.info("Scanned "+count+" in "+resource.getURI());
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
      return count;
      
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
  
  /**
   * Output a progress log message after every n'th record has been processed.
   * 
   * @param progressInterval
   */
  public void setProgressInterval(int progressInterval)
  { this.progressInterval=progressInterval;
  }

  /**
   * A computation to perform and return as a result
   * 
   * @param computeX
   */
  public void setComputeX(Expression<C> computeX)
  { this.computeX=computeX;
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
  
  public void setFilterX(Expression<Boolean> filterX)
  { this.filterX=new Binding<Boolean>(filterX);
  }
  
  
  public void setURI(URI uri)
  { this.resourceURI=uri;
  }
  
  @Override
  public Focus<?> bindImports(Focus<?> focusChain)
    throws ContextualException
  {
    if (contextX!=null)
    { 
      throw new BindException
        ("contextX not permitted for Scan. Context is a Resource[] ");
    }
    this.contextReflector=BeanReflector.getInstance(Resource[].class);
    return super.bindImports(focusChain);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
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

    resourceIter
      =this.contextChannel
        .<IterationDecorator>
           decorate(IterationDecorator.class);
         
    resultChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance
          (format.getType())
        );
    focusChain=focusChain.chain(resultChannel);
    
    if (filterX!=null)
    { filterX.bind(focusChain);
    }
    
    if (computeX!=null)
    { 
      this.storeResults=true;
      computation
        =new Computation<Tuple,R,C>(resultChannel,focusChain,computeX);
      if (debug)
      { computation.setDebug(true);
      }
      this.resultReflector
        =computation.getResultChannel().getReflector();
      
    }
    
    return super.bindExports(focusChain);
  }
  



}
