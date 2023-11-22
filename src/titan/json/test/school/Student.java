package titan.json.test.school;

import java.util.Arrays;
import java.util.Objects;

/**
 * Student.
 *
 * @author tian wei jun
 */
public class Student {
  String name;
  int age;
  int[] scores;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Student student = (Student) o;
    return age == student.age
        && Objects.equals(name, student.name)
        && Arrays.equals(scores, student.scores);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(name, age);
    result = 31 * result + Arrays.hashCode(scores);
    return result;
  }
}
