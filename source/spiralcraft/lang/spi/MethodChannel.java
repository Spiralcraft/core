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
package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;



public class MethodChannel<Tprop,Tbean>
  extends TranslatorChannel<Tprop,Tbean>
{
  public MethodChannel
    (Channel<Tbean> source
    ,MethodTranslator<Tprop,Tbean> translator
    )
  { super(source,translator,null);
  }

  public MethodChannel
    (Channel<Tbean> source
    ,MethodTranslator<Tprop,Tbean> translator
    ,Channel<?>[] params
    )
  { super(source,translator,params);
  }

  @Override
  public boolean isStatic()
  { return false;
  }

}


