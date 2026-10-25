Feature: Security
  Clients obtain access tokens with the client credentials grant and send them as bearer tokens.
  Listing persons and health checks are public, everything else needs a token with the right scope.

  @02-AC-1 @SEC-1 @SEC-2 @SEC-4
  Scenario: A registered client obtains a signed access token
    When the client requests a token with the scopes "person.read person.write"
    Then a token is issued
    And the token is signed with a key published by the server

  @02-AC-2 @SEC-2
  Scenario: A client with a wrong secret gets no token
    When the client requests a token with a wrong secret
    Then the request is rejected as unauthorized

  @02-AC-3 @SEC-6
  Scenario: Anyone can list persons
    Given the client has no token
    When the client lists persons
    Then the request succeeds

  @02-AC-4 @SEC-7 @SEC-11
  Scenario: Reading a person requires a token
    Given the client has no token
    When the client reads the person with id "1"
    Then the request is rejected as unauthorized

  @02-AC-5 @SEC-8 @SEC-11
  Scenario: Creating a person requires a token
    Given the client has no token
    When the client creates a person "Jane Doe" with e-mail "jane.doe@example.com"
    Then the request is rejected as unauthorized

  @02-AC-6 @SEC-8 @SEC-11
  Scenario: Read access is not enough to create a person
    Given the client has read access
    When the client creates a person "Jane Doe" with e-mail "jane.doe@example.com"
    Then the request is rejected as forbidden

  @02-AC-7 @SEC-9
  Scenario: Anyone can check that the application is alive
    Given the client has no token
    When the client checks whether the application is alive
    Then the request succeeds

  @02-AC-8 @SEC-10
  Scenario: Application information requires a token
    Given the client has no token
    When the client requests the application information
    Then the request is rejected as unauthorized
