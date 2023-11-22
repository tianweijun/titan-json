package titan.json.test.school;

import java.util.Arrays;
import java.util.Objects;

/**
 * School.
 *
 * @author tian wei jun
 */
public class School {
  String name;
  Clazz[] clazzes;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    School school = (School) o;
    return Objects.equals(name, school.name) && Arrays.equals(clazzes, school.clazzes);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name);
    result = 31 * result + Arrays.hashCode(clazzes);
    return result;
  }
}
