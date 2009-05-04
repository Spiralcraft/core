package spiralcraft.io.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;

import spiralcraft.io.record.FileRecordIterator;

import spiralcraft.io.record.InputStreamRecordIterator;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;

import spiralcraft.task.Task;

import spiralcraft.test.Test;
import spiralcraft.test.TestResult;

public class RecordIteratorTest
  extends Test
{
 
  private URI fileURI=null;
  
  public void setFileURI(URI fileURI)
  { this.fileURI=fileURI;
  }

  @Override
  protected Task task()
  {
    return new AbstractTask()
    {      
      @Override
      public void work()
      { 
        RandomAccessFile file=null;
        try
        {
          byte[] delimiter=System.getProperty("line.separator").getBytes();
          file=new RandomAccessFile(fileURI.getPath(),"r");
          FileRecordIterator fri
            =new FileRecordIterator
              (file,delimiter);
          byte[] data=new byte[256];
          
          int counter=0;
          while (fri.next())
          { 
            int bytes=fri.read(0,data,0,data.length);
            
            if (counter!=fri.getRecordPointer())
            {
              log.log
                (Level.FINE,"Error: "+counter+"!="+fri.getRecordPointer()+": "
                +new String(data,0,bytes)
                );
            }
            counter++;
          }
          
          while (fri.previous())
          {
            
            counter--;
            int bytes=fri.read(0,data,0,data.length);
            if (counter!=fri.getRecordPointer())
            {
              log.log
                (Level.FINE,"Error: "+counter+"!="+fri.getRecordPointer()+": "
                +new String(data,0,bytes)
                );
            }
          }
  
          InputStream in=new FileInputStream(fileURI.getPath());
          InputStreamRecordIterator isri
            =new InputStreamRecordIterator(in,delimiter);
          counter=0;
          while (isri.next())
          { 
            int bytes=isri.read(0,data,0,data.length);
            if (counter!=isri.getRecordPointer())
            {
              log.log
                (Level.FINE,"Error: "+counter+"!="+isri.getRecordPointer()+": "
                +new String(data,0,bytes)
                );
            }
            counter++;
          }
          addResult(new TestResult(RecordIteratorTest.this,true));
        }
        catch (Exception x)
        { 
          addResult
            (new TestResult
                (RecordIteratorTest.this,false,"Caught exception",x)
            );
          
          addException(x);
          log.log(Level.WARNING,"Error",x);
        }
        finally
        { 
          try
          { file.close();
          }
          catch (IOException x)
          { log.log(Level.WARNING,"Error",x);
          }
        }
        if (chain!=null && exception==null)
        { addResult(executeChild(chain));
        }
      }
      
          
    };
  }

  
    
}
