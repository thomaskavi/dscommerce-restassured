package com.thomaskavi.dscommerce.dscommerce_restassured.controllers;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thomaskavi.dscommerce.dscommerce_restassured.tests.TokenUtil;

import io.restassured.http.ContentType;

public class ProductControllerRA {

  private String clientUsername, clientPassword, adminUsername, adminPassword;
  private String clientToken, adminToken, invalidToken;
  private Long existingProductId, nonExistingProductId, dependentProductId;
  private String productName;

  private Map<String, Object> postProductInstance;

  @BeforeEach
  public void setUp() throws Exception {
    baseURI = "http://localhost:8080";

    clientUsername = "maria@gmail.com";
    clientPassword = "123456";

    adminUsername = "alex@gmail.com";
    adminPassword = "123456";

    clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
    adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
    invalidToken = adminToken + "xpto"; // INVALID TOKEN

    productName = "macbook";

    postProductInstance = new HashMap<>();
    postProductInstance.put("name", "Novo Produto");
    postProductInstance.put("description", "Nova Descrição");
    postProductInstance.put("imgUrl",
        "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
    postProductInstance.put("price", 100.0);

    List<Map<String, Object>> categories = new ArrayList<>();

    Map<String, Object> category1 = new HashMap<>();
    category1.put("id", 2);

    Map<String, Object> category2 = new HashMap<>();
    category2.put("id", 3);

    categories.add(category1);
    categories.add(category2);

    postProductInstance.put("categories", categories);
  }

  @Test
  public void findByIdShouldReturnProductWhenIdExists() {
    existingProductId = 2L;

    given()
        .get("/products/{id}", existingProductId)
        .then()
        .statusCode(200)
        .body("id", is(2))
        .body("name", equalTo("Smart TV"))
        .body("imgUrl",
            equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
        .body("price", is(2190.0F))
        .body("categories.id", hasItems(2, 3))
        .body("categories.name", hasItems("Eletrônicos", "Computadores"));
  }

  @Test
  public void findAllShouldReturnAllProductsPageWhenNameIsEmpty() {

    given()
        .get("/products?page=0")
        .then()
        .statusCode(200)
        .body("content.id", hasItems(3, 6))
        .body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
  }

  @Test
  public void findAllShouldReturnPageProductsWhenNameIsNotEmpty() {

    given()
        .get("/products?name={productName}", productName)
        .then()
        .statusCode(200)
        .body("content.id[0]", is(3))
        .body("content.name[0]", equalTo("Macbook Pro"))
        .body("content.price[0]", is(1250.0F))
        .body("content.imgUrl[0]",
            equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
  }

  @Test
  public void findAllShouldReturnPagedProductWithPriceGreaterThan2000() {

    given()
        .get("/products?size=25")
        .then()
        .statusCode(200)
        .body("content.findAll { it.price > 2000 }.name", hasItems("Smart TV", "PC Gamer Weed"));
  }

  @Test
  public void insertShouldReturnProductCreatedWhenAdminLogged() {

    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + adminToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(201)
        .body("name", equalTo("Novo Produto"))
        .body("price", is(100.0F))
        .body("imgUrl",
            equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"))
        .body("categories.id", hasItems(2, 3));
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidName() {

    postProductInstance.put("name", "ab");
    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + adminToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(422)
        .body("errors.message[0]", equalTo("Nome precisar ter de 3 a 80 caracteres"));
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidDescription() {

    postProductInstance.put("description", "ab");
    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + adminToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(422)
        .body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidPriceIsNegative() {

    postProductInstance.put("price", -100.0);
    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + adminToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(422)
        .body("errors.message[0]", equalTo("O preço deve ser positivo"));
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndInvalidPriceIsZero() {

    postProductInstance.put("price", 0.0);
    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + adminToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(422)
        .body("errors.message[0]", equalTo("O preço deve ser positivo"));
  }

  @Test
  public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndProductHasNoCategory() {

    postProductInstance.put("categories", null);
    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + adminToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(422)
        .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
  }

  @Test
  public void insertShouldReturnForbbidenStatusWhenClientLogged() {

    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + clientToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(403);
  }

  @Test
  public void insertShouldReturnUnauthorizedStatusWhenInvalidToken() {

    JSONObject newProduct = new JSONObject(postProductInstance);

    given()
        .header("Content-type", "application/json")
        .header("Authorization", "Bearer " + invalidToken)
        .body(newProduct)
        .contentType(ContentType.JSON)
        .accept(ContentType.JSON)
        .when()
        .post("/products")
        .then()
        .statusCode(401);
  }

  @Test
  public void deleteShouldReturnNoContentWhenAdminLoggedAndIdExists() {
    existingProductId = 25L;

    given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/products/{id}", existingProductId)
        .then()
        .statusCode(204);
  }

  @Test
  public void deleteShouldReturnNotFoundWhenAdminLoggedAndIdDoesNotExist() {
    nonExistingProductId = 30L;

    given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/products/{id}", nonExistingProductId)
        .then()
        .statusCode(404)
        .body("error", equalTo("Recurso não encontrado"))
        .body("status", equalTo(404));
  }

  @Test
  public void deleteShouldReturnBadRequestWhenAdminLoggedAndDependentId() {
    dependentProductId = 1L;

    given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/products/{id}", dependentProductId)
        .then()
        .statusCode(400);
  }

  @Test
  public void deleteShouldReturnForbbidenWhenClientLogged() {
    existingProductId = 25L;

    given()
        .header("Authorization", "Bearer " + clientToken)
        .when()
        .delete("/products/{id}", existingProductId)
        .then()
        .statusCode(403);
  }

  @Test
  public void deleteShouldReturnUnauthorizedWhenIdExistsAndInvalidToken() throws Exception {
    existingProductId = 25L;

    given()
        .header("Authorization", "Bearer " + invalidToken)
        .when()
        .delete("/products/{id}", existingProductId)
        .then()
        .statusCode(401);
  }
}
