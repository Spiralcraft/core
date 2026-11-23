package spiralcraft.util.string;

import java.util.regex.Pattern;

/**
 * camelCase identifiers in, snake_case names out: "toolCallId" and
 *   "tool_call_id".
 *
 * The contract: on the out side, parts of lowercase letters and digits,
 *   each starting with a letter, joined by single underscores; on the in
 *   side, the same parts with each part after the first starting with its
 *   letter in uppercase and no underscores. Within the contract each
 *   direction undoes the other exactly.
 *
 * A name outside the contract passes through unchanged, in both
 *   directions: a part starting with a digit ("a_1"), a leading, trailing
 *   or doubled underscore, an uppercase letter in a snake_case name, a
 *   camelCase name starting with an uppercase letter ("HTTPStatus").
 *
 * Instances are stateless and thread-safe.
 */
public class CamelSnakeConverter
  implements IdentifierConverter
{
  public static final CamelSnakeConverter INSTANCE
    =new CamelSnakeConverter();

  private static final Pattern SNAKE
    =Pattern.compile("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*");

  private static final Pattern CAMEL
    =Pattern.compile("[a-z][a-z0-9]*([A-Z][a-z0-9]*)*");

  /** snake_case -> camelCase */
  @Override
  public String toIn(String name)
  {
    if (name==null
        || name.indexOf('_')<0
        || !SNAKE.matcher(name).matches()
       )
    { return name;
    }

    StringBuilder out=new StringBuilder(name.length());
    int n=name.length();
    for (int i=0;i<n;i++)
    {
      char c=name.charAt(i);
      if (c=='_')
      { out.append(Character.toUpperCase(name.charAt(++i)));
      }
      else
      { out.append(c);
      }
    }
    return out.toString();
  }

  /** camelCase -> snake_case */
  @Override
  public String toOut(String name)
  {
    if (name==null || !CAMEL.matcher(name).matches())
    { return name;
    }

    StringBuilder out=new StringBuilder(name.length()+4);
    int n=name.length();
    for (int i=0;i<n;i++)
    {
      char c=name.charAt(i);
      if (c>='A' && c<='Z')
      { out.append('_').append(Character.toLowerCase(c));
      }
      else
      { out.append(c);
      }
    }
    return out.toString();
  }
}
