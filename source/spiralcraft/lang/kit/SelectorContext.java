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
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.GenericReflector;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.lang.util.LangUtil;

/**
 * <p>Publishes a list of available options and a currently selected option 
 *   based on a key function.
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
  private Expression<Toptions> optionsX;
  private Channel<Toptions> optionsC;
  private Reflector<Toptions> optionsType;
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
  { this.optionsX=optionsX;
  }

  public void setOptions(Toptions options)
  {
    if (optionsType==null)
    { this.optionsC=LangUtil.constantChannel(options);
    }
    else
    {
      this.optionsC=new SimpleChannel<Toptions>
        (optionsType
        ,options
        ,true
        );
    }
    
    if (logLevel.isConfig())
    { log.fine("Got options: "+optionsC.getReflector()+" :options="+options);
    }
  }
  
  public void setOptionsType(Reflector<Toptions> optionsType)
  { this.optionsType=optionsType;
  }
  
  public void setLookupX(Expression<Toption> lookupX)
  { this.lookupX=new Binding<Toption>(lookupX);
  }

  public void setSelectedKeyX(Expression<Tkey> selectedKeyX)
  { this.selectedKeyX=new Binding<Tkey>(selectedKeyX);
  }
  
  public Tkey keyOf(Toption option)
  { return keyFunction.evaluate(option);
  }
  
  protected void computeSelection(SelectorState state)
  {
    state.options=optionsC.get();
    if (state.selectedKey==null && selectedKeyX!=null)
    { state.selectedKey=selectedKeyX.get();
    }
    
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
    
    if (optionsX!=null)
    { optionsC=chain.bind(optionsX);
    }
    optionsIter=optionsC.decorate(IterationDecorator.class);
    if (optionsIter==null)
    { throw new BindException(optionsC.getReflector().getTypeURI()+" is not iterable");
    }
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
    selfReflector.enhance
      ("keyOf"
      ,new Reflector<?>[] {optionsIter.getComponentReflector()}
      ,keyX.getReflector()
      );
    selfReflector.enhance("options",optionsC.getReflector());
    selfReflector.enhance("selectedOption",optionsIter.getComponentReflector());
    chain=chain.chain
      (new SimpleChannel<SelectorContext<?,?,?>>(selfReflector,this,true));

    if (selectedKeyX!=null)
    { selectedKeyX.bind(chain);
    }

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
    SelectorState state=newSelectorState();
    set(state);
    computeSelection(state);
  }
  
  @Override
  protected void popLocal()
  { 
    super.popLocal();
  }
  
  protected class SelectorState
  {
    public Tkey selectedKey;
    public Toptions options;
    public Toption selectedOption;
  }
}
