//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.sax;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.Stack;

import org.xml.sax.SAXException;

//import spiralcraft.log.ClassLog;
import spiralcraft.util.MutableInt;

/**
 * Utility class for SAX processing
 * 
 * @author mike
 *
 */
public class SaxUtil
{
//  private static final ClassLog log=ClassLog.getInstance(SaxUtil.class);
  
  /**
   * Ellipsize an XML fragment. If the tree contains textual
   *   content in excess of threshold characters, trim the textual content
   *   to no more than trimLength characters, add the ellipsis markup
   *   after the trimmed text, and truncate the tree after the trimmed
   *   Element. 
   * 
   * @param fragment
   * @param threshold
   * @param trimLength
   * @return
   */
  public static String ellipsizeFragment
    (String fragment
    ,int threshold
    ,int trimLength
    ,String ellipsisText
    ,Locale locale
    ) throws SAXException, IOException
  { 
    
    if (locale==null)
    { locale=Locale.getDefault();
    }
    ParseTree tree=ParseTreeFactory.fromFragment(fragment,null);
    ellipsize
      (tree.getDocument()
      ,threshold
      ,trimLength
      ,ellipsisText
      ,locale
      );
    return ParseTreeFactory.toFragment(tree);
  }

  
  /**
   * Ellipsize a text based node tree. If the tree contains textual
   *   content in excess of threshold characters, trim the textual content
   *   to no more than trimLength characters, add the ellipsis markup
   *   after the trimmed text, and truncate the tree after the trimmed
   *   Element. 
   * 
   * @param root
   * @param threshold
   * @param trimLength
   * @return
   */
  public static Node ellipsize
    (Node root
    ,int threshold
    ,int trimLength
    ,String ellipsisText
    ,Locale locale
    )
  { 
    
    if (threshold<trimLength)
    { threshold=trimLength;
    }

    int charcount=0;

    Characters mark=null;
    int markOffset=0;
    int[] markPath=null;
    
    Node current=root;
    
    Stack<MutableInt> path=new Stack<MutableInt>();
    path.push(new MutableInt());
    
    while (!path.isEmpty() && charcount<threshold)
    {
      if (current instanceof Characters)
      { 
        Characters charsNode=(Characters) current;
        char[] characters=charsNode.getCharacters();
        if (mark==null)
        {
          if (charcount+characters.length>=trimLength)
          { 
            // This is the node we need to slice
           mark=charsNode;
             markOffset=trimLength-charcount;
            markPath=new int[path.size()-1];
          
            int i=0;
            for (MutableInt childNum:path)
            { 
              if (i<markPath.length)
              { 
                markPath[i]=childNum.value-1;

//                log.fine("Path: "+childNum.value); 
              }
              i++;
            }
          }
        }
        
        charcount+=characters.length;
        if (charcount>=threshold)
        { break;
        }
      }
      
      while (!path.isEmpty()
            && path.peek().value
              ==current.getChildCount()
            )
      { 
        // Back up the tree until we find a non-exhausted parent
        path.pop();
        current=current.getParent();
      }
      
      if (!path.isEmpty())
      {
        int childnum=path.peek().value++;
        current=current.getChild(childnum);
//        log.fine("Descending into child "+childnum+": "+current);
//        log.fine("Stack: "+path);
        path.push(new MutableInt());
      }
      
    }
    
    if (mark!=null && charcount>=threshold)
    {
      // Slice the marked node and truncate the rest of the tree
      BreakIterator breakIterator=BreakIterator.getWordInstance(locale);
      String characters=new String(mark.getCharacters());
      breakIterator.setText(characters);
      
      int offset=breakIterator.preceding(markOffset);
      if (offset==BreakIterator.DONE)
      { offset=markOffset;
      }
      mark.setCharacters
        ((characters.substring(0,offset)+ellipsisText).toCharArray());
      
      current=root;
      for (int pathSeg : markPath)
      { 
        current.truncate(pathSeg+1);
        current=current.getChild(pathSeg);
      }
      
    }
    
    return root;
    
    
  }


}
