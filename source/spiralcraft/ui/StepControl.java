package spiralcraft.ui;

import java.util.HashMap;

/**
 * Controls a user interface component which presents a
 *   linear series of steps to the user.
 */
public abstract class StepControl
  implements Control
{

  private Step[] _steps;
  private int _currentStep;
  private boolean _skip;

  private final AbstractCommand _nextStepCommand
    =new AbstractCommand()
    {
      public void execute()
      { nextStep();
      }

      public boolean isEnabled()
      { return isNextStepEnabled();
      }
    };

  private final AbstractCommand _previousStepCommand
    =new AbstractCommand()
    {
      public void execute()
      { previousStep();
      }

      public boolean isEnabled()
      { return isPreviousStepEnabled();
      }
    };

  protected abstract void stepChanged(Step oldStep,Step newStep);

  private void changeStep(Step oldStep,Step newStep)
  { 
    stepChanged(oldStep,newStep);
    _nextStepCommand.reset();
    _previousStepCommand.reset();
  }

  public void setSkip(boolean val)
  { _skip=val;
  }
  
  public boolean getSkip()
  { return _skip;
  }
  
  public void init()
  { 
    for (int i=0;i<_steps.length;i++)
    { _steps[i].init();
    }

    _steps[_currentStep].stepEntered();
    changeStep(null,_steps[_currentStep]);
  }

  public void destroy()
  { 
    _steps[_currentStep].stepExited();
    changeStep(_steps[_currentStep],null);
    for (int i=0;i<_steps.length;i++)
    { _steps[i].destroy();
    }
  }

  public void setSteps(Step[] val)
  { _steps=val;
  }

  public Command getNextStepCommand()
  { return _nextStepCommand;
  }

  public Command getPreviousStepCommand()
  { return _previousStepCommand;
  }
  
  public final void nextStep()
  { 
    Step previousStep=_steps[_currentStep];
    int nextStepNum=_currentStep+1;
    
    for (;nextStepNum<_steps.length 
          && _steps[nextStepNum].getSkipNext()
        ;nextStepNum++
        );
    
    
    Step nextStep=_steps[nextStepNum];
    previousStep.stepExited();
    nextStep.stepEntered();
    _currentStep=nextStepNum;
    changeStep(previousStep,nextStep);
  }
  
  public final void previousStep()
  {
    Step previousStep=_steps[_currentStep];
    int nextStepNum=_currentStep-1;
    for (;nextStepNum>=0 
          && _steps[nextStepNum].getSkipBack()
        ;nextStepNum--
        );
    
    
    Step nextStep=_steps[nextStepNum];
    previousStep.stepExited();
    nextStep.stepEntered();
    _currentStep=nextStepNum;
    changeStep(previousStep,nextStep);
  }

  public final boolean isNextStepEnabled()
  { 
    return _currentStep+1<_steps.length 
      && _steps[_currentStep].isNextEnabled();
  }

  public final boolean isPreviousStepEnabled()
  { 
    return _currentStep>0  
      && _steps[_currentStep].isBackEnabled();
  }

}
