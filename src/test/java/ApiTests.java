import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiTests {
    private final String RANDOMUSER_URL = "https://randomuser.me/api/";

    @Test
    @DisplayName("RandomUserApi - Проверка статус кода")
    public void checkStatusCode() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("RandomUserApi - Получение записей с конкретным Gender")
    public void checkQueryParams() {
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("gender", "male")
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results", RandomUserPojo.class);
        assertTrue(users.stream().allMatch(x -> x.getGender().equals("male")));
    }

    @Test
    @DisplayName("RandomUserApi - Получение списка с ограничением на количество записей")
    public void checkQueryParamsLimiter() {
        int resultsQuantity = 17;
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("results", resultsQuantity)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results", RandomUserPojo.class);
        assertEquals(resultsQuantity, users.size());
    }

    @Test
    @DisplayName("RandomUserApi - Получение >5000 пользователей")
    public void getRandomUsersOverLimit() {
        int resultsQuantity = 10000;
        //При привышении лимита сервер возвращает 1 пользователя
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("results", resultsQuantity)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results", RandomUserPojo.class);
        Assertions.assertThat(users.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("RandomUserApi - Получение 5000 пользователей")
    public void getMaximumRandomUsers() {
        int resultsQuantity = 5000;
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("results", resultsQuantity)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results", RandomUserPojo.class);
        Assertions.assertThat(users.size()).isEqualTo(resultsQuantity);
    }

    @Test
    @DisplayName("RandomUserApi - Установление условия для пароля")
    public void setUpUserPassword() {
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("password", "upper")
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results.login", RandomUserPojo.class);
        Assertions.assertThat(users.get(0).getPassword()).isUpperCase();
    }

    @Test
    @DisplayName("RandomUserApi - Путь к фото профиля соответствует полу юзера")
    public void checkMatchGenderAndPictureEndpoint() {
        String gender = "female";
        int resultsQuantity = 1;
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("gender", gender)
                .queryParam("results", resultsQuantity)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results.picture", RandomUserPojo.class);
        Assertions.assertThat(users.get(0).getThumbnail()).contains("women");
    }

    @Test
    @DisplayName("RandomUserApi - Email заканчивается на @example.com")
    public void checkEmailEndsWith() {
        int resultsQuantity = 100;
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("results", resultsQuantity)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results", RandomUserPojo.class);
        assertTrue(users.stream().allMatch(x -> x.getEmail().endsWith("@example.com")));
    }

    @Test
    @DisplayName("RandomUserApi - Один и тот же user при использовании seed")
    public void getUserBySeed() {
        String seed = "test";
        int resultsQuantity = 1;
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("seed", seed)
                .queryParam("results", resultsQuantity)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results.name", RandomUserPojo.class);
        Assertions.assertThat(users.get(0).getTitle()).isEqualTo("Miss");
        Assertions.assertThat(users.get(0).getFirst()).isEqualTo("Areta");
        Assertions.assertThat(users.get(0).getLast()).isEqualTo("Araújo");
    }

    @Test
    @DisplayName("RandomUserApi - Проверка зависимости адреса и национальности пользователя")
    public void checkMatchingNationalityAndAddress() {
        String nationality = "DE";
        List<RandomUserPojo> users = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("nat", nationality)
                .get(RANDOMUSER_URL)
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList("results.location", RandomUserPojo.class);
        assertTrue(users.stream().allMatch(x -> x.getCountry().equals("Germany")));
    }
}
