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

  private final Command _nextStepCommand
    =new AbstractCommand()
    {
      public void execute()
      { nextStep();
      }

      public boolean isEnabled()
      { return isNextStepEnabled();
      }
    };

  private final Command _previousStepCommand
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

  public void init()
  { 
    for (int i=0;i<_steps.length;i++)
    { _steps[i].init();
    }

    _steps[_currentStep].stepEntered();
    stepChanged(null,_steps[_currentStep]);
  }

  public void destroy()
  { 
    _steps[_currentStep].stepExited();
    stepChanged(_steps[_currentStep],null);
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
    if (isNextStepEnabled())
    { 
      Step previousStep=_steps[_currentStep];
      Step nextStep=_steps[_currentStep+1];
      previousStep.stepExited();
      nextStep.stepEntered();
      _currentStep++;
      stepChanged(previousStep,nextStep);
    }
  }
  
  public final void previousStep()
  {
    if (isPreviousStepEnabled())
    {
      Step previousStep=_steps[_currentStep];
      Step nextStep=_steps[_currentStep-1];
      previousStep.stepExited();
      nextStep.stepEntered();
      _currentStep--;
      stepChanged(previousStep,nextStep);
    }
  }

  public final boolean isNextStepEnabled()
  { return _currentStep+1<_steps.length;
  }

  public final boolean isPreviousStepEnabled()
  { return _currentStep>0;
  }

}
