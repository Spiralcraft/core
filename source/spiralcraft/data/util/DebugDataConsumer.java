package spiralcraft.data.util;

import java.io.PrintStream;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;

import spiralcraft.data.transport.DataConsumer;
import spiralcraft.data.transport.DataConsumerChain;

/**
 * A DataConsumer which prints data and events in a debugging friendly format
 *   to a PrintStream.
 *
 */
public class DebugDataConsumer<T extends Tuple>
    implements DataConsumerChain<T>
{
  
  private DataConsumer nextConsumer;
  
  private PrintStream out=System.err;

  public void insertDataConsumer(DataConsumerChain consumerChain)
  { 
    if (nextConsumer!=null)
    { consumerChain.setDataConsumer(nextConsumer);
    }
    nextConsumer=consumerChain;
  }

  public void setDataConsumer(DataConsumer consumer)
  { nextConsumer=consumer;
  }

  @SuppressWarnings("unchecked") // Chain pass-through is not runtime type safe
  public void dataAvailable(T tuple) throws DataException
  {
    out.println(tuple.toText("| "));
    if (nextConsumer!=null)
    { nextConsumer.dataAvailable(tuple);
    }
  }

  public void dataFinalize() throws DataException
  {
    out.println("DebugDataConsumer:dataFinalize()");
    if (nextConsumer!=null)
    { nextConsumer.dataFinalize();
    }
  }

  public void dataInitialize(FieldSet fieldSet) throws DataException
  {
    out.println("DebugDataConsumer:dataInitialize(): "+fieldSet.toString());
    if (nextConsumer!=null)
    { nextConsumer.dataInitialize(fieldSet);
    }
  }

}
