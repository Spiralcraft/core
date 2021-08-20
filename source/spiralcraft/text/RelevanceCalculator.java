package spiralcraft.text;

/**
 * Computes a relevance score for a specified set of terms against 
 *   one or more input text fragments.
 * 
 * @author mike
 *
 */
public class RelevanceCalculator
{
//  private final String[] terms;
  private final KmpMatcher[] masterTermMatchers;
  
  public RelevanceCalculator(String[] terms)
  {
//    this.terms=terms;
    masterTermMatchers=new KmpMatcher[terms.length];
    for (int i=0;i<masterTermMatchers.length;i++)
    { masterTermMatchers[i]=new KmpMatcher(terms[i].toLowerCase());
    }
    
  }

  public int score(CharSequence text)
  {
    KmpMatcher[] termMatchers=new KmpMatcher[masterTermMatchers.length];
    for (int i=0;i<termMatchers.length;i++)
    { termMatchers[i]=new KmpMatcher(masterTermMatchers[i]);
    }
    int[] termMatchCounts=new int[termMatchers.length];
    for (int i=0;i<text.length();i++)
    {
      char c=Character.toLowerCase(text.charAt(i));
      for (int j=0;j<termMatchers.length;j++)
      { 
        if (termMatchers[j].match(c))
        { termMatchCounts[j]++;
        }
      }
    }
    int score=0;
    for (int i=0;i<termMatchCounts.length;i++)
    { 
      if (termMatchCounts[i]>0)
      { score+=((8*256)+termMatchCounts[i]);
      }
    }
    return score;
    
  }
  
}