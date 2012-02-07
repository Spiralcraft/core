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
package spiralcraft.ui;

import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.SelectorContext;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.util.Path;

public class NavContext<Toptions,Toption>
  extends SelectorContext<Toptions,Toption,String>
{
  
  protected NavContext<?,?> parent;
  protected String selectedPathSegment;
  protected String name;
  protected Binding<Path> pathX;
  protected Binding<URI> viewResourceX;
  protected int level=0;
  
  public void setPathX(Expression<Path> pathX)
  { this.pathX=new Binding<Path>(pathX);
  }
  
  public Path resolve(Path relativePath)
  {
    if (relativePath.isAbsolute())
    { return relativePath;
    }
    else if (parent==null)
    { return Path.ROOT_PATH.append(relativePath);
    }
    else 
    { return parent.getAbsoluteSelectedPath().append(relativePath.elements());
    }
  }

  @SuppressWarnings("unchecked")
  public Path getAbsoluteSelectedPath()
  { return ((NavState) get()).absoluteSelectedPath;
  }

  @Override
  protected NavState newSelectorState()
  { return new NavState();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected void computeSelection(SelectorState sstate)
  {
    NavState state=(NavState) sstate;
    if (parent==null)
    { 
      if (pathX!=null)
      { state.currentPath=pathX.get();
      }
      state.absoluteSelectedPath
        =new Path(new String[]{state.currentPath.firstElement()},true);
    }
    else
    { 
      state.currentPath=parent.getNextPath();
      if (state.currentPath!=null)
      {
        state.absoluteSelectedPath
          =parent.getAbsoluteSelectedPath().append(state.currentPath.firstElement());
      }
    }
    
    if (state.currentPath!=null)
    {
      if (state.currentPath.size()>1)
      { state.nextPath=state.currentPath.subPath(1);
      }
      state.selectedKey=state.currentPath.firstElement();
    }
    
    if (viewResourceX!=null)
    { state.viewResource=viewResourceX.get();
    }

    super.computeSelection(state);
  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { 
    parent=LangUtil.findInstance(NavContext.class,chain);
    if (parent!=null)
    { level=parent.level+1;
    }
    
    if (pathX!=null)
    { pathX.bind(chain);
    }
    

    Focus<?> selectedOptionFocus=super.bindImports(chain);
    if (viewResourceX!=null)
    { viewResourceX.bind(selectedOptionFocus);
    }
    return selectedOptionFocus;
  }
  
  public NavContext<?,?> forLevel(int level)
  { 
    if (level<0)
    { return null;
    }
    
    if (level<this.level)
    { return parent.forLevel(level);
    }
    else if (level==this.level)
    { return this;
    }
    else
    { return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  protected Path getNextPath()
  { return ((NavState) get()).nextPath;
  }

  
  @SuppressWarnings("unchecked")
  public URI getViewResourceURI()
  { return ((NavState) get()).viewResource;
  }
  
  public void setViewResourceX(Binding<URI> viewResourceX)
  { this.viewResourceX=viewResourceX;
  }
  
  protected class NavState
    extends SelectorState
  { 
    Path currentPath;
    Path nextPath;
    Path absoluteSelectedPath;
    URI viewResource;
  }
  
}
