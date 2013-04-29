package spiralcraft.app.test;

import spiralcraft.app.kit.AbstractController;
import spiralcraft.app.kit.CallHandler;
import spiralcraft.command.SimpleCommandFactory;
import spiralcraft.time.Clock;
import spiralcraft.time.Instant;

public class Timer
  extends AbstractController<TimerState>
{

  
  public final SimpleCommandFactory start
    =new SimpleCommandFactory()
    {
      @Override
      public void execute()
      { 
        TimerState state=getState();
        if (state.running)
        { log.warning("Timer already running");
        }
        else
        { 
          state.running=true;
          state.startTime=Clock.instance().approxTimeMillis();
          state.startCount++;
          log.info("Timer started at "+new Instant(state.startTime));
        }
      }
    };

  public final SimpleCommandFactory stop
    =new SimpleCommandFactory()
    {
      @Override
      public void execute()
      { 
        TimerState state=getState();
        if (!state.running)
        { log.warning("Timer already stopped");
        }
        else
        { 
          state.running=false;
          state.stopTime=Clock.instance().approxTimeMillis();
          state.lastRunTime=
            state.lastRunTime
              +(state.stopTime-state.startTime);
          log.info("Timer stopped at "+new Instant(state.stopTime));
        }
      }
    };
    
  protected final CallHandler callHandler=new CallHandler();
  
  @Override
  protected void addHandlers()
  { 
    super.addHandlers();
    addHandler(callHandler);
    callHandler.callInterface.add("start",start);
    callHandler.callInterface.add("stop",stop);
  }
    
    
  @Override
  public Class<TimerState> getStateClass()
  { return TimerState.class;
  }
  
  
}

