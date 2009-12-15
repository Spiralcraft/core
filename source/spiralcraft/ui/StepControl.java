//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.ui;

import spiralcraft.common.LifecycleException;

import spiralcraft.command.CommandFactory;
import spiralcraft.command.CommandAdapter;
import spiralcraft.command.Command;

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

  public final CommandFactory<Void,Void,Void> nextStep
    =new CommandFactory<Void,Void,Void>()
    {
      @Override
      public Command<Void,Void,Void> command()
      { 
        return new CommandAdapter<Void,Void,Void>()
        {
          @Override
          public void run()
          { nextStep();
          }
        };
      }

      @Override
      public boolean isCommandEnabled()
      { return isNextStepEnabled();
      }
    };

  public final CommandFactory<Void,Void,Void> previousStep
    =new CommandFactory<Void,Void,Void>()
    {
      @Override
      public Command<Void,Void,Void> command()
      { 
        return new CommandAdapter<Void,Void,Void>()
        {
          @Override
          public void run()
          { previousStep();
          }
        };
      }

      @Override
      public boolean isCommandEnabled()
      { return isPreviousStepEnabled();
      }
    };



  protected abstract void stepChanged(Step oldStep,Step newStep);

  private void changeStep(Step oldStep,Step newStep)
  { stepChanged(oldStep,newStep);
  }

  public void setSkip(boolean val)
  { _skip=val;
  }
  
  public boolean getSkip()
  { return _skip;
  }
  
  public void start()
    throws LifecycleException
  { 
    for (int i=0;i<_steps.length;i++)
    { _steps[i].start();
    }

    _steps[_currentStep].stepEntered();
    changeStep(null,_steps[_currentStep]);
  }

  public void stop()
    throws LifecycleException
  { 
    _steps[_currentStep].stepExited();
    changeStep(_steps[_currentStep],null);
    for (int i=0;i<_steps.length;i++)
    { _steps[i].stop();
    }
  }

  public void setSteps(Step[] val)
  { _steps=val;
  }
  
  public final void nextStep()
  { 
    Step previousStep=_steps[_currentStep];
    int nextStepNum=_currentStep+1;
    
    for (;nextStepNum<_steps.length 
          && _steps[nextStepNum].getSkipNext()
        ;nextStepNum++
        ) {}
    
    
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
        ) {}
    
    
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
