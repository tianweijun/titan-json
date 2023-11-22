package titan.json.test.school;

import java.util.Arrays;
import java.util.Objects;

/**
 * Clazz.
 *
 * @author tian wei jun
 */
public class Clazz {
  String name;
  Teacher[] teachers;
  Student[] students;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Clazz clazz = (Clazz) o;
    return Objects.equals(name, clazz.name)
        && Arrays.equals(teachers, clazz.teachers)
        && Arrays.equals(students, clazz.students);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name);
    result = 31 * result + Arrays.hashCode(teachers);
    result = 31 * result + Arrays.hashCode(students);
    return result;
  }
}
