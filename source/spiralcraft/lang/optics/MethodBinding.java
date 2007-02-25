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
package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;



public class MethodBinding<Tprop,Tbean>
  extends LenseBinding<Tprop,Tbean>
{
  public MethodBinding
    (Binding<Tbean> source
    ,MethodLense<Tprop,Tbean> lense
    )
  { super(source,lense,null);
  }

  public MethodBinding
    (Binding<Tbean> source
    ,MethodLense<Tprop,Tbean> lense
    ,Optic[] params
    )
  { super(source,lense,params);
  }

  public boolean isStatic()
  { return false;
  }

}


