//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.lang.kit;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.GenericReflector;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * <p>Publishes a list of available options and a currently selected option based 
 *   on a key function.
 * </p>
 * 
 * @author mike
 *
 * @param <Toptions>
 * @param <Toption>
 * @param <Tkey>
 */
public class SelectorContext<Toptions,Toption,Tkey>
  extends ThreadLocalContext
    <SelectorContext<Toptions,Toption,Tkey>.SelectorState>
{

  private GenericReflector<SelectorContext<?,?,?>> selfReflector;
  private Binding<Tkey> keyX;
  private Callable<Toption,Tkey> keyFunction;
  private Binding<Toptions> optionsX;
  private Binding<Toption> lookupX;
  private Binding<Tkey> selectedKeyX;
  private IterationDecorator<Toptions,Toption> optionsIter;
  
  public SelectorContext()
  { super(BeanReflector.<SelectorState>getInstance(SelectorState.class));
  }
  
  public void setKeyX(Expression<Tkey> keyX)
  { this.keyX=new Binding<Tkey>(keyX);
  }
  
  public void setOptionsX(Expression<Toptions> optionsX)
  { this.optionsX=new Binding<Toptions>(optionsX);
  }

  public void setLookupX(Expression<Toption> lookupX)
  { this.lookupX=new Binding<Toption>(lookupX);
  }

  public void setSelectedKeyX(Expression<Tkey> selectedKeyX)
  { this.selectedKeyX=new Binding<Tkey>(selectedKeyX);
  }
  
  protected void computeSelection()
  {
    SelectorState state=get();
    state.options=optionsX.get();
    state.selectedKey=selectedKeyX.get();
    
    if (lookupX!=null)
    { state.selectedOption=lookupX.get();
    }
    else if (keyX!=null)
    { 
      for (Toption option : optionsIter)
      { 
        Tkey localKey=keyFunction.evaluate(option);
        if (localKey==state.selectedKey
            || (localKey!=null && localKey.equals(state.selectedKey))
           )
        { state.selectedOption=option;
        }
      }
    }
    
  }
  
  public Tkey getSelectedKey()
  { return get().selectedKey;
  }
  
  public Toptions getOptions()
  { return get().options;
  }
  
  public Toption getSelectedOption()
  { return get().selectedOption;
  }
  
  
  @SuppressWarnings("unchecked")
  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  {
    super.bindImports(chain);
    
    optionsX.bind(chain);
    optionsIter=optionsX.decorate(IterationDecorator.class);
    keyFunction
      =new Callable<Toption,Tkey>
        (chain
        ,optionsIter.getComponentReflector()
        ,keyX
        );
    

    selfReflector
      =new GenericReflector<SelectorContext<?,?,?>>
        (BeanReflector.<SelectorContext<?,?,?>>getInstance(getClass()));
    selfReflector.enhance("selectedKey",keyX.getReflector());
    selfReflector.enhance("options",optionsX.getReflector());
    selfReflector.enhance("selectedOption",optionsIter.getComponentReflector());
    chain=chain.chain
      (new SimpleChannel<SelectorContext<?,?,?>>(selfReflector,this,true));

    selectedKeyX.bind(chain);

    if (lookupX!=null)
    { lookupX.bind(chain);
    }

    
    
    return chain.chain(chain.getSubject().resolve(chain,"selectedOption",null));
  }
  
  
  protected SelectorState newSelectorState()
  { return new SelectorState();    
  }
  
  @Override
  protected void pushLocal()
  {
    super.pushLocal();
    set(newSelectorState());
    computeSelection();
  }
  
  @Override
  protected void popLocal()
  { 
    super.popLocal();
  }
  
  class SelectorState
  {
    Tkey selectedKey;
    Toptions options;
    Toption selectedOption;
  }
}
