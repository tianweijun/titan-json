package titan.json.test.school;

import java.io.IOException;
import java.io.InputStream;
import titan.json.Json;

/**
 * JsonApplicationTest.
 *
 * @author tian wei jun
 */
public class ToJsonApplicationTest {

  public static void main(String[] args) throws IOException {

    InputStream jsonInputStream =
        ToJsonApplicationTest.class
            .getClassLoader()
            .getResourceAsStream("titan/json/test/school/school.json");

    School school = Json.fromJson(jsonInputStream, School.class);

    if (null != jsonInputStream) {
      try {
        jsonInputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    String str = Json.toJson(school);
    System.out.println(str);
    School strSchool = Json.fromJson(str, School.class);
    System.out.println("school==strSchool: " + school.equals(strSchool));

    String prettyStr = Json.toPrettyJson(school);
    System.out.println(prettyStr);
    School strPrettySchool = Json.fromJson(prettyStr, School.class);
    System.out.println("school==strPrettySchool: " + school.equals(strPrettySchool));

    System.out.println("ToJsonApplicationTest success.");
  }
}
