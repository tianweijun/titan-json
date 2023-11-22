package titan.json.test.school;

import java.util.Objects;

/**
 * Teacher.
 *
 * @author tian wei jun
 */
public class Teacher {
  String name;
  int age;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Teacher teacher = (Teacher) o;
    return age == teacher.age && Objects.equals(name, teacher.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, age);
  }
}
