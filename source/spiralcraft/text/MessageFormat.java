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
package spiralcraft.text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.markup.MarkupHandler;
import spiralcraft.text.markup.MarkupParser;

/**
 * <p>Generates a message from a basic markup template syntax.
 * </p>
 * 
 * <p>This is a placeholder to support future I18N by providing a
 *   ResourceBundle for the templates
 * </p>
 * 
 * @author mike
 *
 */
public class MessageFormat
  implements Contextual,Renderer
{
  private static final MarkupParser substitutionParser
    =new MarkupParser("{|","|}",'\\');
  
  private static final ClassLog log
    =ClassLog.getInstance(MessageFormat.class);
  
  public static final MessageFormat create(String template)
  { 
    try
    {
      return new MessageFormat(template);
    }
    catch (MarkupException x)
    { throw new RuntimeException(x);
    }
    catch (spiralcraft.text.ParseException x)
    { throw new RuntimeException(x);
    }
  }
  
  private List<AbstractRenderer> renderers
    =new LinkedList<AbstractRenderer>();
  private String template;

  public MessageFormat(String template)
    throws spiralcraft.text.ParseException,MarkupException
  {
    this.template=template;
    substitutionParser.parse
      (template
      ,new MarkupHandler()
      {
        private ParsePosition position;
        
        @Override
        public void handleContent(
          CharSequence text)
        { renderers.add(new ContentRenderer(text));
        }

        @Override
        public void handleMarkup(
          CharSequence code)
          throws spiralcraft.text.ParseException
        { 
          try
          { renderers.add(new MarkupRenderer(code));
          }
          catch (ParseException x)
          { throw new spiralcraft.text.ParseException
              ("Error parsing expression '"+code+"'",position,x);
          }
        }

        @Override
        public void setPosition(ParsePosition position)
        { this.position=position;
          
        } 
      }
      ,null
      );
  }
  
  @Override
  public void render(Appendable appendable)
    throws IOException
  {
    for (Renderer renderer:renderers)
    { renderer.render(appendable);
    }
  }
  
  /**
   * Convenience method to render to a String
   * 
   * @return
   */
  public String render()
  { 
    StringBuilder builder=new StringBuilder();
    try
    { render(builder);
    }
    catch (IOException x)
    { log.log(Level.WARNING,"Caught exception rendering ["+template+"]",x);
    }
    return builder.toString();
  }

  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws ContextualException
  {
    for (AbstractRenderer renderer:renderers)
    { renderer.bind(focusChain);
    }
    return focusChain;
  }

}

abstract class AbstractRenderer
  implements Renderer,Contextual
{
 
}

class ContentRenderer
  extends AbstractRenderer
{
  
  private CharSequence content;
  
  public ContentRenderer(CharSequence content)
  { this.content=content;
  }
  
  @Override
  public void render(Appendable out)
    throws IOException
  { out.append(content);
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }
  
}

class MarkupRenderer
  extends AbstractRenderer
{
  
  private Binding<?> binding;
  
  public MarkupRenderer(CharSequence expression) 
    throws ParseException
  { this.binding=new Binding<Object>(expression.toString());
  }
  
  @Override
  public void render(Appendable out)
    throws IOException
  { 
    Object ret=binding.get();
    if (ret!=null)
    { out.append(ret.toString());
    }
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  { 
    binding.bind(focusChain);
    return focusChain;
  }

}
