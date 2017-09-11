package com.ontestautomation.restassured.authentication;

import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class RestAssuredAuthenticationTest {
	
	Response responseJSON = null;

	@BeforeSuite
	public void requestToken(ITestContext context) {

		String response =
				given().
					parameters("grant_type","client_credentials").
					auth().
					preemptive().
					basic("AUyqLmmlHyX4Th7BdXpIN-sKu5rARNpWLNtQZabRneRp5eDrKEU5pdiNIOMgc-4OiNu4jX8VJwfwWr1a","ECFXJmz2yW0WDf0itUE13jgaBhLkF5kEV9pyzt8iK9vvWgoSBRQ0HCywNIqYftSwXmB6EH_KOGq0nO39").
				when().
					post("https://api.sandbox.paypal.com/v1/oauth2/token").
					asString();

		JsonPath jsonPath = new JsonPath(response);

		String accessToken = jsonPath.getString("access_token");
		
		context.setAttribute("accessToken", accessToken);

		System.out.println("Access token: " + context.getAttribute("accessToken"));
	}

	@Test(priority=1)
	public void checkUserInfoContainsUserId(ITestContext context) {
		responseJSON = 
				given().
					contentType("application/json").auth().oauth2(context.getAttribute("accessToken").toString()).
				when().
					get("https://api.sandbox.paypal.com/v1/identity/openidconnect/userinfo/?schema=openid");

		System.out.println(responseJSON.asString());

		responseJSON.
			then().
				assertThat().body("", hasKey("user_id"));
	}

	@Test(priority=2)
	public void checkNumberOfAssociatedPaymentsIsEqualToZero(ITestContext context) {
		responseJSON = 
				given().contentType("application/json").auth()
					.oauth2(context.getAttribute("accessToken").toString()).
				when().
					get("https://api.sandbox.paypal.com/v1/payments/payment/");

		System.out.println(responseJSON.asString());

		responseJSON.
			then().
				assertThat().body("count", equalTo(0));
	}
}