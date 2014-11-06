package pac1.Bean;

import java.util.HashMap;

public interface Basic
{
  public int Add(int a, int b);

  public String retrieveUserNameByUserId(long userId);

  public Object removeCreditCard(HashMap userDetails);
}