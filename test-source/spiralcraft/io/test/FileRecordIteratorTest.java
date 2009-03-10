package spiralcraft.io.test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

import spiralcraft.io.FileRecordIterator;
import spiralcraft.log.Level;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

public class FileRecordIteratorTest
  extends Scenario<Task,Void>
{
 
  private URI fileURI=URI.create("data/io/RecordFile.small.log");
  
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
          file=new RandomAccessFile(fileURI.getPath(),"r");
          FileRecordIterator fri
            =new FileRecordIterator
              (file,System.getProperty("line.separator").getBytes());
          byte[] data=new byte[256];
          
          while (fri.next())
          { 
            int bytes=fri.read(0,data,0,data.length);
            log.log
              (Level.FINE,fri.getRecordPointer()+": "+new String(data,0,bytes));
          }
          
          while (fri.previous())
          {
            int bytes=fri.read(0,data,0,data.length);
            log.log
              (Level.FINE,fri.getRecordPointer()+": "
              +new String(data,0,bytes)
              );
          }
  
        }
        catch (Exception x)
        { log.log(Level.WARNING,"Error",x);
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
      }
      
          
    };
  }

  
    
}
