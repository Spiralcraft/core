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
package spiralcraft.data.flatfile.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import spiralcraft.data.DataException;
import spiralcraft.data.flatfile.RecordCursor;
import spiralcraft.data.flatfile.RecordFormat;
import spiralcraft.io.record.InputStreamRecordIterator;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

public class FormatTest
  extends Scenario<Task,Void>
{

  private URI inputURI;
  private URI outputURI;
  private String recordSeparator;
  private RecordFormat recordFormat;
  
  public void setInputURI(URI inputURI)
  { this.inputURI=inputURI;
  }
  
  public void setOutputURI(URI outputURI)
  { this.outputURI=outputURI;
  }
  
  public void setRecordSeparator(String recordSeparator)
  { this.recordSeparator=recordSeparator;
  }
  
  public void setRecordFormat(RecordFormat recordFormat)
  { this.recordFormat=recordFormat;
  }
  
  @Override
  protected Task task()
  {
    return new AbstractTask()
    {
      { debug=FormatTest.this.debug;
      }
      
      @Override
      public void work()
      {
        InputStream in=null;
        OutputStream out=null;
        try
        {
          Resource input=Resolver.getInstance().resolve(inputURI);
          Resource output=Resolver.getInstance().resolve(outputURI);
        
          in=input.getInputStream();
          out=output.getOutputStream();
        
    
          InputStreamRecordIterator recordIterator
            =new InputStreamRecordIterator
              (in,recordSeparator.getBytes());
    
//          while (recordIterator.next())
//          { System.out.println(new String(recordIterator.read()));
//          }
//          
//          in.close();
//          
//          in=input.getInputStream();
//          recordIterator
//            =new InputStreamRecordIterator
//              (in,recordSeparator.getBytes());
          
          RecordCursor recordCursor
            =new RecordCursor(recordIterator,recordFormat);  
        
          log.log
            (Level.FINE,"Record separator= ["
                +ArrayUtil.format(recordSeparator.getBytes(),",",null)
                +"]");
           
          int count=0;
          while (recordCursor.dataNext())
          {
            count++;
            if (debug)
            { log.log(Level.DEBUG,"Processing "+recordCursor.dataGetTuple());
            }
            
            out.write(recordFormat.format(recordCursor.dataGetTuple()));
            out.write(recordSeparator.getBytes());
          
          }
          
          log.log(Level.FINE,"Processed "+count+" records");
          
          out.flush();
          in.close();
          out.close();
          
        }
        catch (DataException x)
        { log.log(Level.SEVERE,"Error running test",x);
        }
        catch (IOException x)
        { log.log(Level.SEVERE,"Error running test",x);
        }
        finally
        {
          if (out!=null)
          { 
            try
            { out.close();
            }
            catch (IOException x)
            { }
          }
          if (in!=null)
          { 
            try
            { in.close();
            }
            catch (IOException x)
            { }
          }
        }
      }
      
      
    };
    
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return recordFormat.bind(focusChain);
  }

}

