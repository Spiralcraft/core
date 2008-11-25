package spiralcraft.log;


import spiralcraft.exec.ExecutionContext;

public class ConsoleHandler
  implements EventHandler
{

  private static final Formatter DEFAULT_FORMATTER
    =new DefaultFormatter();
  
  private Formatter formatter=DEFAULT_FORMATTER;
  
  @Override
  public void handleEvent(
    Event event)
  { 
    ExecutionContext.getInstance().err().println(formatter.format(event));
  }

}
